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

import net.fenyo.gnetwatch.GUI.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.util.Arrays;
import java.io.*;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.mp.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;
import org.snmp4j.event.*;
import org.snmp4j.security.*;

/**
 * An SNMP querier maintains SNMP parameters needed for the manager to talk to
 * an agent.
 * 
 * @author Alexandre Fenyo
 * @version $Id: SNMPQuerier.java,v 1.33 2008/10/31 19:00:20 fenyo Exp $
 */

// grosse limitation en V3 : si 2 targets ont le même username mais pas le même
// pass ni valeur de sec, ca marchera pas
// car on n'a pas géré les engines
public class SNMPQuerier {
  private static Log  log           = LogFactory.getLog(SNMPQuerier.class);

  // persistent
  private Long        id;

  // persistent - not null
  private InetAddress address;

  // persistent - not null
  private int         version       = 0;                                   // SNMPv1

  // persistent - not null
  private int         sec           = SecurityLevel.AUTH_PRIV;

  // persistent - not null
  private int         retries       = 3;

  // persistent - not null
  private int         timeout       = 1500;                                // microsec

  // persistent - not null
  private int         port          = 161;

  // persistent - not null
  private String      community     = "public";

  // persistent - not null
  private String      username      = "";

  // persistent - not null
  private String      password_auth = "";

  // persistent - not null
  private String      password_priv = "";

  // persistent - not null
  private int         pdu_max_size  = 1000;

  // persistent - not null
  private boolean     snmp_capable  = false;

  // persistent
  private String      last_sysdescr = null;

  private SNMPManager snmp_manager;

  private Target      snmp_target;

  /**
   * Interface used to manage asynchronous SNMP answers.
   */
  public interface QuerierListener {
    /**
     * Called for each ansynchronous answer.
     * 
     * @param event
     *          response event recived.
     * @return void.
     */
    public void onResponse(final ResponseEvent event);

    /**
     * Called after a timeout occured.
     * 
     * @param event
     *          timeout event.
     * @return void.
     */
    public void onTimeout(final ResponseEvent event);
  }

  /**
   * Constructor.
   * 
   * @param address
   *          target address.
   * @param snmp_manager
   *          SNMPManager instance.
   */
  protected SNMPQuerier(final InetAddress address,
      final SNMPManager snmp_manager) {
    this.snmp_manager = snmp_manager;
    this.address = address;
    parametersHaveChanged();
  }

  /**
   * Default constructor.
   * 
   * @param none.
   */
  protected SNMPQuerier() {
    this.snmp_manager = null;
    this.address = null;
  }

  public Target getSNMPTarget() {
    return snmp_target;
  }

  public void setSNMPTarget(final Target snmp_target) {
    this.snmp_target = snmp_target;
  }

  public SNMPManager getSNMPManager() {
    return snmp_manager;
  }

  public void setSNMPManager(final SNMPManager snmp_manager) {
    this.snmp_manager = snmp_manager;
  }

  public Long getId() {
    return id;
  }

  protected void setId(final Long id) {
    this.id = id;
  }

  /**
   * Returns the target address.
   * 
   * @param none.
   * @return InetAddress target address.
   */
  public InetAddress getAddress() {
    return address;
  }

  public void setAddress(final InetAddress address) {
    this.address = address;
  }

  /**
   * Checks that the associated target has already answered a SNMP query.
   * 
   * @param none.
   * @return boolean true if the target has already answered a SNMP query.
   */
  public boolean isSNMPCapable() {
    return snmp_capable;
  }

  public void setSNMPCapable(final boolean snmp_capable) {
    this.snmp_capable = snmp_capable;
  }

  /**
   * Returns the last sysdescr returned.
   * 
   * @param none.
   * @return String system description.
   */
  public String getLastSysdescr() {
    return last_sysdescr;
  }

  public void setLastSysdescr(final String last_sysdescr) {
    this.last_sysdescr = last_sysdescr;
  }

  /**
   * Must be called after some setters have been called.
   * 
   * @param none.
   * @return void.
   */
  public void update() {
    parametersHaveChanged();
  }

