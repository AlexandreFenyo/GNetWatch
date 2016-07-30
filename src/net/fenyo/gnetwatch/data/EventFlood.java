
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
 * Events of type EventFlood store the amount of data flooded to a target
 * during the period between the last event and this one.
 * @author Alexandre Fenyo
 * @version $Id: EventFlood.java,v 1.12 2008/04/26 22:38:34 fenyo Exp $
 */

public class EventFlood extends EventGeneric {
  private static Log log = LogFactory.getLog(EventFlood.class);

  // persistent - not null
  private long bytes_sent;

  private long cache_operand_1 = 0;
  private long cache_operand_2 = 0;
  private int cache_result = 0;

  /**
   * Constructor.
   * @param bytes_sent bytes flooded during the period between the last event and this one.
   */
  // Queue thread
  public EventFlood(final long bytes_sent) {
    this.bytes_sent = bytes_sent;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventFlood() {
    bytes_sent = -1;
  }

  /**
   * Returns the throughput in bit/s at the moment of this event.
   * @return int throughput.
   */
  public int getIntValue() {
    return (int) getBytesSent();
  }

  public void setIntValue(int value) {
    setBytesSent(value);
  }

  /**
   * Returns the numeric value stored with this event.
   * @param none.
   * @return long numeric value.
   */
  // Queue & AWT thread
  public long getBytesSent() {
    return bytes_sent;
  }

  public void setBytesSent(final long bytes_sent) {
    this.bytes_sent = bytes_sent;
  }
}
