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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.Date;

import net.fenyo.gnetwatch.data.EventBytesReceived;
import net.fenyo.gnetwatch.data.EventGeneric;
import net.fenyo.gnetwatch.targets.Target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements an AWT component capable of drawing time series with the Java2D API.
 * @author Alexandre Fenyo
 * @version $Id: BasicComponent.java,v 1.39 2008/08/06 16:51:42 fenyo Exp $
 */

public abstract class BasicComponent extends Component
  implements ComponentListener, WindowListener, KeyListener,
             MouseMotionListener, MouseListener {
  private static Log log = LogFactory.getLog(BasicComponent.class);

  private boolean manual_mode = false;
  private long manual_now = 0;
  private long manual_delay_per_interval = 0;
  private int lastPixelsOffsetValue = 0;

  final private Target target;

  private static Object sync_update = new Object();

  protected java.util.List<EventGeneric> events = null;

  private Image backing_store = null;
  protected Graphics2D backing_g = null;
  protected Dimension dimension = null;

  private final DateFormat date_format = DateFormat.getDateTimeInstance();

  private int fps = 0;
  private long last_paint = System.currentTimeMillis();
  private long last_paint_100ms = System.currentTimeMillis();
  private int last_fps_100ms = 0;

  // horizontal interval
  protected final int pixels_per_interval = 80; // 80 pix/interval ; multiple de 10

  // vertical intervals
  protected int value_per_vinterval = 200;
  private final Object sync_value_per_vinterval = new Object();
  protected final int pixels_per_vinterval = 80; // 80 pix/vinterval ; multiple de 10

  private final static int std_margin = 30;
  private final static int std_separator = 3;

  protected final static int axis_margin_bottom = std_margin;
  protected final static int axis_margin_top = std_margin;
  protected int axis_margin_left = std_margin;
  protected final static int axis_margin_right = std_margin;

  private int drag_x_start = 0;
  private long drag_now_start = 0;

  /**
   * Constructor.
   * @param target target this graphic component works on.
   */
  // GUI thread
  public BasicComponent(final Target target) {
    this.target = target;
  }

  public boolean isManualMode() {
    return manual_mode;
  }

  /**
   * Returns the horizontal scale.
   * @param none.
   * @return long horizontal scale.
   */
  public long getDelayPerInterval() {
    return 5000; // 5 secs/interval
  }

  protected long _getDelayPerInterval() {
    if (!manual_mode) return getDelayPerInterval();
    else return manual_delay_per_interval;
  }

  /**
   * Sets the list of events to display.
   * @param events events to display.
   * @return void.
   */
  protected void setEvents(final java.util.List<EventGeneric> events) {
    this.events = events;
  }

  /**
   * Returns the dimensions of this component.
   * @param none.
   * @return Dimension dimensions.
   */
  protected Dimension getDimension() {
    return dimension;
  }

  /**
   * Returns the associated target.
   * @param none.
   * @return Target target.
   */
  protected Target getTarget() {
    return target;
  }

  /**
   * Initialize the component and ask AWT to receive events.
   *
   */
  // GUI thread
  // lock survey: synchro << sync_tree << HERE
  public void init() {
    setPreferredSize(new Dimension(700, 400));
    addComponentListener(this);
    setFocusable(true);
    addKeyListener(this);
    addMouseMotionListener(this);
    addMouseListener(this);
  }

  /**
   * Called when the component is hidden.
   * @param e event.
   * @return void.
   */
  public void componentHidden(final ComponentEvent e) {
  }

  /**
   * Called when the component is moved.
   * @param e event.
   * @return void.
   */
  public void componentMoved(final ComponentEvent e) {
  }

  /**
   * When the component is resized, creates a new backing store,
   * reset margins and fetch events that can be displayed.
   * @param e event.
   * @return void.
   */
  // AWT thread
  // lock survey: THIS
  public void componentResized(final ComponentEvent e) {
    synchronized (sync_value_per_vinterval) {
      newBackingElts();
      setMargin();
    }
    updateValues();
  }

  /**
   * Called when the component appears first.
   * @param e event.
   * @return void.
   */
  // AWT thread
  public void componentShown(final ComponentEvent e) {
    componentResized(e);
  }

  /**
   * Called whenthe window is activated.
   * @param e event.
   * @return void.
   */
  public void windowActivated(final WindowEvent e) {
  }

  /**
   * Called whenthe window is closed.
   * @param e event.
   * @return void.
   */
  public void windowClosed(final WindowEvent e) {
  }

  /**
   * Called when the window is closing.
   * @param e event.
   * @return void.
   */
  // AWT thread
  public abstract void windowClosing(final WindowEvent e);

  /**
   * Called whenthe window is deactivated.
   * @param e event.
   * @return void.
   */
  public void windowDeactivated(final WindowEvent e) {
  }

  /**
   * Called whenthe window is deiconified.
   * @param e event.
   * @return void.
   */
  public void windowDeiconified(final WindowEvent e) {
  }

  /**
   * Called whenthe window is iconified.
   * @param e event.
   * @return void.
   */
  public void windowIconified(final WindowEvent e) {
  }

  /**
   * Called whenthe window is opened.
   * @param e event.
   * @return void.
   */
  public void windowOpened(final WindowEvent e) {
  }

  /**
   * Computes new margins.
   * @param none.
   * @return void.
   */
  // AWT thread
  // lock survey: sync_update << sync_value_per_vinterval << HERE
  //              synchro << sync_tree << sync_update << sync_value_per_vinterval << events << HERE
  //              sync_value_per_vinterval << HERE
  private void setMargin() {
    /*
    final String left_values_str = "" + (int) (value_per_vinterval *
        (1 + (dimension.height - axis_margin_top - axis_margin_bottom) / pixels_per_vinterval));
*/
    final String left_values_str = "1000M";
    final TextLayout layout = new TextLayout(left_values_str, backing_g.getFont(), backing_g.getFontRenderContext());
    final Rectangle2D bounds = layout.getBounds();
    axis_margin_left = (int) bounds.getWidth() + 5 + 10;
  }

  /**
   * Creates a backing store.
   * @param none.
   * @return void.
   */
  private void newBackingElts() {
    dimension = getSize();
    backing_store = createImage(dimension.width, dimension.height);
    backing_g = (Graphics2D) backing_store.getGraphics();
    backing_g.setBackground(Color.BLACK);
    backing_g.setColor(Color.WHITE);
    backing_g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    backing_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
  }

  /**
   * Removes events that can not be displayed.
   * @param none.
   * @return void.
   *
   */
  // lock survey: synchro << sync_tree << sync_update << sync_value_per_vinterval << events << HERE  
  private void removeOldEvents() {
    final Date begin = new Date(System.currentTimeMillis() - _getDelayPerInterval() *
        (dimension.width - axis_margin_left - axis_margin_right) / pixels_per_interval);
        while (events.size() >= 2 && (events.get(1).getDate().before(begin) || events.get(1).getDate().equals(begin)))
          events.remove(0);
  }

  protected int getIntValue(final EventGeneric event) {
    return event.getIntValue();
  }

  /**
   * Updates the vertical scale.
   * @param none.
   * @return void.
   */
  // Queue & AWT thread
  // lock survey: sync_update << sync_value_per_vinterval << HERE
  // lock survey: synchro << sync_tree << sync_update << sync_value_per_vinterval << events << HERE  
  protected void updateVerticalScale() {
    if (manual_mode) return;

    final int previous_value_per_vinterval = value_per_vinterval;

    int max_value = 0;
    for (final EventGeneric event : events) {
      final int value = getIntValue(event);
      if (value > max_value) max_value = value;
    }

    int nintervals = (dimension.height - axis_margin_top - axis_margin_bottom) / pixels_per_vinterval - 1;
    if (nintervals < 1) nintervals = 1;
    value_per_vinterval = max_value / nintervals;

    // possible values for value_per_vinterval: 1, 2, 5, 10, 20, 25, 50, 100, 200, etc.
    final int base = (int) Math.pow(10, new Integer(value_per_vinterval).toString().length() - 1);
    if (value_per_vinterval != base)
      if (value_per_vinterval <= 2 * base) value_per_vinterval = 2 * base;
      else if (base >= 10 && value_per_vinterval <= 25 * base / 10) value_per_vinterval = 25 * base / 10;
      else if (value_per_vinterval <= 5 * base) value_per_vinterval = 5 * base;
      else value_per_vinterval = 10 * base;

    if (value_per_vinterval == 0) value_per_vinterval = 1;

    if (previous_value_per_vinterval != value_per_vinterval) setMargin();
  }

  /**
   * Takes a new event into account.
   * @param event new event.
   * @return void.
   */
  // Queue thread
  // lock survey: synchro << sync_tree << HERE  
  public void updateValues(final EventGeneric event) {
    synchronized (sync_update) { // hypothèse pour autres pbs synchronized : sync_value_per_vinterval crée un pb de verrou
    synchronized (sync_value_per_vinterval) {
        synchronized (events) {
          // lock survey: synchro << sync_tree << sync_update << sync_value_per_vinterval << events << HERE --XX  
          if (manual_mode && events.size() > 0) {
            if (event.getDate().after(new Date(manual_now)) && events.get(events.size() - 1).getDate().after(new Date(manual_now))) return;

            final long begin = manual_now - _getDelayPerInterval() *
            (getDimension().width - axis_margin_left - axis_margin_right) / pixels_per_interval;
            if (event.getDate().before(new Date(begin))) return;
          }

          for (int i = events.size() - 1; i >= 0; i--)
            if (events.get(i).getDate().before(event.getDate())) {
              if (i + 1 < events.size()) events.add(i + 1, event);
              else events.add(event);
              if (!manual_mode) removeOldEvents();
              updateVerticalScale();
              return;
            }

          events.add(0, event);
          if (!manual_mode) removeOldEvents();
          updateVerticalScale();
        }
      }
    }
  }

  protected abstract Class getEventClass();

  /**
   * Fetches events that can be displayed.
   * @param none.
   * @return void.
   */
  // AWT thread
  // lock survey: HERE
  protected void updateValues() {
    synchronized (getTarget().getGUI().getSynchro()) {
      synchronized (getTarget().getGUI().sync_tree) {
        synchronized (sync_update) {
          synchronized (sync_value_per_vinterval) {
            getTarget().registerComponent(this, getEventClass());
            final long end;
            if (!manual_mode) end = System.currentTimeMillis();
            else end = manual_now;
            final long begin = end - _getDelayPerInterval() *
            (getDimension().width - axis_margin_left - axis_margin_right) / pixels_per_interval;
            setEvents(getTarget().getEvents(new Date(begin), new Date(end), getEventClass()));
          }
          updateVerticalScale();
          repaint();
        }
      }
    }
  }

  /**
   * Computes the "frames per second" indicator.
   * @param none.
   * @return void.
   */
  // AWT thread
  private void updateFPS() {
    final long current_time = System.currentTimeMillis();
    fps = 100 / (int) (current_time - last_paint + 1) + (9 * fps) / 10;
    last_paint = current_time;
  }

  /**
   * Displays the number of frames per second.
   * @param fps frames per second to display.
   * @return void.
   */
  // AWT thread
  private void paintFPS(final int fps) {
    backing_g.setColor(Color.GRAY);
    if (!manual_mode) {
      // à traduire
      backing_g.drawString(fps + " frames/s - press any key or mouse button to enter manual scaling mode", 1, 13);
      backing_g.setColor(Color.YELLOW);
      backing_g.drawString("AUTO", 60, 50);
    } else {
      // à traduire
      backing_g.drawString("keys: '<' horiz. scale down  '>' horiz. scale up  '-' vert. scale down  '+' vert. scale up  'n' current date  'a' automatic scaling mode", 1, 13);
      backing_g.setColor(Color.YELLOW);
      backing_g.drawString("MANUAL", 60, 50);
    }
  }

  /**
   * Formats a time string to be displayed.
   * @param time time.
   * @return String time to be displayed.
   */
  // AWT thread
  private String formatTime(final long time) {
    String str = date_format.format(new Date(time));
    return str.substring(str.lastIndexOf(' ') + 1);
  }

  private String formatDate(final long time) {
    String str = date_format.format(new Date(time));
    return str.substring(0, str.lastIndexOf(' '));
  }

  public boolean pixelsOffsetChanged() {
    final int new_val = (pixels_per_interval * (int) (System.currentTimeMillis() % _getDelayPerInterval())) / (int) _getDelayPerInterval();
    if (lastPixelsOffsetValue == new_val) return false;
    lastPixelsOffsetValue = new_val;
    return true;
  }

  /**
   * Paints axis.
   * @param none.
   * @return long current time displayed at the axis bottom.
   */
  // AWT thread
  private long paintAxis() {
    backing_g.setColor(new Color(50, 50, 50));
    backing_g.fillRect(axis_margin_left, axis_margin_top,
        dimension.width - axis_margin_left - axis_margin_right + 1,
        dimension.height - axis_margin_top - axis_margin_bottom + 1);

    backing_g.setColor(Color.YELLOW);
    backing_g.drawLine(axis_margin_left, dimension.height - axis_margin_bottom,
        dimension.width - axis_margin_right, dimension.height - axis_margin_bottom);
    backing_g.drawLine(axis_margin_left, axis_margin_top,
        axis_margin_left, dimension.height - axis_margin_bottom);

    backing_g.setColor(Color.YELLOW.darker());
    backing_g.drawLine(axis_margin_left + 1, axis_margin_top,
        dimension.width - axis_margin_right,
        axis_margin_top);
    backing_g.drawLine(dimension.width - axis_margin_right,
        axis_margin_top,
        dimension.width - axis_margin_right,
        dimension.height - axis_margin_bottom - 1);

    int vinterval_pos = dimension.height - axis_margin_bottom - pixels_per_vinterval;
    backing_g.setColor(Color.YELLOW.darker().darker().darker());
    while (vinterval_pos + 9 * (pixels_per_vinterval / 10) > axis_margin_top) {
      int cpt = 10;
      while (--cpt > 0)
        if (vinterval_pos + cpt * (pixels_per_vinterval / 10) > axis_margin_top)
          backing_g.drawLine(axis_margin_left + 1, vinterval_pos + cpt * (pixels_per_vinterval / 10),
              dimension.width - axis_margin_right - 1, vinterval_pos + cpt * (pixels_per_vinterval / 10));
      vinterval_pos -= pixels_per_vinterval;
    }

    final long now;
    if (manual_mode) now = manual_now;
    else now = System.currentTimeMillis();

    final long time_to_display = now - now % _getDelayPerInterval();
    final int pixels_offset = (pixels_per_interval * (int) (now % _getDelayPerInterval())) / (int) _getDelayPerInterval();
    final int last_interval_pos = dimension.width - axis_margin_right - pixels_offset;

    backing_g.setClip(axis_margin_left, 0,
        dimension.width - axis_margin_left - axis_margin_right,
        dimension.height);
    int current_interval_pos = last_interval_pos + pixels_per_interval;
    long current_time_to_display = time_to_display + _getDelayPerInterval();
    boolean stop = false;
    while (stop == false) {
      backing_g.setColor(Color.YELLOW.darker());
      backing_g.drawLine(current_interval_pos, axis_margin_top,
          current_interval_pos, dimension.height - axis_margin_bottom + std_separator);

      int cpt = 10;
      backing_g.setColor(Color.YELLOW.darker().darker().darker());
      while (--cpt > 0)
        if (current_interval_pos - cpt * (pixels_per_interval / 10) > axis_margin_left)
          backing_g.drawLine(current_interval_pos - cpt * (pixels_per_interval / 10),
              axis_margin_top + 1,
              current_interval_pos - cpt * (pixels_per_interval / 10),
              dimension.height - axis_margin_bottom - 1);

      final String current_time_str = formatTime(current_time_to_display);
      final String current_date_str = formatDate(current_time_to_display);
      final TextLayout current_layout = new TextLayout(current_time_str, backing_g.getFont(), backing_g.getFontRenderContext());
      final TextLayout current_layout_date = new TextLayout(current_date_str, backing_g.getFont(), backing_g.getFontRenderContext());
      final Rectangle2D current_bounds = current_layout.getBounds();
      final Rectangle2D current_bounds_date = current_layout_date.getBounds();
      backing_g.setColor(Color.YELLOW.darker());
      backing_g.drawString(current_time_str,
          current_interval_pos - (int) (current_bounds.getWidth() / 2),
          dimension.height - axis_margin_bottom + (int) current_bounds.getHeight() + 2 * std_separator);
      backing_g.setColor(Color.YELLOW.darker().darker());
      backing_g.drawString(current_date_str,
          current_interval_pos - (int) (current_bounds_date.getWidth() / 2),
          3 + ((int) current_bounds.getHeight()) + dimension.height - axis_margin_bottom + (int) current_bounds.getHeight() + 2 * std_separator);
      if (current_interval_pos - current_bounds.getWidth() / 2 < axis_margin_left)
        stop = true;
      current_interval_pos -= pixels_per_interval;
      current_time_to_display -= _getDelayPerInterval();
    }
    backing_g.setClip(null);

    vinterval_pos = dimension.height - axis_margin_bottom - pixels_per_vinterval;
    int value = value_per_vinterval;
    while (vinterval_pos > axis_margin_top) {
      backing_g.setColor(Color.YELLOW.darker());
      backing_g.drawLine(axis_margin_left - std_separator, vinterval_pos,
          dimension.width - axis_margin_right, vinterval_pos);
      final String value_str;
      if (value >= 1000000) value_str = "" + value / 1000000 + "M";
      else if (value >= 1000) value_str = "" + value / 1000 + "k";
      else value_str = "" + value;
      final TextLayout current_layout = new TextLayout(value_str, backing_g.getFont(), backing_g.getFontRenderContext());
      final Rectangle2D current_bounds = current_layout.getBounds();
      backing_g.setColor(Color.YELLOW.darker());
      backing_g.drawString(value_str,
          axis_margin_left - (int) current_bounds.getWidth() - 2 * std_separator,
          vinterval_pos + (int) (current_bounds.getHeight() / 2));
      vinterval_pos -= pixels_per_vinterval;
      value += value_per_vinterval;
    }

    return now;
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

      final long time_to_display = now - now % _getDelayPerInterval();
      final int pixels_offset = (pixels_per_interval * (int) (now % _getDelayPerInterval())) / (int) _getDelayPerInterval();
      final int last_interval_pos = dimension.width - axis_margin_right - pixels_offset;

      for (int i = 0; i < events.size(); i++) {
        final EventGeneric event = events.get(i);
        final long xpos = ((long) last_interval_pos) + (((long) pixels_per_interval) *
            (event.getDate().getTime() - time_to_display)) / _getDelayPerInterval();
        if (xpos < -1000)
          point_x[i] = -1000;
        else if (xpos > 1000 + dimension.width)
          point_x[i] = 1000 + dimension.width;
        else point_x[i] = (int) xpos;

        // cast to double to avoid overflow on int that lead to wrong results
        point_y[i] = (int) (dimension.height - axis_margin_bottom -
          pixels_per_vinterval * (double) getIntValue(event) / value_per_vinterval);
//        if (point_y[i] < 0) log.warn("y < 0: y=" + point_y[i]);
      }

      backing_g.setColor(Color.GREEN);
      backing_g.drawPolyline(point_x, point_y, events.size());

      backing_g.setColor(Color.RED);
      for (int i = 0; i < events.size(); i++)
        backing_g.drawRect(point_x[i] - 2, point_y[i] - 2, 4, 4);

      backing_g.setClip(null);
    }
  }

  /**
   * Repaints the component using the backing store.
   * @param g graphics context.
   * @return void.
   */
  // AWT thread
  public void paint(final Graphics g) {
    if (backing_store == null) return;
    backing_g.clearRect(0, 0, dimension.width, dimension.height);

    updateFPS();

    final long current_time = System.currentTimeMillis();
    if (current_time - last_paint_100ms >= 100) {
      last_paint_100ms = current_time;
      last_fps_100ms = fps;
    }

    synchronized (sync_value_per_vinterval) {
      final long now = paintAxis();
      paintChart(now);
    }

    paintFPS(last_fps_100ms);

    g.drawImage(backing_store, 0, 0, this);
  }

  public void keyPressed(final KeyEvent e) {}

  public void keyReleased(final KeyEvent e) {}

  public void keyTyped(final KeyEvent e) {
    if (!manual_mode) {
      // automatic mode
      manual_now = System.currentTimeMillis();
      manual_delay_per_interval = getDelayPerInterval();
      manual_mode = true;

    } else {
      // manual mode

      if (e.getKeyChar() == 'a') {
        manual_mode = false;
        updateValues();
      }

      if (e.getKeyChar() == 'n') {
        manual_now = System.currentTimeMillis();
        updateValues();
      }
      
      if (e.getKeyChar() == '<') {
        manual_delay_per_interval = manual_delay_per_interval * 2;
        updateValues();
      }
      
      if (e.getKeyChar() == '>') {
        manual_delay_per_interval = manual_delay_per_interval / 2;
        updateValues();
      }

      if (e.getKeyChar() == '+') {
        synchronized (sync_value_per_vinterval) {
          value_per_vinterval = value_per_vinterval / 2;
        }
        repaint();
      }

      if (e.getKeyChar() == '-') {
        synchronized (sync_value_per_vinterval) {
          value_per_vinterval = value_per_vinterval * 2;
        }
        repaint();
      }
    }
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    if (!manual_mode) {
      manual_now = System.currentTimeMillis();
      manual_delay_per_interval = getDelayPerInterval();
      manual_mode = true;
    }

    drag_x_start = e.getX();
    drag_now_start = manual_now;
  }

  public void mouseReleased(MouseEvent e) {
    updateValues();
  }

  public void mouseDragged(final MouseEvent e) {
    if (manual_mode) {
      manual_now = drag_now_start +
      manual_delay_per_interval * (drag_x_start - e.getX()) / pixels_per_interval;
      }
    repaint();
  }

  public void mouseMoved(final MouseEvent e) {}
}