  /**
   * Must be called after some setters have been called.
   * 
   * @param none.
   * @return void.
   */
  // final since it is invoked from the constructor
  public final void parametersHaveChanged() {
    if (version == 0 || version == 1) {
      snmp_target = new CommunityTarget();
      snmp_target.setAddress(new UdpAddress(address, port));
      snmp_target.setVersion((version == 0) ? SnmpConstants.version1
          : SnmpConstants.version2c);
      ((CommunityTarget) snmp_target).setCommunity(new OctetString(community));

    } else {
      try {
        final UsmUserEntry entry = snmp_manager.getSNMP().getUSM()
            .getUserTable().getUser(new OctetString(username));
        if (entry != null
            && snmp_manager.getSNMP().getUSM().removeUser(entry.getEngineID(),
                entry.getUserName()) == null)
          log.error("USM user not found");
        snmp_manager.getSNMP().getUSM().addUser(
            new OctetString(username),
            new UsmUser(new OctetString(username),
                sec != SecurityLevel.NOAUTH_NOPRIV ? AuthMD5.ID : null,
                sec != SecurityLevel.NOAUTH_NOPRIV ? new OctetString(
                    password_auth) : null,
                sec == SecurityLevel.AUTH_PRIV ? PrivDES.ID : null,
                sec == SecurityLevel.AUTH_PRIV ? new OctetString(password_priv)
                    : null));
        snmp_target = new UserTarget(new UdpAddress(address, port),
            new OctetString(username), new byte[] {}, sec);
        snmp_target.setVersion(SnmpConstants.version3);
      } catch (final IllegalArgumentException ex) {
        log.error("Exception", ex);
      }
    }

    snmp_target.setRetries(retries);
    snmp_target.setTimeout(timeout);
    snmp_target.setMaxSizeRequestPDU(pdu_max_size);
  }

  /**
   * Returns the version attribute.
   * 
   * @param none.
   * @return int version attribute.
   */
  public int getVersion() {
    return version;
  }

  /**
   * Returns the security attribute.
   * 
   * @param none.
   * @return int security attribute.
   */
  public int getSec() {
    return sec;
  }

  /**
   * Returns the retries attribute.
   * 
   * @param none.
   * @return int retries attribute.
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Returns the timeout attribute.
   * 
   * @param none.
   * @return int timeout attribute.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Returns the agent UDP port attribute.
   * 
   * @param none.
   * @return int agent UDP port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the community string.
   * 
   * @param none.
   * @return String community string.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Returns the username attribute.
   * 
   * @param none.
   * @return String username attribute.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the password authentication attribute.
   * 
   * @param none.
   * @return String password authentication attribute.
   */
  public String getPasswordAuth() {
    return password_auth;
  }

  /**
   * Returns the password privacy attribute.
   * 
   * @param none.
   * @return String password privacy attribute.
   */
  public String getPasswordPriv() {
    return password_priv;
  }

  /**
   * Returns the PDU maximum size attribute.
   * 
   * @param none.
   * @return int PDU maximum size attribute.
   */
  public int getPDUMaxSize() {
    return pdu_max_size;
  }

  /**
   * Sets the version attribute.
   * 
   * @param version
   *          version.
   * @return void.
   */
  public void setVersion(final int version) {
    this.version = version;
  }

  /**
   * Sets the security attribute.
   * 
   * @param sec
   *          security attribute.
   * @return void.
   */
  public void setSec(final int sec) {
    this.sec = sec;
  }

  /**
   * Sets the retries attribute.
   * 
   * @param retries
   *          retries attribute.
   * @return void.
   */
  public void setRetries(final int retries) {
    this.retries = retries;
  }

  /**
   * Sets the timeout attribute.
   * 
   * @param timeout
   *          timeout attribute.
   * @return void.
   */
  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

  /**
   * Sets the agent UDP port attribute.
   * 
   * @param port
   *          agent UDP port attribute.
   * @return void.
   */
  public void setPort(final int port) {
    this.port = port;
  }

  /**
   * Sets the community string attribute.
   * 
   * @param community
   *          community string attribute.
   * @return void.
   */
  public void setCommunity(final String community) {
    this.community = community;
  }

  /**
   * Sets the username attribute.
   * 
   * @param username
   *          username attribute.
   * @return void.
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * Sets the password authentication attribute.
   * 
   * @param password_auth
   *          password authentication attribute.
   * @return void.
   */
  public void setPasswordAuth(final String password_auth) {
    this.password_auth = password_auth;
  }

