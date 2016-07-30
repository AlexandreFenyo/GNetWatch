
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
import net.fenyo.gnetwatch.actions.*;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.GUI.GUI;
import net.fenyo.gnetwatch.GUI.VisualElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;

import java.net.*;

/**
 * TargetIPv4 implements an IPv4 capable target.
 * @author Alexandre Fenyo
 * @version $Id: TargetIPv4.java,v 1.44 2008/08/01 17:35:40 fenyo Exp $
 */

public class TargetIPv4 extends Target {
  private static Log log = LogFactory.getLog(TargetIPv4.class);

  // persistent - not null
  private Inet4Address address;

  // persistent
  private SNMPQuerier snmp_querier;

  // persistent
  private IPQuerier ip_querier;

  public TargetIPv4() throws AlgorithmException {
//    address = null;
//    snmp_querier = null;
//    ip_querier = null;
  }

    /**
   * Constructor.
   * @param name target name.
   * @param address IPv4 address.
   * @param SNMPManager snmp manager.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public TargetIPv4(final String name, final Inet4Address address, final SNMPManager snmp_manager) throws AlgorithmException {
    super(name);
    if (address == null) throw new AlgorithmException("address is null");
    this.address = address;
    snmp_querier = snmp_manager != null ? snmp_manager.getQuerier(address) : null;
    setItem(address.getHostAddress());
    ip_querier = new IPQuerier(address);
    // may last a long time (DNS resolver)
    // setDescription(address.getCanonicalHostName());
  }

  /**
   * Checks that this host is SNMP capable.
   * @param none.
   * @return void.
   */
// PENSER a changer l'icone en cas d'action SNMP qui marche car si ici ca a fait timeout... ET a mettre a jour le type
  // faire drop action nmap et drop view nmap
  // lock survey: synchro << sync_tree << HERE
  public void checkSNMPAwareness() {
    final TargetIPv4 _this = this;

    snmp_querier.getSysDescr(new SNMPQuerier.QuerierListener() {
      public void onResponse(final ResponseEvent event) {
        // need to synchronize here to be sure objects will not be attached to another session
        synchronized (getGUI().getSynchro()) {
          // lock survey: synchro << HERE

          // need to catch every exception we could generate since SNMP4J does not log them in the asynchronous thread...
          try {
            getGUI().setStatus(getGUI().getConfig().getPattern("discovered_snmp", getAddress().toString().substring(1)));
            getGUI().appendConsole(getGUI().getConfig().getPattern("discovered_snmp", getAddress().toString().substring(1)) + "<BR/>");
            setImageHostSNMP();

            if (event != null && event.getResponse() != null && event.getResponse().size() > 0 &&
                event.getResponse().get(0) != null && event.getResponse().get(0).getVariable() != null) {
              final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
              session.beginTransaction();
              try {
              session.update(_this);
              setType(event.getResponse().get(0).getVariable().toString());
              session.getTransaction().commit();
              } catch (final Exception ex) {
                log.error("Exception", ex);
                session.getTransaction().rollback();
              }
            } else log.error("got a bad SNMP response");
          } catch (final Exception ex) {
            log.error("Exception", ex);
          }
        }
      }

      public void onTimeout(final ResponseEvent event) {
        try {
          getGUI().setStatus(getGUI().getConfig().getPattern("snmp_timeout", getAddress().toString().substring(1)));
        } catch (final Exception ex) {
          log.error("Exception", ex);
        }
      }
    });
  }

  /**
   * Returns the SNMP querier.
   * @param none.
   * @return SNMPQuerier querier instance.
   */
  public SNMPQuerier getSNMPQuerier() {
    return snmp_querier;
  }

  public void setSNMPQuerier(final SNMPQuerier snmp_querier) {
    this.snmp_querier = snmp_querier;
  }

  /**
   * Returns the IP querier.
   * @param none.
   * @return IPQuerier querier instance.
   */
  public IPQuerier getIPQuerier() {
    return ip_querier;
  }

  public void setIPQuerier(final IPQuerier ip_querier) {
    this.ip_querier = ip_querier;
  }

  /**
   * Returns the IP address.
   * @param none.
   * @return Inet4Address IP address.
   */
  // any thread
  public Inet4Address getAddress() {
    return address;
  }

  public void setAddress(final Inet4Address address) {
    this.address = address;
  }

