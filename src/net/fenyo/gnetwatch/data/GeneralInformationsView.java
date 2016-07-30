
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
import net.fenyo.gnetwatch.targets.Target;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.browser.Browser;

/**
 * This view is intended to display general informations.
 * @author Alexandre Fenyo
 * @version $Id: GeneralInformationsView.java,v 1.13 2008/04/15 23:58:18 fenyo Exp $
 */

public class GeneralInformationsView extends DataView {
  private static Log log = LogFactory.getLog(GeneralInformationsView.class);

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param target ingress target interface.
   */
  // GUI thread
  public GeneralInformationsView(final GUI gui, final Target target) {
    // attention : instancier cette classe dans un sync(sync_tree)
    super(gui, target);
  }

  public GeneralInformationsView() {}

  /**
   * Creates a new display component.
   * @param none.
   * @return BasicComponent new display component.
   */
  protected BasicComponent createComponent() {
    return null;
  }

  /**
   * Returns the name of report this view can generate.
   * @param none.
   * @return String report name.
   */
  protected String browserName() {
    return getGUI().getConfig().getString("general");
  }

  /**
   * Returns the data unit for values this view can generate.
   * @param none.
   * @return String data unit.
   */
  protected String browserUnit() {
    return getGUI().getConfig().getString("byte") + "/s";
  }

  /**
   * Returns the event class this view can manage.
   * @param none.
   * @return Class event class.
   */
  public Class browserEventClass() {
    return null;
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