  /**
   * Sets the password privacy attribute.
   * 
   * @param password_priv
   *          password_privacy attribute.
   * @return void.
   */
  public void setPasswordPriv(final String password_priv) {
    this.password_priv = password_priv;
  }

  /**
   * Sets the PDU maximum size attribute.
   * 
   * @param pdu_max_size
   *          PDU maximum size attribute.
   * @return void.
   */
  public void setPDUMaxSize(final int pdu_max_size) {
    this.pdu_max_size = pdu_max_size;
  }

  /**
   * Returns the SNMP4J Snmp instance used to perform further SNMP queries.
   * 
   * @param none.
   * @return Snmp Snmp instance.
   */
  private Snmp getSNMP() {
    return snmp_manager.getSNMP();
  }

  private void setSNMP(final SNMPManager snmp_manager) {
    this.snmp_manager = snmp_manager;
  }

  /**
   * Creates a new empty PDU.
   * 
   * @param none.
   * @return PDU new PDU.
   */
  private PDU getPDU() {
    if (version == 0)
      return new PDUv1();
    if (version == 1)
      return new PDU();
    return new ScopedPDU();
  }

  private String variableToString(final org.snmp4j.smi.Variable var) {
    if (org.snmp4j.smi.OctetString.class.isInstance(var))
      return ((org.snmp4j.smi.OctetString) var).toASCII('.');
    else
      return var.toString();
  }

  /**
   * Gets the system description OID content synchronously.
   * 
   * @param none.
   * @return String system description.
   */
  // snmptranslate -Td IF-MIB::ifName.1
  // récupérer une table :
  // http://lists.agentpp.org/pipermail/snmp4j/2005-October/000786.html
  // lock survey : sync_tree << synchro << HERE
  public String getSysDescr() {
    final PDU pdu = getPDU();
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1")));
    pdu.setType(PDU.GETNEXT);
    try {
      final ResponseEvent response = getSNMP().send(pdu, snmp_target);

      if (response.getResponse() != null) {

        if (response.getResponse().get(0) != null
            && response.getResponse().get(0).getVariable() != null) {
          snmp_capable = true;
          last_sysdescr = response.getResponse().get(0).getVariable()
              .toString();
        }

        return response.getResponse().toString();
      }
    } catch (final IOException ex) {
      log.error("Exception", ex);
    }
    return null;
  }

  private final java.util.Map<Integer, String> ifDescrCache = new java.util.HashMap<Integer, String>();

  private String getIfDescr(final int idx) {
    if (ifDescrCache.get(idx) != null)
      return ifDescrCache.get(idx);
    final PDU pdu = getPDU();
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.2." + idx)));
    pdu.setType(PDU.GET);
    try {
      final ResponseEvent response = getSNMP().send(pdu, snmp_target);
      if (response != null && response.getResponse() != null
          && response.getResponse().get(0) != null
          && response.getResponse().get(0).getVariable() != null
          && response.getResponse().get(0).getVariable().toString() != null) {
        String retval = variableToString(response.getResponse().get(0)
            .getVariable());
        if (retval.equals("noSuchInstance"))
          retval = "";
        return retval;
      }
    } catch (final IOException ex) {
      log.error("Exception", ex);
    }
    return null;
  }

  private String htmlFace(final String html) {
    return "<FONT FACE='Verdana' size='-2'>" + html + "</FONT>";
  }

