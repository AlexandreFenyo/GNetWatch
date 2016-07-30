
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

package net.fenyo.gnetwatch.data;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.targets.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Events of type EventHTTP store the amount of data received from a HTTP/FTP server
 * during the period between the last event and this one.
 * @author Alexandre Fenyo
 * @version $Id: EventHTTP.java,v 1.6 2008/04/26 22:38:34 fenyo Exp $
 */

public class EventHTTP extends EventGeneric {
  private static Log log = LogFactory.getLog(EventHTTP.class);

  // persistent - not null
  private long bytes_received;

  /**
   * Constructor.
   * @param bytes_received bytes received during the period between the last event and this one.
   */
  // Queue thread
  public EventHTTP(final long bytes_received) {
    this.bytes_received = bytes_received;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventHTTP() {
    bytes_received = -1;
  }

  /**
   * Returns the throughput in bit/s at the moment of this event.
   * @return int throughput.
   */
  public int getIntValue() {
    return (int) getBytesReceived();
  }

  public void setIntValue(int value) {
    setBytesReceived(value);
  }

  /**
   * Returns the numeric value stored with this event.
   * @param none.
   * @return long numeric value.
   */
  // Queue & AWT thread
  public long getBytesReceived() {
    return bytes_received;
  }

  public void setBytesReceived(final long bytes_received) {
    this.bytes_received = bytes_received;
  }
}
