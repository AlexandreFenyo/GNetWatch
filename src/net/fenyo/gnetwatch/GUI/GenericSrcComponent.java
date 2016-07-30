
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
 * This class implements a component that can display EventGenericSrc events.
 * @author Alexandre Fenyo
 * @version $Id: GenericSrcComponent.java,v 1.5 2008/05/29 15:28:46 fenyo Exp $
 */

public class GenericSrcComponent extends BasicComponent {
  private static Log log = LogFactory.getLog(GenericSrcComponent.class);

  /**
   * Constructor.
   * @param target target this graphic component works on.
   */
  // GUI thread
  public GenericSrcComponent(final Target target) {
    super(target);
  }

  /**
   * Called when the window is closing.
   * @param e event.
   * @return void.
   */
  // AWT thread
  public void windowClosing(final WindowEvent e) {
    getTarget().unregisterComponent(this, EventGenericSrc.class);
  }

  protected Class getEventClass() {
    return EventGenericSrc.class;
  }
  protected boolean isMultipleValueEvent(final EventGenericSrc event) {
    return event.isReachable() && event.getDelay() < 0 &&
    (event.getValue1() > 0 || event.getValue2() > 0 || event.getValue3() > 0 ||
        event.getValue4() > 0 || event.getValue5() > 0 || event.getValue6() > 0 ||
        event.getValue7() > 0 || event.getValue8() > 0 || event.getValue9() > 0 ||
        event.getValue10() > 0);
  }