  // snmptranslate -m ALL -M"/usr/share/snmp/mibs:$HOME/MIBs/Cisco:$HOME/MIBS/3Com" -OXn
  // RFC1213-MIB::sysServices.0 2> /dev/null
  public String getGeneralInformation() {
    StringBuffer result = new StringBuffer(
        "<HR/><B>General information for host " + getAddress() + "</B><BR/>");

    final PDU pdu = getPDU();
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"))); // RFC1213-MIB::sysName.0
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.3.0"))); // RFC1213-MIB::sysUpTimeInstance.0
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.6.0"))); // RFC1213-MIB::sysLocation.0
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.4.0"))); // RFC1213-MIB::sysContact.0
    pdu.setType(PDU.GET);
    try {
      final ResponseEvent response = getSNMP().send(pdu, snmp_target);

      if (response != null && response.getResponse() != null
          && response.getResponse().get(0) != null
          && response.getResponse().get(0).getVariable() != null
          && response.getResponse().get(0).getVariable().toString() != null)
        result.append("host name: "
            + response.getResponse().get(0).getVariable().toString() + "<BR/>");

      if (response != null && response.getResponse() != null
          && response.getResponse().get(1) != null
          && response.getResponse().get(1).getVariable() != null
          && response.getResponse().get(1).getVariable().toString() != null)
        result.append("uptime: "
            + response.getResponse().get(1).getVariable().toString() + "<BR/>");

      if (response != null && response.getResponse() != null
          && response.getResponse().get(3) != null
          && response.getResponse().get(3).getVariable() != null
          && response.getResponse().get(3).getVariable().toString() != null)
        result.append("contact: "
            + response.getResponse().get(3).getVariable().toString() + "<BR/>");

      if (response != null && response.getResponse() != null
          && response.getResponse().get(2) != null
          && response.getResponse().get(2).getVariable() != null
          && response.getResponse().get(2).getVariable().toString() != null)
        result.append("location: "
            + response.getResponse().get(2).getVariable().toString() + "<BR/>");

    } catch (final IOException ex) {
      log.error("Exception", ex);
    }

    return result.toString();
  }

  public String getRoutingTable(final TransientGUI tg) {
    StringBuffer result = new StringBuffer("<HR/><B>Routing table for host "
        + getAddress() + "</B><BR/>");

    // ipRouteTable
    final String route_proto[] = { "gnetwatch error", "other", "local",
        "netmgmt", "icmp", "egp", "ggp", "hello", "rip", "is-is", "es-is",
        "ciscoIgrp", "bbnSpfIgp", "ospf", "bgp" };
    final String route_type[] = { "gnetwatch error", "other", "invalid",
        "direct", "indirect" };
    result.append("<P/><B>Routing table</B><BR/>");
    final PDUFactory pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
    TableUtils table_utils = new TableUtils(getSNMP(), pdu_factory);
    final OID[] cols = new OID[] { new OID("1.3.6.1.2.1.4.21.1.9"), // IF-MIB::ipRouteProto
        new OID("1.3.6.1.2.1.4.21.1.1"), // IF-MIB::ipRouteDest
        new OID("1.3.6.1.2.1.4.21.1.11"), // IF-MIB::ipRouteMask
        new OID("1.3.6.1.2.1.4.21.1.2"), // IF-MIB::ipRouteIfIndex
        new OID("1.3.6.1.2.1.4.21.1.7"), // IF-MIB::ipRouteNextHop
        new OID("1.3.6.1.2.1.4.21.1.8"), // IF-MIB::ipRouteType
        new OID("1.3.6.1.2.1.4.21.1.10"), // IF-MIB::ipRouteAge
        new OID("1.3.6.1.2.1.4.21.1.3"), // IF-MIB::ipRouteMetric1
        new OID("1.3.6.1.2.1.4.21.1.4"), // IF-MIB::ipRouteMetric2
        new OID("1.3.6.1.2.1.4.21.1.5"), // IF-MIB::ipRouteMetric3
        new OID("1.3.6.1.2.1.4.21.1.6"), // IF-MIB::ipRouteMetric4
        new OID("1.3.6.1.2.1.4.21.1.12"), // IF-MIB::ipRouteMetric5
        new OID("1.3.6.1.2.1.4.21.1.13"), // IF-MIB::ipRouteInfo
    };
    result
        .append("<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR bgcolor='lightyellow' align='right'>");
    result.append("<TD><B>" + htmlFace("proto") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("destination") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("netmask") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("interface") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("next hop") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("type") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("age") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("metric1") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("metric2") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("metric3") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("metric4") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("metric5") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("info") + "</B></TD>");
    result.append("</TR>");
    java.util.List<TableEvent> table = table_utils.getTable(snmp_target, cols, null, null);
    if (table != null)
      for (final TableEvent table_event : table) {
        if (tg.isFinished() == true) return "";
        final VariableBinding[] columns = table_event.getColumns();
        result.append("<TR bgcolor='lightyellow' align='right'>");
        if (columns != null && columns.length >= 13) {
          final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[13];
          for (int i = 0; i <= 12; i++)
            if (columns[i] == null)
              variable[i] = null;
            else
              variable[i] = columns[i].getVariable();
          for (int i = 0; i <= 12; i++)
            if (variable[i] == null)
              result.append("<TD/>");
            else if (i == 0)
              result.append("<TD>" + htmlFace(route_proto[variable[i].toInt()])
                  + "</TD>");
            else if (i == 5)
              result.append("<TD>" + htmlFace(route_type[variable[i].toInt()])
                  + "</TD>");
            else if (i == 3)
              result.append("<TD>" + htmlFace(getIfDescr(variable[i].toInt()))
                  + "</TD>");
            else
              result
                  .append("<TD>" + htmlFace(variable[i].toString()) + "</TD>");
        } else
          for (int i = 0; i <= 12; i++)
            result.append("<TD>" + htmlFace("error") + "</TD>");
        result.append("</TR>");
      }
    result.append("</TABLE>");
    return result.toString();
  }