  /**
   * Checks that the parameter can be attached to this target.
   * @param visual_element parameter to check.
   * @return true if the parameter can be attached to this target.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    if (ActionPing.class.isInstance(visual_element)) return true;
    if (ActionFlood.class.isInstance(visual_element)) return true;
    if (ActionSNMP.class.isInstance(visual_element)) return true;
    if (EventReachable.class.isInstance(visual_element)) return true;
    if (TargetInterface.class.isInstance(visual_element)) return true;
    if (ActionHTTP.class.isInstance(visual_element)) return true;
    if (ActionNmap.class.isInstance(visual_element)) return true;
    return false;
  }

  /**
   * Initializes this target.
   * @param gui current GUI instance.
   * @return void.
   */
  // final because called by constructor (by means of setItem())
  public final void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageHost(); // bcl sans fil qui arrive parfois au lancement (3)
  }

  /**
   * Attaches this target to a specific parent.
   * @param gui current GUI instance.
   * @param parent parent.
   * @return true if this target has been succesfully attached.
   */
  public boolean addTarget(final GUI gui, final Target parent) {
    initialize(gui); // bcl sans fil qui arrive parfois au lancement (2)

    if (!canAddTarget(parent)) return false;
    if (parent != null && !parent.canManageThisChild(this)) return false;

    final boolean is_new = !getGUI().containsCanonicalInstance(this);
    final TargetIPv4 target_ipv4 = (TargetIPv4) getGUI().getCanonicalInstance(this);

    final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
    session.saveOrUpdate(target_ipv4);
    session.update(parent);

    if (!getGUI().getVisualTransientAll().contains(target_ipv4)) {
      session.update(getGUI().getVisualTransientAll());
      target_ipv4.setParent(getGUI(), getGUI().getVisualTransientAll());
    }

    final String addr_str = GenericTools.inet4AddressToString(target_ipv4.getAddress());
    final String net_str = GenericTools.getNetFromAddress(addr_str);

    if (addr_str == null || net_str == null) {
      if (is_new) getGUI().dropTargetInstance(target_ipv4);
      log.error("addr_str or net_str is null - " + getItem());
      return false;
    }

    Inet4Address net_addr = null;
    Inet4Address net_netmask = null;

    try {
      if (net_str.endsWith("/4")) {
        net_addr = (Inet4Address)
        InetAddress.getByName(GenericTools.unsignedByteToShort(address.getAddress()[0]) < 240 ?
            "224.0.0.0" : "240.0.0.0");
        net_netmask = (Inet4Address) InetAddress.getByName("240.0.0.0");
      }

      if (net_str.endsWith("/8")) {
        net_addr = (Inet4Address) InetAddress.getByName(net_str.substring(0, net_str.length() - 2));
        net_netmask = (Inet4Address) InetAddress.getByName("255.0.0.0");
      }

      if (net_str.endsWith("/16")) {
        net_addr = (Inet4Address) InetAddress.getByName(net_str.substring(0, net_str.length() - 3));
        net_netmask = (Inet4Address) InetAddress.getByName("255.255.0.0");
      }

      if (net_str.endsWith("/24")) {
        net_addr = (Inet4Address) InetAddress.getByName(net_str.substring(0, net_str.length() - 3));
        net_netmask = (Inet4Address) InetAddress.getByName("255.255.255.0");
      }
    } catch (final UnknownHostException ex) {
      if (is_new) getGUI().dropTargetInstance(target_ipv4);
      log.error("Exception", ex);
      return false;
    }

    TargetIPv4Subnet subnet;
    try {
      subnet = new TargetIPv4Subnet(addr_str, net_addr, net_netmask);
    } catch (final AlgorithmException ex) {
      if (is_new) getGUI().dropTargetInstance(target_ipv4);
      log.error("Exception", ex);
      return false;
    }

    if (getGUI().getConfig().getProperty("enableeverynetworks") != null &&
        getGUI().getConfig().getProperty("enableeverynetworks").equals("true")) {
      subnet = (TargetIPv4Subnet) getGUI().getCanonicalInstance(subnet);
      session.saveOrUpdate(subnet);
      if (!getGUI().getVisualTransientNetworks().contains(subnet)) {
        session.update(getGUI().getVisualTransientNetworks());
        subnet.setParent(getGUI(), getGUI().getVisualTransientNetworks());
      }
      if (!subnet.contains(target_ipv4)) target_ipv4.setParent(getGUI(), subnet);
    }

    if (parent != null) target_ipv4.setParent(getGUI(), parent);

    return is_new;
  }

  /**
   * Attaches this target to a specific parent defined by its address.
   * @param gui current GUI instance.
   * @param addr_str parent address.
   * @return true if this target has been succesfully attached.
   */
  public static void addTargetIPv4(final GUI gui, final String addr_str) {
    try {
      synchronized (gui.sync_tree) {
        final TargetIPv4 foo = new TargetIPv4(addr_str, (Inet4Address) InetAddress.getByName(addr_str), gui.getSNMPManager());
        if (gui.containsCanonicalInstance(foo)) return;
        gui.asyncExec(new Runnable() {
          public void run() {
            synchronized (gui.getSynchro()) {
              synchronized (gui.sync_tree) {
                if (gui.containsCanonicalInstance(foo)) return;

                final Session session = gui.getSynchro().getSessionFactory().getCurrentSession();
                session.beginTransaction();
                // à revoir
                try {
                  if (foo.addTarget(gui, (Target) null) == true) {
                    session.getTransaction().commit();
                    gui.setStatus(gui.getConfig().getPattern("adding_target", foo.getAddress().toString().substring(1)));
                    foo.checkSNMPAwareness();
                  } else session.getTransaction().rollback();
                } catch (final Exception ex) {
                  log.error("Exception", ex);
                  session.getTransaction().rollback();
                }

              }
            }
          }
        });
      }
    } catch (final AlgorithmException ex) {
      log.error("Exception", ex);
    } catch (final UnknownHostException ex) {
      log.error("Exception", ex);
    }
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
    final TargetIPv4 target = (TargetIPv4) o;
    return getAddress().equals(target.getAddress());
  }
  
  /**
   * Returns the hashcode for this target.
   * @param none.
   * @return int hashcode.
   */
  // any thread
  public int hashCode() {
    return getAddress().hashCode();
  }
}
