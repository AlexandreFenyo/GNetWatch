
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
 * This view displays RTT to an IP target.
 * @author Alexandre Fenyo
 * @version $Id: GenericProcessView.java,v 1.2 2008/05/22 16:02:59 fenyo Exp $
 */

public class GenericProcessView extends DataView {
  private static Log log = LogFactory.getLog(GenericProcessView.class);

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param target ingress target interface.
   */
  // GUI thread
  public GenericProcessView(final GUI gui, final Target target) {
    super(gui, target);
    setItem(getGUI().getConfig().getString("external_process"));
  }

  public GenericProcessView() {}

  /**
   * Creates a new display component.
   * @param none.
   * @return BasicComponent new display component.
   */
  protected BasicComponent createComponent() {
    return new GenericProcessComponent(getTarget());
  }

  /**
   * Returns the name of report this view can generate.
   * @param none.
   * @return String report name.
   */
  protected String browserName() {
    return getGUI().getConfig().getString("process_view");
  }

  /**
   * Returns the data unit for values this view can generate.
   * @param none.
   * @return String data unit.
   */
  protected String browserUnit() {
    return ((TargetGroup) getTarget()).getGenericQuerier().getUnit();
  }

  /**
   * Returns the event class this view can manage.
   * @param none.
   * @return Class event class.
   */
  public Class browserEventClass() {
    return EventGenericProcess.class;
  }

  /**
   * Returns a report as an HTML string.
   * @param none.
   * @return StringBull HTML report.
   */
  protected StringBuffer getBrowserContent() {
    return super.getBrowserContent();
  }
}