  public String getArpTable(final TransientGUI tg) {
    StringBuffer result = new StringBuffer("<HR/><B>ARP table for host "
        + getAddress() + "</B><BR/>");

    // ipNetToMedia
    result.append("<P/><B>ARP table</B><BR/>");
    final String type[] = { "gnetwatch error", "other", "invalid", "dynamic",
        "static" };

    final PDUFactory pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
    TableUtils table_utils = new TableUtils(getSNMP(), pdu_factory);
    final OID[] cols = new OID[] { new OID("1.3.6.1.2.1.4.22.1.1"), // RFC1213-MIB::ipNetToMediaIfIndex
        new OID("1.3.6.1.2.1.4.22.1.2"), // RFC1213-MIB::ipNetToMediaPhysAddress
        new OID("1.3.6.1.2.1.4.22.1.3"), // RFC1213-MIB::ipNetToMediaNetAddress
        new OID("1.3.6.1.2.1.4.22.1.4"), // RFC1213-MIB::ipNetToMediaType
    };

    result.append("<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR bgcolor='lightyellow' align='right'>");
    result.append("<TD><B>" + htmlFace("interface") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("mac address") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("ip address") + "</B></TD>");
    result.append("<TD><B>" + htmlFace("entry type") + "</B></TD>");
    result.append("</TR>");

    java.util.List<TableEvent> table = table_utils.getTable(snmp_target, cols, null, null);
    if (table != null)
      for (final TableEvent table_event : table) {
        if (tg.isFinished() == true) return "";
        final VariableBinding[] columns = table_event.getColumns();
        result.append("<TR bgcolor='lightyellow' align='right'>");
        if (columns != null && columns.length >= 4) {
          final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[4];
          for (int i = 0; i <= 3; i++)
            if (columns[i] == null)
              variable[i] = null;
            else
              variable[i] = columns[i].getVariable();
          for (int i = 0; i <= 3; i++)
            if (variable[i] == null)
              result.append("<TD/>");
            else if (i == 0)
              result.append("<TD>" + htmlFace(getIfDescr(variable[i].toInt()))
                  + "</TD>");
            else if (i == 3)
              result.append("<TD>" + htmlFace(type[variable[i].toInt()])
                  + "</TD>");
            else
              result
                  .append("<TD>" + htmlFace(variable[i].toString()) + "</TD>");
        } else
          for (int i = 0; i <= 3; i++)
            result.append("<TD>" + htmlFace("error") + "</TD>");
        result.append("</TR>");
      }

    result.append("</TABLE>");
    return result.toString();
  }

