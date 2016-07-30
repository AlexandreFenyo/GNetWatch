
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

package net.fenyo.gnetwatch.actions;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.VisualElement;
import net.fenyo.gnetwatch.actions.Action.InterruptCause;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.mp.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;
import org.snmp4j.event.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * Instances of this action class use SNMP to get the interface list of their target,
 * and to get SNMP counters for each interface.
 * @author Alexandre Fenyo
 * @version $Id: ActionSNMP.java,v 1.40 2008/08/07 16:44:37 fenyo Exp $
 */

public class ActionSNMP extends Action {
  private static Log log = LogFactory.getLog(ActionPing.class);

  private boolean interrupted = false;
  private boolean interfaces_created = false;

  private Map<Integer, TargetInterface> targets = new HashMap<Integer, TargetInterface>();
  private Map<Integer, Long> last_total_bytes_received = new HashMap<Integer, Long>();
  private Map<Integer, Long> last_total_bytes_sent = new HashMap<Integer, Long>();
  private Map<Integer, Long> last_total_bytes_received_time = new HashMap<Integer, Long>();
  private Map<Integer, Long> last_total_bytes_sent_time = new HashMap<Integer, Long>();

  /**
   * Default constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionSNMP(final net.fenyo.gnetwatch.targets.Target target, final Background background) {
    super(target, background);
    setItem("snmp");
  }

  /**
   * Constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionSNMP() {
    setItem("snmp");
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "snmp";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  public long getMaxDelay() {
    return 30000000;
  }

  /**
   * Asks this action to stop rapidely.
   * @param cause cause.
   * @return void.
   * @throws IOException IO exception.
   */
  // main & Background threads
  // supports any thread
  public void interrupt(final InterruptCause cause) {
    interrupted = true;
  }

