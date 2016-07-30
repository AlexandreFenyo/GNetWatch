
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
package net.fenyo.gnetwatch.GUI;

import java.io.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;

import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a component that can display EventHTTP events.
 * @author Alexandre Fenyo
 * @version $Id: HTTPComponent.java,v 1.3 2008/04/20 14:40:37 fenyo Exp $
 */

public class HTTPComponent extends BasicComponent {
  private static Log log = LogFactory.getLog(HTTPComponent.class);

  /**
   * Constructor.
   * @param target target this graphic component works on.
   */
  // GUI thread
  public HTTPComponent(final Target target) {
    super(target);
  }

  /**
   * Called when the window is closing.
   * @param e event.
   * @return void.
   */
  // AWT thread
  public void windowClosing(final WindowEvent e) {
    getTarget().unregisterComponent(this, EventHTTP.class);
  }

  /**
   * Fetches events that can be displayed.
   * @param none.
   * @return void.
   */
  // AWT thread
  // AWT thread << sync_value_per_vinterval << sync_update << registered_components
/*
  protected void updateValues() {
    // getSyncUpdate() peut-être plus nécessaire ?
    synchronized (getSyncUpdate()) {
      getTarget().registerComponent(this, EventHTTP.class);
      final long end = System.currentTimeMillis();
      final long begin = end - getDelayPerInterval() *
      (getDimension().width - axis_margin_left - axis_margin_right) / pixels_per_interval;
      setEvents(getTarget().getEvents(new Date(begin), new Date(end), EventHTTP.class));
      updateVerticalScale();
    }
  }
*/

  protected Class getEventClass() {
    return EventHTTP.class;
  }
}