  public String getGenericTable(final TransientGUI tg,
      final String tablename, final String oid, final int oids[], final String names[], Object fields[][]) {
    final java.util.HashMap<Integer, String []> fieldsmap = new java.util.HashMap<Integer, String []>();
    for (final Object fieldslist[] : fields) {
      final String foo[] = new String [fieldslist.length - 1];
      for (int i = 1; i < fieldslist.length; i++) foo[i - 1] = (String) fieldslist[i];
      fieldsmap.put((Integer) fieldslist[0], foo);
    }

    StringBuffer result = new StringBuffer("<HR/><B>Table '" + tablename + "' for host "
        + getAddress() + "</B><BR/><P/>");

    final PDUFactory pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
    TableUtils table_utils = new TableUtils(getSNMP(), pdu_factory);
    final OID cols[] = new OID[oids.length];
    int cnt = 0;
    for (final int i : oids) cols[cnt++] = new OID(oid + "." + i);

    result.append("<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR bgcolor='lightyellow' align='right'>");
    for (final String name : names) result.append("<TD><B>" + htmlFace(name) + "</B></TD>");
    result.append("</TR>");
    java.util.List<TableEvent> table = table_utils.getTable(snmp_target, cols, null, null);
    if (table != null)
      for (final TableEvent table_event : table) {
        if (tg.isFinished() == true) return "";
        final VariableBinding columns[] = table_event.getColumns();
        result.append("<TR bgcolor='lightyellow' align='right'>");
        if (columns != null && columns.length >= oids.length) {
          final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[oids.length];
          for (int i = 0; i < oids.length; i++)
            if (columns[i] == null) variable[i] = null;
            else variable[i] = columns[i].getVariable();
          for (int i = 0; i < oids.length; i++)
            if (variable[i] == null) result.append("<TD/>");
            else if (fieldsmap.containsKey(i)) {
              final String app;
              if (fieldsmap.get(i).length == 0) app = getIfDescr(variable[i].toInt());
              else if (fieldsmap.get(i)[0].equals("#idx"))
                app = new Integer(columns[i].getOid().getValue()[columns[i].getOid().getValue().length - 1]).toString();
              else if (variable[i].toInt() < fieldsmap.get(i).length) app = fieldsmap.get(i)[variable[i].toInt()];
              else app = variable[i].toString();
              result.append("<TD>" + htmlFace(app) + "</TD>");
            }
            else result.append("<TD>" + htmlFace(variable[i].toString()) + "</TD>");
        } else
          for (int i = 0; i < oids.length; i++)
            result.append("<TD>" + htmlFace("error") + "</TD>");
        result.append("</TR>");
      }
    result.append("</TABLE>");
    return result.toString();
  }

  /***********************************************************/