  /**
   * Gets the interface list to create interfaces and gets snmp counters.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  // Queue thread
  // supports any thread
  public void invoke() throws IOException, InterruptedException {
    if (isDisposed() == true) return;

    super.invoke();

    final SNMPQuerier querier;
    if (TargetIPv4.class.isInstance(getTarget())) {
      querier = (SNMPQuerier) ((TargetIPv4) getTarget()).getSNMPQuerier().clone();
    } else if (TargetIPv6.class.isInstance(getTarget())) {
      querier = (SNMPQuerier) ((TargetIPv6) getTarget()).getSNMPQuerier().clone();
    } else return;

    if (interfaces_created == false) {
      interfaces_created = true;

      getGUI().setStatus(getGUI().getConfig().getPattern("get_if_list", querier.getAddress().toString().substring(1)));
      final java.util.List<TableEvent> interfaces = querier.getInterfaces();

      if (querier.isSNMPCapable()) getTarget().setImageHostSNMP();

      synchronized (getGUI().getSynchro()) {
        synchronized (getGUI().sync_tree) {
          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
          session.beginTransaction();
          try {
            for (final TableEvent table_event : interfaces) {
              try {
                if (table_event == null || table_event.getColumns() == null || table_event.getColumns().length < 2 ||
                    table_event.getColumns()[0] == null || table_event.getColumns()[0].getVariable() == null ||
                    table_event.getColumns()[1] == null || table_event.getColumns()[1].getVariable() == null) {
                  log.info("can not get the full interface list of " + querier.getAddress().toString().substring(1));
                  getGUI().setStatus(getGUI().getConfig().getPattern("cannot_get_if_list", querier.getAddress().toString().substring(1)));
                  getGUI().appendConsole("<HR/>" + getGUI().getConfig().getPattern("cannot_get_if_list", querier.getAddress().toString().substring(1)));

                  // comme il y a des synchronized, on fait pas de asyncExecIfNeeded mais un asyncExec
/*                  getGUI().asyncExec(new Runnable() {
                    public void run() {
                      synchronized (getGUI().getSynchro()) {
                        synchronized (getGUI().sync_tree) {
                          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
                          session.beginTransaction();
                          try  {
                            if (getParents() != null && getParents().size() > 0 && getParents().get(0) != null)
                              removeVisualElements(getParents().get(0));
                            else log.error("no parent!");
                            session.getTransaction().commit();
                          } catch (final Exception ex) {
                            log.error("Exception", ex);
                            session.getTransaction().rollback();
                          }
                        }
                      }
                    }
                  });*/
                } else {
                  getGUI().setStatus(getGUI().getConfig().getPattern("received_if_list", querier.getAddress().toString().substring(1)));
                  final TargetInterface foo =
                    new TargetInterface(getTarget().toString() + ".interface[" +
                        table_event.getColumns()[0].getVariable().toString() + "]",
                        ((OctetString) table_event.getColumns()[1].getVariable()).toASCII(' '));

                  if (table_event.getColumns().length > 10 && table_event.getColumns()[10] != null &&
                      table_event.getColumns()[10].getVariable() != null)
                    foo.setDescription(table_event.getColumns()[10].getVariable().toString());

                  if (getGUI().containsCanonicalInstance(foo) == true)
                    targets.put(new Integer(table_event.getColumns()[0].getVariable().toString()),
                        (TargetInterface) getGUI().getCanonicalInstance(foo));
                  else

                    getGUI().asyncExec(new Runnable() {
                      public void run() {
                        synchronized (getGUI().getSynchro()) {
                          synchronized (getGUI().sync_tree) {

                            if (getGUI().containsCanonicalInstance(foo)) return;

                            final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
                            session.beginTransaction();
                            try {

                              foo.addTarget(getGUI(), getTarget());
                              targets.put(new Integer(table_event.getColumns()[0].getVariable().toString()),
                                  (TargetInterface) getGUI().getCanonicalInstance(foo));

                              session.getTransaction().commit();
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
              }
            }
            session.getTransaction().commit();
            
          } catch (final Exception ex) {
            log.error("Exception", ex);
            session.getTransaction().rollback();
          }
        }
      }
    }

    final java.util.List<TableEvent> interfaces = querier.getInterfaces();

    synchronized (getGUI().getSynchro()) {
      synchronized (getGUI().sync_tree) {
        final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try {

          for (final TableEvent table_event : interfaces) { // bug : en sortant de gnetwatch, une modification concurrente est apparue sur interfaces => � prot�ger par un verrou
            if (table_event == null || table_event.getColumns() == null || table_event.getColumns().length == 0 ||
                table_event.getColumns()[0] == null || table_event.getColumns()[0].getVariable() == null) continue;

            final int interface_index = table_event.getColumns()[0].getVariable().toInt();
            final TargetInterface target_interface = targets.get(interface_index);
            if (target_interface == null) continue;

            getGUI().setStatus(getGUI().getConfig().getPattern("received_snmp", querier.getAddress().toString().substring(1)));

            if (table_event.getColumns().length <= 8 || table_event.getColumns()[8] == null ||
                table_event.getColumns()[8].getVariable() == null) continue;
            final long total_bytes_received = table_event.getColumns()[8].getVariable().toLong();

            if (last_total_bytes_received.containsKey(interface_index) == false) {
              last_total_bytes_received.put(interface_index, total_bytes_received);
              last_total_bytes_received_time.put(interface_index, System.currentTimeMillis());
            }
            else if (last_total_bytes_received.get(interface_index) != total_bytes_received) {
//              target_interface.addEvent(new EventBytesReceived(total_bytes_received - last_total_bytes_received.get(interface_index)));

              final long last_time = last_total_bytes_received_time.get(interface_index);
              final long last_val = last_total_bytes_received.get(interface_index);

              final int value = (int) ((8 * 1000 * (double) (total_bytes_received - last_val)) / (System.currentTimeMillis() - last_time));
              target_interface.addEvent(new EventBytesReceived(value));

              // limitation : seulement 2 fils pour les interfaces - il faudrait chercher les fils concern�s : attention : locker
              if (target_interface.getChildren().isEmpty() == false && target_interface.getChildren().get(0).getItem().equals("ingress"))
                target_interface.getChildren().get(0).setDescription(
                    GenericTools.formatNumericString(getGUI().getConfig(), "" +
                        (int) ((8 * 1000 * (double) (total_bytes_received - last_val)) / (System.currentTimeMillis() - last_time)))
                    + " bit/s");

              if (target_interface.getChildren().size() == 2 && target_interface.getChildren().get(1).getItem().equals("ingress"))
                target_interface.getChildren().get(1).setDescription(
                    GenericTools.formatNumericString(getGUI().getConfig(), "" +
                        (int) ((8 * 1000 * (double) (total_bytes_received - last_val)) / (System.currentTimeMillis() - last_time)))
                    + " bit/s");

              last_total_bytes_received.put(interface_index, total_bytes_received);
              last_total_bytes_received_time.put(interface_index, System.currentTimeMillis());
            }

            if (table_event.getColumns().length <= 9 || table_event.getColumns()[9] == null ||
                table_event.getColumns()[9].getVariable() == null) continue;
            final long total_bytes_sent = table_event.getColumns()[9].getVariable().toLong();
            if (last_total_bytes_sent.containsKey(interface_index) == false) {
              last_total_bytes_sent.put(interface_index, total_bytes_sent);
              last_total_bytes_sent_time.put(interface_index, System.currentTimeMillis());
            }
            else if (last_total_bytes_sent.get(interface_index) != total_bytes_sent) {
//              target_interface.addEvent(new EventBytesSent(total_bytes_sent - last_total_bytes_sent.get(interface_index)));

              final long last_time = last_total_bytes_sent_time.get(interface_index);
              final long last_val = last_total_bytes_sent.get(interface_index);

              final int value = (int) ((8 * 1000 * (double) (total_bytes_sent - last_val)) / (System.currentTimeMillis() - last_time));
              target_interface.addEvent(new EventBytesSent(value));

              if (target_interface.getChildren().isEmpty() == false && target_interface.getChildren().get(0).getItem().equals("egress"))
                target_interface.getChildren().get(0).setDescription(
                    GenericTools.formatNumericString(getGUI().getConfig(), "" +
                        (int) ((8 * 1000 * (double) (total_bytes_sent - last_val)) / (System.currentTimeMillis() - last_time)))
                    + " bit/s");
              if (target_interface.getChildren().size() == 2 && target_interface.getChildren().get(1).getItem().equals("egress"))
                target_interface.getChildren().get(1).setDescription(
                    GenericTools.formatNumericString(getGUI().getConfig(), "" +
                        (int) ((8 * 1000 * (double) (total_bytes_sent - last_val)) / (System.currentTimeMillis() - last_time)))
                    + " bit/s");

              last_total_bytes_sent.put(interface_index, total_bytes_sent);
              last_total_bytes_sent_time.put(interface_index, System.currentTimeMillis());
            }
          }

          session.getTransaction().commit();
        } catch (final Exception ex) {
          log.error("Exception", ex);
          session.getTransaction().rollback();
        }
      }
    }
  }

  /**
   * Called when this element is being removed.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    // remove us from the queue
    super.disposed();

    // interrupt if currently running
    interrupt(InterruptCause.removed);
  }
}