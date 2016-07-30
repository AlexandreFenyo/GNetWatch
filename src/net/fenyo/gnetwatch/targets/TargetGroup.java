
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
import net.fenyo.gnetwatch.GUI.GUI;
import net.fenyo.gnetwatch.GUI.VisualElement;
import net.fenyo.gnetwatch.actions.ActionGenericProcess;
import net.fenyo.gnetwatch.actions.ActionGenericSrc;
import net.fenyo.gnetwatch.actions.ActionPing;
import net.fenyo.gnetwatch.data.EventReachable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TargetGroup implements a target that can contain other targets.
 * @author Alexandre Fenyo
 * @version $Id: TargetGroup.java,v 1.13 2008/05/22 16:02:59 fenyo Exp $
 */

public class TargetGroup extends Target {
  private static Log log = LogFactory.getLog(TargetGroup.class);

  // persistent
  private String groupName; // not null

  // persistent
  private GenericQuerier generic_querier;

  /**
   * Constructor.
   * @param name target name.
   * @param group_name group name.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetGroup(final String name, final String group_name) throws AlgorithmException {
    super(name);
    groupName = group_name;
    setItem(group_name);
    generic_querier = new GenericQuerier();
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  public TargetGroup() {}

  public GenericQuerier getGenericQuerier() {
    return generic_querier;
  }

  public void setGenericQuerier(final GenericQuerier generic_querier) {
    this.generic_querier = generic_querier;
  }

  /**
   * Returns the group name.
   * @param none.
   * @return String group name.
   */
  protected String getGroupName() {
    return groupName;
  }

  protected void setGroupName(final String group_name) {
    this.groupName = group_name;
    setItem(group_name);
  }

  /**
   * Checks that the parameter can be attached to this target.
   * @param visual_element parameter to check.
   * @return true if the parameter can be attached to this target.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    if (Target.class.isInstance(visual_element)) return true;
    if (ActionGenericSrc.class.isInstance(visual_element)) return true;
    if (ActionGenericProcess.class.isInstance(visual_element)) return true;
    return false;
  }

  /**
   * Compares two targets.
   * @param o target to compare to.
   * @return true if the targets are equal.
   */
  // any thread
  public boolean equals(final Object o) {
    if (this == o) return true;
    if ((o == null) || (o.getClass() != getClass())) return false;
    final TargetGroup target = (TargetGroup) o;
    return getGroupName().equals(target.getGroupName());
  }

  /**
   * Returns the hashcode for this target.
   * @param none.
   * @return int hashcode.
   */
  // any thread
  public int hashCode() {
    return getGroupName().hashCode();
  }
}