  public String getMACTable(final TransientGUI tg) {
    StringBuffer result;

    final java.util.ArrayList<Integer> vlans = new java.util.ArrayList();
    final java.util.HashMap<Integer, java.util.List<String>> if2mac =
      new java.util.HashMap<Integer, java.util.List<String>>();

    /***********************************************************/
    // get vlan list

    String oid = "1.3.6.1.4.1.9.9.46.1.3.1.1";
    int oids[] = new int [] {18};
    Object fields[][] = {
        {0, "#idx"}
    };

    java.util.HashMap<Integer, String []> fieldsmap = new java.util.HashMap<Integer, String []>();
    for (final Object fieldslist[] : fields) {
      final String foo[] = new String [fieldslist.length - 1];
      for (int i = 1; i < fieldslist.length; i++) foo[i - 1] = (String) fieldslist[i];
      fieldsmap.put((Integer) fieldslist[0], foo);
    }

    PDUFactory pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
    TableUtils table_utils = new TableUtils(getSNMP(), pdu_factory);
    OID cols[] = new OID[oids.length];
    int cnt = 0;
    for (final int i : oids) cols[cnt++] = new OID(oid + "." + i);

    java.util.List<TableEvent> table = table_utils.getTable(snmp_target, cols, null, null);
    if (table != null)
      for (final TableEvent table_event : table) {
        if (tg.isFinished() == true) return "";
        final VariableBinding columns[] = table_event.getColumns();
        if (columns != null && columns.length >= oids.length) {
          final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[oids.length];
          for (int i = 0; i < oids.length; i++)
            if (columns[i] == null) variable[i] = null;
            else variable[i] = columns[i].getVariable();
          for (int i = 0; i < oids.length; i++)
            if (variable[i] != null && fieldsmap.containsKey(i))
              if (fieldsmap.get(i).length > 0 && fieldsmap.get(i)[0].equals("#idx"))
                vlans.add(columns[i].getOid().getValue()[columns[i].getOid().getValue().length - 1]);
        }
      }

    /***********************************************************/

    for (Integer vlan : vlans) {
      tg.setInfo("vlan " + vlan.toString() + " - click to interrupt");

      final java.util.HashMap<Integer, Integer> port2if = new java.util.HashMap<Integer, Integer>();

      final Target snmp_target;
      if (version == 0 || version == 1) {
        snmp_target = new CommunityTarget();
        snmp_target.setAddress(new UdpAddress(address, port));
        snmp_target.setVersion((version == 0) ? SnmpConstants.version1 : SnmpConstants.version2c);
        ((CommunityTarget) snmp_target).setCommunity(new OctetString(community + "@" + vlan));
      } else {
        try {
          final UsmUserEntry entry = snmp_manager.getSNMP().getUSM()
              .getUserTable().getUser(new OctetString(username));
          if (entry != null
              && snmp_manager.getSNMP().getUSM().removeUser(entry.getEngineID(),
                  entry.getUserName()) == null)
            log.error("USM user not found");
          snmp_manager.getSNMP().getUSM().addUser(
              new OctetString(username),
              new UsmUser(new OctetString(username),
                  sec != SecurityLevel.NOAUTH_NOPRIV ? AuthMD5.ID : null,
                  sec != SecurityLevel.NOAUTH_NOPRIV ? new OctetString(
                      password_auth + "@" + vlan) : null, // pas sûr que Cisco déclare le vlan comme ça en cas de SNMPv3
                  sec == SecurityLevel.AUTH_PRIV ? PrivDES.ID : null,
                  sec == SecurityLevel.AUTH_PRIV ? new OctetString(password_priv + "@" + vlan)
                      : null));
          snmp_target = new UserTarget(new UdpAddress(address, port),
              new OctetString(username), new byte[] {}, sec);
          snmp_target.setVersion(SnmpConstants.version3);
        } catch (final IllegalArgumentException ex) {
          log.error("Exception", ex);
          return "internal error";
        }
      }

      snmp_target.setRetries(retries);
      snmp_target.setTimeout(timeout);
      snmp_target.setMaxSizeRequestPDU(pdu_max_size);

      /***********************************************************/

      oid = "1.3.6.1.2.1.17.1.4.1";
      oids = new int [] {1, 2};

      pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
      table_utils = new TableUtils(getSNMP(), pdu_factory);
      cols = new OID[oids.length];
      cnt = 0;
      for (final int i : oids) cols[cnt++] = new OID(oid + "." + i);

      table = table_utils.getTable(snmp_target, cols, null, null);
      if (table != null)
        for (final TableEvent table_event : table) {
          if (tg.isFinished() == true) return "";
          final VariableBinding columns[] = table_event.getColumns();
          if (columns != null && columns.length >= oids.length) {
            final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[oids.length];
            for (int i = 0; i < oids.length; i++)
              if (columns[i] == null) variable[i] = null;
              else variable[i] = columns[i].getVariable();
            if (variable[0] != null && variable[1] != null)
              port2if.put(variable[0].toInt(), variable[1].toInt());
          }
        }

      /***********************************************************/

      oid = "1.3.6.1.2.1.17.4.3.1";
      oids = new int [] {1, 2, 3};

      pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);
      table_utils = new TableUtils(getSNMP(), pdu_factory);
      cols = new OID[oids.length];
      cnt = 0;
      for (final int i : oids) cols[cnt++] = new OID(oid + "." + i);
      table = table_utils.getTable(snmp_target, cols, null, null);
      if (table != null)
        for (final TableEvent table_event : table) {
          if (tg.isFinished() == true) return "";
          final VariableBinding columns[] = table_event.getColumns();
          if (columns != null && columns.length >= oids.length) {
            final org.snmp4j.smi.Variable variable[] = new org.snmp4j.smi.Variable[oids.length];
            for (int i = 0; i < oids.length; i++)
              if (columns[i] == null) variable[i] = null;
              else variable[i] = columns[i].getVariable();
            if (port2if.containsKey(variable[1].toInt()))
              if (variable[0] != null && variable[1] != null)
                if (if2mac.containsKey(port2if.get(variable[1].toInt())))
                  if2mac.get(port2if.get(variable[1].toInt())).add(variable[0].toString());
                else {
                  final java.util.ArrayList<String> al = new java.util.ArrayList<String>();
                  al.add(variable[0].toString());
                  if2mac.put(port2if.get(variable[1].toInt()), al);
                }
          }
        }
      }

    /***********************************************************/

