
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
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.targets.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.awt.Component;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.browser.*;

/**
 * DataView is the base class to display time series.
 * @author Alexandre Fenyo
 * @version $Id: DataView.java,v 1.34 2008/11/17 01:39:19 fenyo Exp $
 */

public abstract class DataView extends VisualElement {
  private static Log log = LogFactory.getLog(DataView.class);

  // persistent
  private Target target;

  protected CTabItem tab_item = null;

  private JFrame frame = null;
  private Browser browser = null;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param target ingress target interface.
   */
  // GUI thread
  public DataView(final GUI gui, final Target target) {
    this.target = target;
    setType("view");
    setParent(gui, target);
  }

  public DataView() {
    this.target = null;
  }

  /**
   * Sets the current GUI instance.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageOscillo();
  }

  /**
   * Returns the SWT browser.
   * @param none.
   * @return Browser SWT browser.
   */
  protected Browser getBrowser() {
    return browser;
  }

  /**
   * Returns the target this view works on.
   * @param none.
   * @return Target target this view works on.
   */
  // GUI thread
  public Target getTarget() {
    return target;
  }

  public void setTarget(final Target target) {
    this.target = target;
  }

  /**
   * Returns the title of the view.
   * @param none.
   * @return String title view.
   */
  // GUI thread
  public String getTitle() {
    return "DataView";
  }

  /**
   * Returns the name of report this view can generate.
   * @param none.
   * @return String report name.
   */
  protected abstract String browserName();

  /**
   * Returns the data unit for values this view can generate.
   * @param none.
   * @return String data unit.
   */
  protected abstract String browserUnit();

  /**
   * Returns the event class this view can manage.
   * @param none.
   * @return Class event class.
   */
  public abstract Class browserEventClass();

  /**
   * Embed face informations in an HTML part. 
   * @param html source part.
   * @return String embedded html part.
   */
  private String htmlFace(final String html) {
    return getGUI().htmlFace(html);
  }

  /**
   * Generates an HTML report.
   * @param none.
   * @return StringBuffer HTML report.
   */
  // lock survey: synchro << sync_tree << HERE
  protected StringBuffer getBrowserContent() {
    final StringBuffer content = new StringBuffer();

    content.append("<B>" + getGUI().getConfig().getPattern("report_browser", browserName()) + "</B><HR/>");
    content.append(getGUI().getConfig().getPattern("target_name", getTarget().getName()) + "<BR/>");
    content.append(getGUI().getConfig().getPattern("target_type", getTarget().getClass().toString()) + "<BR/>");

    if (TargetIPv4.class.isInstance(getTarget())) {
      final TargetIPv4 _target = (TargetIPv4) getTarget();
      content.append(getGUI().getConfig().getPattern("address_name", _target.getAddress().toString().substring(1)) + "<BR/>");
    }

    if (TargetIPv6.class.isInstance(getTarget())) {
      final TargetIPv6 _target = (TargetIPv6) getTarget();
      content.append(getGUI().getConfig().getPattern("address_name", _target.getAddress().toString().substring(1)) + "<BR/>");
    }

    Date begin, end;

    content.append("<HR/><B>" + getGUI().getConfig().getString("every_event") + "</B><BR/>");
    // should use <? extends EventGeneric>
    end = new Date();
    begin = new Date(0);
    content.append(analyzeEvents(getTarget().getEvents(begin, end, browserEventClass()), begin, end));

    content.append("<HR/><B>" + getGUI().getConfig().getString("last_30_sec") + "</B><BR/>");
    end = new Date();
    begin = new Date(end.getTime() - 30000);
    content.append(analyzeEvents(getTarget().getEvents(begin, end, browserEventClass()), begin, end));

    content.append("<HR/><B>" + getGUI().getConfig().getString("last_5_min") + "</B><BR/>");
    end = new Date();
    begin = new Date(new Date().getTime() - 300000);
    content.append(analyzeEvents(getTarget().getEvents(begin, end, browserEventClass()), begin, end));

    content.append("<HR/><B>" + getGUI().getConfig().getString("last_1_hour") + "</B><BR/>");
    end = new Date();
    begin = new Date(new Date().getTime() - 3600000);
    content.append(analyzeEvents(getTarget().getEvents(begin, end, browserEventClass()), begin, end));

    return new StringBuffer(htmlFace(content.toString()));
  }

