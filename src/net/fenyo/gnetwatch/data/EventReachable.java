
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
 * This event stores the last RTT computed.
 * @author Alexandre Fenyo
 * @version $Id: EventReachable.java,v 1.14 2008/04/26 22:38:34 fenyo Exp $
 */

public class EventReachable extends EventGeneric {
  private static Log log = LogFactory.getLog(EventReachable.class);

  // persistent - not null
  private boolean reachable;

  // persistent - not null
  private int delay;

  /**
   * Constructor.
   * @param reachable true if RTT < +inft
   * @param delay RTT if reachable is true
   */
  // Queue thread
  public EventReachable(final boolean reachable, final int delay) {
    this.reachable = reachable;
    this.delay = delay;
  }

  /**
   * Constructor.
   * @param reachable true if RTT < +inft
   */
  // Queue thread
  public EventReachable(final boolean reachable) {
    this.reachable = reachable;
    this.delay = -1;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventReachable() {
    this.reachable = false;
    this.delay = -1;
  }

  /**
   * Returns an integer representation of the performance counter associated whith this event.
   * @param events every event.
   * @return int performance counter.
   */
  public int getIntValue() {
    return getDelay();
  }

  public void setIntValue(int value) {
    setDelay(value);
    setReachable(value < 0 ? false : true);
  }

  /**
   * Returns the RTT associated to this event.
   * @param none.
   * @return int RTT.
   */
  // Queue & AWT thread
  public int getDelay() {
    return delay;
  }

  public void setDelay(final int delay) {
    this.delay = delay;
  }

  public boolean isReachable() {
    return reachable;
  }

  public void setReachable(final boolean reachable) {
    this.reachable = reachable;
  }
}
