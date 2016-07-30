
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
 * Events of type EventsBytesExchanged store the amount of data exchanged
 * during the period between the last event and this one.
 * @author Alexandre Fenyo
 * @version $Id: EventBytesExchanged.java,v 1.13 2008/04/26 22:38:34 fenyo Exp $
 */

public class EventBytesExchanged extends EventGeneric {
  private static Log log = LogFactory.getLog(EventBytesExchanged.class);

  // persistent - not null
  private long bytes_exchanged;

  /**
   * Constructor.
   * @param bytes_exchanged bytes exchanged during the period between the last event and this one.
   */
  // Queue thread
  public EventBytesExchanged(final long bytes_exchanged) {
    this.bytes_exchanged = bytes_exchanged;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventBytesExchanged() {
    bytes_exchanged = -1;
  }

  /**
   * Returns the throughput in bit/s at the moment of this event.
   * @return int throughput.
   */
  public int getIntValue() {
    return (int) getBytesExchanged();
  }

  public void setIntValue(int value) {
    setBytesExchanged(value);
  }

  /**
   * Returns the numeric value stored with this event.
   * @param none.
   * @return long numeric value.
   */
  // Queue & AWT thread
  public long getBytesExchanged() {
    return bytes_exchanged;
  }

  public void setBytesExchanged(final long bytes_exchanged) {
    this.bytes_exchanged = bytes_exchanged;
  }
}
