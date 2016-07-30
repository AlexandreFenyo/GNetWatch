
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

package net.fenyo.gnetwatch.targets;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.*;
import net.fenyo.gnetwatch.data.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TargetInterface implements an interface of an IP target.
 * @author Alexandre Fenyo
 * @version $Id: TargetInterface.java,v 1.11 2008/04/15 23:58:17 fenyo Exp $
 */

public class TargetInterface extends Target {
  private static Log log = LogFactory.getLog(TargetInterface.class);

  // persistent
  private String interfaceName;

  /**
   * Constructor.
   * @param name target name.
   * @param interface_name interface name.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetInterface(final String name, final String interface_name) throws AlgorithmException {
    super(name);
    interfaceName = interface_name;
    setItem(interface_name);
  }

  /**
   * Default constructor.
   * @param none.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetInterface() throws AlgorithmException {
//    interfaceName = null;
  }

  /**
   * Initializes this target.
   * @param gui current GUI instance.
   * @return void.
   */
  // final because called by constructor (by means of setItem())
  protected final void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageInterface();
  }

  protected String getInterfaceName() {
    return interfaceName;
  }

  protected void setInterfaceName(final String interface_name) {
    interfaceName = interface_name;
    setItem(interface_name);
  }

  /**
   * Checks that the parameter can be attached to this target.
   * @param visual_element parameter to check.
   * @return true if the parameter can be attached to this target.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    if (Target.class.isInstance(visual_element)) return true;
    return false;
  }

  // we do not define equals() nor hashCode() since we want name being the discriminant
  // instead of interface_name
}
