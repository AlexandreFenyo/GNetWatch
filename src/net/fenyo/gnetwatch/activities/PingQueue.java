
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

package net.fenyo.gnetwatch.activities;

import java.util.LinkedList;
import java.util.List;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.Action;
import net.fenyo.gnetwatch.targets.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This queue holds ICMP actions.
 * @author Alexandre Fenyo
 * @version $Id: PingQueue.java,v 1.14 2008/04/15 23:58:17 fenyo Exp $
 */

public class PingQueue extends Queue implements Runnable {
  private static Log log = LogFactory.getLog(PingQueue.class);

  /**
   * Constructor.
   * @param name queue name.
   * @param config configuration.
   */
  // main thread
  public PingQueue(final String name, final Config config) {
    super(name, config);
    setDescription(config.getString("dedicated_for_reachability_testing"));
  }

  /**
   * Returns the time to wait after each cycle.
   * @param none.
   * @return int time to wait.
   */
  // Queue thread
  protected int getCycleDelay() {
    if (getConfig().getDebugLevel() == 1) return 0;
    return 1000;
  }

  /**
   * Returns the time to wait between empty cycles.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  protected int getEmptyCycleDelay() {
    return 1000;
  }

  /**
   * Returns the time to wait between two actions.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  protected int getActionDelay() {
    return 0;
  }
}
