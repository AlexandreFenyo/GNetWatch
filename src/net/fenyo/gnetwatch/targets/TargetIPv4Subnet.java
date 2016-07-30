
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
 * TargetIPv4Subnet implements an IPv4 subnet defined by a subnet address and a subnet mask.
 * @author Alexandre Fenyo
 * @version $Id: TargetIPv4Subnet.java,v 1.13 2008/04/15 23:58:17 fenyo Exp $
 */

public class TargetIPv4Subnet extends Target {
  private static Log log = LogFactory.getLog(TargetIPv4Subnet.class);

  // persistent - not null
  private Inet4Address network;

  // persistent - not null
  private Inet4Address netmask;

  /**
   * Constructor.
   * @param name target name.
   * @param network network address.
   * @param netmask netmask value.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetIPv4Subnet(final String name, final Inet4Address network, final Inet4Address netmask) throws AlgorithmException {
    super(name);
    if (network == null || netmask == null) throw new AlgorithmException("network or netmask is null");
    this.network = network;
    this.netmask = netmask;
    setItem(network.getHostAddress() + "/" + netmask.getHostAddress());
  }

  public TargetIPv4Subnet() throws AlgorithmException {
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
   * Returns the network address of this subnet.
   * @param none.
   * @return Inet4Address network address.
   */
  // any thread
  protected Inet4Address getNetwork() {
    return network;
  }

  public void setNetwork(final Inet4Address network) {
    this.network= network;
  }

  /**
   * Returns the netmask of this subnet.
   * @param none.
   * @return Inet4Address netmask.
   */
  // any thread
  protected Inet4Address getNetmask() {
    return netmask;
  }

  public void setNetmask(final Inet4Address netmask) {
    this.netmask = netmask;
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
    final TargetIPv4Subnet target = (TargetIPv4Subnet) o;
    return getNetwork().equals(target.getNetwork()) &&
      getNetmask().equals(target.getNetmask());
  }

  /**
   * Returns the hashcode for this target.
   * @param none.
   * @return int hashcode.
   */
  // any thread
  public int hashCode() {
    return getNetwork().hashCode() ^ getNetmask().hashCode();
  }
}