  /**
   * Returns a report about a specific period of time.
   * @param events every event managed by this view and concerning the associated target.
   * @param begin start time.
   * @param end end time.
   * @return String HTML report.
   */
  private StringBuffer analyzeEvents(List<EventGeneric> events, final Date begin, final Date end) {
    final StringBuffer content = new StringBuffer();

    if (events.size() > 0 && events.get(events.size() - 1).getDate().after(end))
      events.remove(events.size() - 1);

    if (events.size() > 0 && events.get(0).getDate().before(begin))
      events.remove(0);

    if (events.size() > 0)
      content.append("<B>" + events.size() + " " + getGUI().getConfig().getString("event") +
          (events.size() > 1 ? "s" : "") +
          "</B><BR/><TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR><TD bgcolor='lightyellow'>" +
          htmlFace(getGUI().getConfig().getString("first_event")) +
          "</TD><TD bgcolor='lightyellow' ALIGN='right'>" +
          htmlFace(events.get(0).getDate().toString()) +
          "</TD></TR><TR><TD bgcolor='lightyellow'>" +
          htmlFace(getGUI().getConfig().getString("last_event")) +
          "</TD><TD bgcolor='lightyellow' ALIGN='right'>" +
          htmlFace(events.get(events.size() - 1).getDate().toString()) + "</TD></TR>");
    else {
      content.append(getGUI().getConfig().getString("no_event") + "<BR/>");
      return content;
    }

    // if the first event has a null value, we do not take it into account

    int min_value, max_value = 0;
    double average = 0;
    int i = 0;

    if (events.size() == 1) min_value = events.get(0).getIntValue();
    else {
      if (events.get(0).getIntValue() == 0) min_value = events.get(1).getIntValue();
      else min_value = events.get(0).getIntValue();
    }

    for (final EventGeneric event : events) {
      final int value = event.getIntValue();
      if (value > max_value) max_value = value;
      if ((i == 0 && value != 0 || i != 0) && value < min_value) min_value = value;
      average += value;
      i++;
    }

    content.append("<TR><TD bgcolor='lightyellow'>"
                   + htmlFace(getGUI().getConfig().getString("min_value"))
                   + "</TD><TD bgcolor='lightyellow' ALIGN='right'>"
                   + htmlFace(GenericTools.formatNumericString(getGUI().getConfig(), "" + min_value) + " " + browserUnit()) + "</TD></TR>");
    content.append("<TR><TD bgcolor='lightyellow'>"
                   + htmlFace(getGUI().getConfig().getString("max_value"))
                   + "</TD><TD bgcolor='lightyellow' ALIGN='right'>"
                   + htmlFace(GenericTools.formatNumericString(getGUI().getConfig(), "" + max_value) + " " + browserUnit()) + "</TD></TR>");

    if (events.size() != 0) {
      average = average / (events.size() - (events.get(0).getIntValue() != 0 ? 0 : 1));
      content.append("<TR><TD bgcolor='lightyellow'>"
                     + htmlFace(getGUI().getConfig().getString("average"))
                     + "</TD><TD bgcolor='lightyellow' ALIGN='right'>"
                     + htmlFace(GenericTools.formatNumericString(getGUI().getConfig(), "" + (int) average) + " " + browserUnit()) + "</TD></TR>");
    }

    content.append("</TABLE><BR/>");

    return content;
  }

  /**
   * Creates a new display component.
   * @param none.
   * @return BasicComponent new display component.
   */
  // lock survey: synchro << sync_tree << HERE
  // lock survey: synchro << sync_tree << folder << HERE
  protected abstract BasicComponent createComponent();

  /**
   * Computes a new version of the report.
   * @param none.
   * @return void.
   */
  // lock survey: synchro << sync_tree << HERE
  // lock survey: synchro << sync_tree << folder << HERE
  private void updateBrowserContent() {
  // If you get the following error message "org.eclipse.swt.SWTError XPCOM error -2147467262",
  // try removing any xulrunner version higher than the latest 1.8.x.

  browser.setText("<html><body bgcolor='#" +
        String.format("%2x%2x%2x",
            getGUI().getBackgroundColor().getRed(),
            getGUI().getBackgroundColor().getGreen(),
            getGUI().getBackgroundColor().getBlue()) +
            "'><small>" +
            getBrowserContent() +
            "</small></body></html>");
  }

  /**
   * Called when the user wants this DataView instance to create a CTabFolder instance
   * containing a report.
   * @param none.
   * @return void.
   */
  // this method must only be called from the SWT thread
  // GUI thread
  // lock survey: synchro << sync_tree << HERE
  public void informSelected() {
    final CTabFolder folder = getGUI().getTabFolder();

    // ce synchr n'est pas forc�ment utile
    synchronized (folder) {
      // create a tab item

      boolean tab_item_found = false;
      for (final CTabItem tab_item : folder.getItems())
        if (tab_item == this.tab_item) {
          folder.setSelection(tab_item);
          tab_item_found = true;
          updateBrowserContent();
        }
      if (tab_item_found == false) {
        // create a new tab item
        tab_item = new CTabItem(folder, SWT.CLOSE);
        tab_item.setText(getTitle());
        folder.setSelection(tab_item);

        // create a control inside the tab item
        browser = new Browser(getGUI().getTabFolder(), SWT.BORDER | SWT.FILL);
        tab_item.setControl(browser);

        updateBrowserContent();
      }

      // create a drawing window

      if (frame != null && frame.isDisplayable()) {
        frame.setVisible(true);
        frame.toFront();
      } else {
        final BasicComponent component = createComponent();
        try {
          component.init();
          final String title = getTarget().getParents().get(0).getItem()
          + " / " + getTarget().getItem()
          + " / " + getItem();
          frame = getGUI().getAwtGUI().createFrame(title /* getTarget().toString() */, component);
        } catch (final InterruptedException ex) {
          log.info("Exception", ex);
        } catch (final InvocationTargetException ex) {
          log.warn("Exception", ex);
        }
      }
    }
  }

  /**
   * Removes objects associated with this DataView instance.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    super.disposed();
    if (frame != null) getGUI().getAwtGUI().dropFrame(frame);
    if (tab_item != null) tab_item.dispose();
  }
}
