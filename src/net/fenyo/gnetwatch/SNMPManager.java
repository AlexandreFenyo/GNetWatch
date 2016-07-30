
/*
 * GNetWatch
 * Copyright 2006, 2007, 2008 Alexandre Fenyo
 * gnetwatch@fenyo.net
 *
 * This file is part of GNetWatch.
 *
 * GNetWatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GNetWatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GNetWatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.fenyo.gnetwatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.io.*;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.mp.*;
import org.snmp4j.transport.*;
import org.snmp4j.event.*;
import org.snmp4j.security.*;

/**
 * This class maintains general structures to deal with SNMP: transport, security model, ...
 * This class delivers SNMP queriers.
 * @author Alexandre Fenyo
 * @version $Id: SNMPManager.java,v 1.11 2008/04/15 23:58:17 fenyo Exp $
 */

public class SNMPManager {
  private static Log log = LogFactory.getLog(SNMPManager.class);
  private final Snmp snmp;
  
  /**
   * Constructor.
   * @param none.
   * @throws IOException SNMP4J exception.
   */
  public SNMPManager() throws IOException {
    TransportMapping transport = new DefaultUdpTransportMapping();
    snmp = new Snmp(transport);
    final USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
    SecurityModels.getInstance().addSecurityModel(usm);
    transport.listen();
  }

  /**
   * Creates a new querier.
   * @param address target address.
   * @return SNMPQuerier new querier.
   */
  public SNMPQuerier getQuerier(final InetAddress address) {
    return new SNMPQuerier(address, this);
  }

  /**
   * Returns the SNMP4J Snmp instance used to perform further SNMP queries.
   * @param none.
   * @return Snmp Snmp instance.
   */
  protected Snmp getSNMP() {
    return snmp; 
   }
}
