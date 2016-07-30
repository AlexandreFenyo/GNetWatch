
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
 * NmapView displays output text produced by Nmap.
 * @author Alexandre Fenyo
 * @version $Id: NmapView.java,v 1.6 2008/04/20 23:44:27 fenyo Exp $
 */

public class NmapView extends DataView {
  private static Log log = LogFactory.getLog(NmapView.class);

//  final private Target target;
//  private CTabItem tab_item = null;

  private Browser browser = null;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param target ingress target interface.
   */
  // GUI thread
  public NmapView(final GUI gui, final Target target) {
    super(gui, target);
    setItem("nmap signature");
  }

  public NmapView() {}

  /**
   * Sets the current GUI instance.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageMultiRow();
  }

  /**
   * Returns the name of report this view can generate.
   * @param none.
   * @return String report name.
   */
  protected String browserName() {
    return "NMAP";
  }

  /**
   * Returns the data unit for values this view can generate.
   * @param none.
   * @return String data unit.
   */
  protected String browserUnit() {
    return "no unit";
  }

  /**
   * Returns the event class this view can manage.
   * @param none.
   * @return Class event class.
   */
  public Class browserEventClass() {
    return EventNmap.class;
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
   * Returns the title of the view.
   * @param none.
   * @return String title view.
   */
  // GUI thread
  public String getTitle() {
    return "NmapView";
  }

  /**
   * Embed face informations in an HTML part. 
   * @param html source part.
   * @return String embedded html part.
   */
  private String htmlFace(final String html) {
    return getGUI().htmlFace(html);
  }

  /**
   * Generates the Nmap report.
   * @param none.
   * @return StringBuffer Nmap report.
   */
  protected StringBuffer getBrowserContent() {
    final StringBuffer content = new StringBuffer();

   content.append("Nmap output:<BR/><TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'><TR><TD bgcolor='lightyellow'>" +
       htmlFace("<PRE>" + ((EventNmap) getTarget().getLastEvent(EventNmap.class)).getOutput() + "</PRE>") +
       "</TD></TR></TABLE");

    return new StringBuffer(htmlFace(content.toString()));
  }

  /**
   * Computes a new version of the report.
   * @param none.
   * @return void.
   */
  private void updateBrowserContent() {
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
   * Called when the user wants this NmapView instance to create a CTabFolder instance
   * containing a report.
   * @param none.
   * @return void.
   */
  // this method must only be called from the SWT thread
  // GUI thread
  final public void informSelected() {
    final CTabFolder folder = getGUI().getTabFolder();

    // ce synchr n'est pas forcément utile
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

    }
  }

  /**
   * Creates a new display component.
   * @param none.
   * @return BasicComponent new display component.
   */
  protected BasicComponent createComponent() {
    return null;
  }
}
