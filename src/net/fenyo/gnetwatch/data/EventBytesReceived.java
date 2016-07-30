
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
 * Events of type EventsBytesReceived store the amount of data received
 * during the period between the last event and this one.
 * @author Alexandre Fenyo
 * @version $Id: EventBytesReceived.java,v 1.8 2008/04/15 23:58:18 fenyo Exp $
 */

public class EventBytesReceived extends EventBytesExchanged {
  private static Log log = LogFactory.getLog(EventBytesReceived.class);

  /**
   * Constructor.
   * @param bytes_received bytes received during the period between the last event and this one.
   */
  // Queue thread
  public EventBytesReceived(final long bytes_received) {
    super(bytes_received);
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventBytesReceived() {}

  /**
   * Returns the numeric value stored with this event.
   * @param none.
   * @return long numeric value.
   */
  // Queue & AWT thread
  public long getBytesReceived() {
    return getBytesReceived();
  }
}
