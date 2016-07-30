
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.net.*;

/**
 * TargetIPv4Range implements a range defined by two IPv4 adresses.
 * @author Alexandre Fenyo
 * @version $Id: TargetIPv4Range.java,v 1.13 2008/04/15 23:58:17 fenyo Exp $
 */

public class TargetIPv4Range extends Target {
  private static Log log = LogFactory.getLog(TargetIPv4Range.class);

  // persistent - not null
  private Inet4Address begin;

  // persistent - not null
  private Inet4Address end;

  /**
   * Constructor.
   * @param name target name.
   * @param begin first address.
   * @param end last address.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetIPv4Range(final String name, final Inet4Address begin, final Inet4Address end) throws AlgorithmException {
    super(name);
    if (begin == null || end == null) throw new AlgorithmException("begin or end is null");
    this.begin = begin;
    this.end = end;
    setItem(begin.getHostAddress() + "-" + end.getHostAddress());
  }

  public TargetIPv4Range() throws AlgorithmException {
  }

  /**
   * Initializes this target.
   * @param gui current GUI instance.
   * @return void.
   */
  public void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageNetwork();
  }

  /**
   * Returns the first address in the range.
   * @param none.
   * @return Inet4Address first address.
   */
  // any thread
  protected Inet4Address getBegin() {
    return begin;
  }

  public void setBegin(final Inet4Address begin) {
    this.begin = begin;
  }

  /**
   * Returns the last address in the range.
   * @param none.
   * @return Inet4Address last address.
   */
  // any thread
  protected Inet4Address getEnd() {
    return end;
  }

  public void setEnd(final Inet4Address end) {
    this.end= end;
  }

  /**
   * Checks that the parameter can be attached to this target.
   * @param visual_element parameter to check.
   * @return true if the parameter can be attached to this target.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    if (TargetIPv4.class.isInstance(visual_element)) return true;
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
    final TargetIPv4Range target = (TargetIPv4Range) o;
    return getBegin().equals(target.getBegin()) && getEnd().equals(target.getEnd());
  }
  
  /**
   * Returns the hashcode for this target.
   * @param none.
   * @return int hashcode.
   */
  // any thread
  public int hashCode() {
    return getBegin().hashCode() ^ getEnd().hashCode();
  }
}
