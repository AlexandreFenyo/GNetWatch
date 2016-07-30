
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

package net.fenyo.gnetwatch;

import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.targets.*;
import net.fenyo.gnetwatch.GUI.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import java.util.*;
import org.hibernate.*;

/**
 * This class is dedicated to maintain main operations.
 * @author Alexandre Fenyo
 * @version $Id: Main.java,v 1.18 2008/04/15 23:58:17 fenyo Exp $
 */

public class Main {
  private static Log log = LogFactory.getLog(Main.class);
  private final Config config;
  private GUI gui = null;
  private final CaptureManager capture_mgr;
  private CaptureManager.HandlePacket handler = null;

  /**
   * Constructor.
   * main thread
   * @param config configuration.
   */
  public Main(final Config config, final CaptureManager capture_mgr) {
    this.config = config;
    this.capture_mgr = capture_mgr;
  }

  /**
   * Defines the GUI instance.
   * @param GUI gui.
   * @return void.
   */
  public void setGUI(final GUI gui) {
    this.gui = gui;
  }

  /**
   * Ask to spawn tethereal instances and add targets for new discovered IP targets.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void startDiscover() {
    handler =
      new CaptureManager.HandlePacket() {
        public void document(final Document packet) {
          final List<Node> nodes = packet.selectNodes("/packet/proto[@name='ip']/field[@name='ip.addr']");
          if (nodes.size() == 2) {
            final String srcaddr = nodes.get(0).valueOf("@show");
            final String dstaddr = nodes.get(1).valueOf("@show");
            // shorter but slower way :
            // final String srcaddr = (String) packet.selectObject("string(//field[@name='ip.addr'][1]/@show)");

//            log.debug("ipv4 packet captured: " + srcaddr + " -> " + dstaddr);

            TargetIPv4.addTargetIPv4(gui, srcaddr);
            TargetIPv4.addTargetIPv4(gui, dstaddr);
          }

          final List<Node> nodes_ipv6 = packet.selectNodes("/packet/proto[@name='ipv6']/field[@name='ipv6.addr']");
          if (nodes_ipv6.size() == 2) {
            final String srcaddr = nodes_ipv6.get(0).valueOf("@show");
            final String dstaddr = nodes_ipv6.get(1).valueOf("@show");

//            log.debug("ipv6 packet captured: " + srcaddr + " -> " + dstaddr);

            TargetIPv6.addTargetIPv6(gui, srcaddr);
            TargetIPv6.addTargetIPv6(gui, dstaddr);
          }
        }
      };
    try {
      capture_mgr.registerListener("ip", handler);
    } catch (final InterruptedException ex) {
      log.error("Exception", ex);
    }
  }

  /**
   * Ask to kill tethereal instances if this is the last listener.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void stopDiscover() {
    try {
      capture_mgr.unRegisterListener("ip", handler);
    } catch (final InterruptedException ex) {
      log.error("Exception", ex);
    }
  }
}
