
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
 * This event stores the output of nmap.
 * @author Alexandre Fenyo
 * @version $Id: EventNmap.java,v 1.4 2008/04/15 23:58:18 fenyo Exp $
 */

public class EventNmap extends EventGeneric {
  private static Log log = LogFactory.getLog(EventNmap.class);

  // persistent - not null
  private String output;

  /**
   * Constructor.
   * @param output output of Nmap process.
   */
  // Queue thread
  public EventNmap(final String output) {
    this.output = output;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventNmap() {
    output = null;
  }

  /**
   * Returns the stored output.
   * @param none.
   * @return String stored output.
   */
  // Queue & AWT thread
  public String getOutput() {
    return output;
  }

  public void setOutput(final String output) {
    this.output = output;
  }
}
