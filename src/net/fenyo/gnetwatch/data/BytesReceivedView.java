
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

import java.lang.reflect.*;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.targets.*;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * This view displays SNMP counters relative to ingress interfaces.
 * @author Alexandre Fenyo
 * @version $Id: BytesReceivedView.java,v 1.13 2008/04/23 00:46:41 fenyo Exp $
 */

public class BytesReceivedView extends DataView {
  private static Log log = LogFactory.getLog(BytesReceivedView.class);

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param target ingress target interface.
   */
  // GUI thread
  public BytesReceivedView(final GUI gui, final Target target) {
    super(gui, target);
    setItem("ingress");
  }

  public BytesReceivedView() {}

  /**
   * Creates a new display component.
   * @param none.
   * @return BasicComponent new display component.
   */
  protected BasicComponent createComponent() {
    return new BytesReceivedComponent(getTarget());
  }

  /**
   * Returns the name of report this view can generate.
   * @param none.
   * @return String report name.
   */
  protected String browserName() {
    return getGUI().getConfig().getString("ingress_interface");
  }

  /**
   * Returns the data unit for values this view can generate.
   * @param none.
   * @return String data unit.
   */
  protected String browserUnit() {
    return "bit/s";
  }

  /**
   * Returns the event class this view can manage.
   * @param none.
   * @return Class event class.
   */
  public Class browserEventClass() {
    return EventBytesReceived.class;
  }

  /**
   * Returns a report as an HTML string.
   * @param none.
   * @return StringBull HTML report.
   */
  // lock survey: synchro << sync_tree << HERE
  protected StringBuffer getBrowserContent() {
    return super.getBrowserContent();
  }
}