  protected int getIntValue(final EventGeneric event) {
    final EventGenericSrc src_evt = (EventGenericSrc) event;
    if (!isMultipleValueEvent(src_evt)) return event.getIntValue();
    else return Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(src_evt.getValue1(),
        src_evt.getValue2()), src_evt.getValue3()), src_evt.getValue4()), src_evt.getValue5()), src_evt.getValue6()),
        src_evt.getValue7()), src_evt.getValue8()), src_evt.getValue9()), src_evt.getValue10());
  }

  /**
   * Returns the horizontal scale.
   * @param none.
   * @return long horizontal scale.
   */
  public long getDelayPerInterval() {
    return 60000;
  }

  /**
   * Paints the chart.
   * @param now current time.
   * @return void.
   */
  // AWT thread
  // AWT thread << sync_value_per_vinterval << events
  public void paintChart(final long now) {
    backing_g.setClip(axis_margin_left + 1, axis_margin_top,
        dimension.width - axis_margin_left - axis_margin_right - 1,
        dimension.height - axis_margin_top - axis_margin_bottom);

    synchronized (events) {
      final int npoints = events.size();
      final int point_x[] = new int [npoints];
      final int point_y[] = new int [npoints];
      final int point_y1[] = new int [npoints];
      final int point_y2[] = new int [npoints];
      final int point_y3[] = new int [npoints];
      final int point_y4[] = new int [npoints];
      final int point_y5[] = new int [npoints];
      final int point_y6[] = new int [npoints];
      final int point_y7[] = new int [npoints];
      final int point_y8[] = new int [npoints];
      final int point_y9[] = new int [npoints];
      final int point_y10[] = new int [npoints];

      final long time_to_display = now - now % _getDelayPerInterval();
      final int pixels_offset = (pixels_per_interval * (int) (now % _getDelayPerInterval())) / (int) _getDelayPerInterval();
      final int last_interval_pos = dimension.width - axis_margin_right - pixels_offset;

      for (int i = 0; i < events.size(); i++) {
        final EventGenericSrc event = (EventGenericSrc) events.get(i);
        final long xpos = ((long) last_interval_pos) + (((long) pixels_per_interval) *
            (event.getDate().getTime() - time_to_display)) / _getDelayPerInterval();
        if (xpos < -1000)
          point_x[i] = -1000;
        else if (xpos > 1000 + dimension.width)
          point_x[i] = 1000 + dimension.width;
        else point_x[i] = (int) xpos;

        // cast to double to avoid overflow on int that lead to wrong results
        point_y[i] = (int) (dimension.height - axis_margin_bottom -
          pixels_per_vinterval * (double) event.getIntValue() / value_per_vinterval);
        point_y1[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue1() / value_per_vinterval);
        point_y2[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue2() / value_per_vinterval);
        point_y3[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue3() / value_per_vinterval);
        point_y4[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue4() / value_per_vinterval);
        point_y5[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue5() / value_per_vinterval);
        point_y6[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue6() / value_per_vinterval);
        point_y7[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue7() / value_per_vinterval);
        point_y8[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue8() / value_per_vinterval);
        point_y9[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue9() / value_per_vinterval);
        point_y10[i] = (int) (dimension.height - axis_margin_bottom -
            pixels_per_vinterval * (double) event.getValue10() / value_per_vinterval);
      }

      backing_g.setColor(Color.GREEN);
      backing_g.drawPolyline(point_x, point_y, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y[i] - 2, 4, 4);

      backing_g.setColor(Color.WHITE);
      backing_g.drawPolyline(point_x, point_y1, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y1[i] - 2, 4, 4);

      backing_g.setColor(Color.BLUE);
      backing_g.drawPolyline(point_x, point_y2, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y2[i] - 2, 4, 4);

      backing_g.setColor(Color.GRAY);
      backing_g.drawPolyline(point_x, point_y3, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y3[i] - 2, 4, 4);

      backing_g.setColor(Color.YELLOW);
      backing_g.drawPolyline(point_x, point_y4, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y4[i] - 2, 4, 4);

      backing_g.setColor(Color.ORANGE);
      backing_g.drawPolyline(point_x, point_y5, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y5[i] - 2, 4, 4);

      backing_g.setColor(Color.CYAN);
      backing_g.drawPolyline(point_x, point_y6, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y6[i] - 2, 4, 4);

      backing_g.setColor(Color.MAGENTA);
      backing_g.drawPolyline(point_x, point_y7, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y7[i] - 2, 4, 4);

      backing_g.setColor(Color.LIGHT_GRAY);
      backing_g.drawPolyline(point_x, point_y8, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y8[i] - 2, 4, 4);

      backing_g.setColor(Color.PINK);
      backing_g.drawPolyline(point_x, point_y9, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y9[i] - 2, 4, 4);

      backing_g.setColor(Color.RED);
      backing_g.drawPolyline(point_x, point_y10, events.size());
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y10[i] - 2, 4, 4);

      int cnt = 1;
      int cnt2 = 1;
      if (events.size() > 0)
        for (final String str : ((EventGenericSrc) events.get(0)).getUnits().split(";")) {
          if (str.length() > 0) {
            switch (cnt) {
              case 1:
                backing_g.setColor(Color.WHITE);
                break;
              case 2:
                backing_g.setColor(Color.BLUE);
                break;
              case 3:
                backing_g.setColor(Color.GRAY);
                break;
              case 4:
                backing_g.setColor(Color.YELLOW);
                break;
              case 5:
                backing_g.setColor(Color.ORANGE);
                break;
              case 6:
                backing_g.setColor(Color.CYAN);
                break;
              case 7:
                backing_g.setColor(Color.MAGENTA);
                break;
              case 8:
                backing_g.setColor(Color.LIGHT_GRAY);
                break;
              case 9:
                backing_g.setColor(Color.PINK);
                break;
              case 10:
                backing_g.setColor(Color.RED);
                break;
            }
            backing_g.fillRect(dimension.width - axis_margin_right - 150, 50 - 5 + 13 * cnt2, 20, 3);

            backing_g.setColor(Color.WHITE);
            backing_g.drawString(str, dimension.width - axis_margin_right - 150 + 22, 50 + 13 * cnt2);

            cnt2++;
          }
          cnt++;
        }

      backing_g.setClip(null);
    }
  }
}