    result = new StringBuffer("<HR/><B>MAC Address Table for host "
        + getAddress() + "</B><BR/><P/>");
    result.append("<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR bgcolor='lightyellow' align='right'>");
    result.append("<TD><B>" + htmlFace("interface") + "</B></TD> " + "<TD><B>" + htmlFace("MAC address") + "</B></TD>");
    result.append("</TR>");
    for (final int ifnumber : if2mac.keySet())
      for (final String mac : if2mac.get(ifnumber))
        result.append("<TR bgcolor='lightyellow' align='right'><TD>" + htmlFace(getIfDescr(ifnumber)) +
            "</TD><TD>" + htmlFace(mac) + "</TD></TR>");
    result.append("</TABLE>");
    /***********************************************************/

    return result.toString();
  }

  /**
   * Gets the system description OID content asynchronously.
   * 
   * @param listener listener instance that will be called asynchronously.
   * @return void.
   */
  public void getSysDescr(final QuerierListener listener) {
    final PDU pdu = getPDU();
    pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1")));
    pdu.setType(PDU.GETNEXT);
    try {
      getSNMP().send(pdu, snmp_target, null, new ResponseListener() {
        private Boolean invoked = false;

        public void onResponse(final ResponseEvent event) {
          if (event.getResponse() == null) {
            synchronized (invoked) {
              if (invoked == true)
                return;
              invoked = true;
            }
            listener.onTimeout(event);
          } else {
            synchronized (invoked) {
              if (invoked == true)
                return;
              invoked = true;
            }
            ((Snmp) event.getSource()).cancel(event.getRequest(), this);

            snmp_capable = true;
            if (event.getResponse() != null
                && event.getResponse().get(0) != null
                && event.getResponse().get(0).getVariable() != null)
              last_sysdescr = event.getResponse().get(0).getVariable()
                  .toString();

            listener.onResponse(event);
          }
        }
      });
    } catch (final IOException ex) {
      log.error("Exception", ex);
    }
  }

  /**
   * Gets some columns of the interface list table synchronously.
   * 
   * @param none.
   * @return java.util.List<TableEvent> list of rows.
   */
  public java.util.List<TableEvent> getInterfaces() {
    final PDUFactory pdu_factory = new DefaultPDUFactory(PDU.GETNEXT);

    TableUtils table_utils = new TableUtils(getSNMP(), pdu_factory);
    /* regarder IF-MIB::ifName */
    final OID[] cols = new OID[] { new OID("1.3.6.1.2.1.2.2.1.1"), // IF-MIB::ifIndex
        new OID("1.3.6.1.2.1.2.2.1.2"), // IF-MIB::ifDescr
        new OID("1.3.6.1.2.1.2.2.1.3"), // IF-MIB::ifType
        new OID("1.3.6.1.2.1.2.2.1.4"), // IF-MIB::ifMtu
        new OID("1.3.6.1.2.1.2.2.1.5"), // IF-MIB::ifSpeed
        new OID("1.3.6.1.2.1.2.2.1.6"), // IF-MIB::ifPhysAddress
        new OID("1.3.6.1.2.1.2.2.1.7"), // IF-MIB::ifAdminStatus
        new OID("1.3.6.1.2.1.2.2.1.8"), // IF-MIB::ifOperStatus
        new OID("1.3.6.1.2.1.2.2.1.10"), // IF-MIB::ifInOctets
        new OID("1.3.6.1.2.1.2.2.1.16"), // IF-MIB::ifOutOctets
        new OID(".1.3.6.1.2.1.31.1.1.1.18"), // IF-MIB::ifAlias
    };

    java.util.List<TableEvent> table = table_utils.getTable(snmp_target, cols,
        null, null);
    if (table != null && table.isEmpty() == false && table.get(0) != null
        && table.get(0).getColumns() != null)
      snmp_capable = true;
    return table;
  }

  public Object clone() {
    final SNMPQuerier result = new SNMPQuerier();

    result.setId(getId());
    result.setAddress(getAddress());
    result.setVersion(getVersion());
    result.setSec(getSec());
    result.setRetries(getRetries());
    result.setTimeout(getTimeout());
    result.setPort(getPort());
    result.setCommunity(getCommunity());
    result.setPasswordAuth(getPasswordAuth());
    result.setPasswordPriv(getPasswordPriv());
    result.setPDUMaxSize(getPDUMaxSize());
    result.setSNMPCapable(isSNMPCapable());
    result.setLastSysdescr(getLastSysdescr());
    result.setSNMPManager(getSNMPManager());
    result.setSNMPTarget(getSNMPTarget());

    return result;
  }
}
