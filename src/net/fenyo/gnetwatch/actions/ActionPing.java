
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
import net.fenyo.gnetwatch.GUI.GUI;
import net.fenyo.gnetwatch.actions.Action.InterruptCause;
import net.fenyo.gnetwatch.activities.Background;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * Instances of this action class can send/receive ICMP echo messages to their target
 * and create events of type EventReachable.
 * @author Alexandre Fenyo
 * @version $Id: ActionPing.java,v 1.46 2008/08/06 16:51:42 fenyo Exp $
 */

public class ActionPing extends Action {
  private static Log log = LogFactory.getLog(ActionPing.class);

  // main, Background & Queue threads
  // supports any thread
  private ExternalCommand cmd_ping = null;

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionPing(final Target target, final Background background) {
    super(target, background);
    setItem("ping");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionPing() {
    setItem("ping");
  }

  /**
   * Called to inform about the current GUI.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageWatch();
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "icmp";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  public long getMaxDelay() {
    return 4000;
  }

  /**
   * Asks this action to stop rapidely.
   * @param cause cause.
   * @return void.
   * @throws IOException IO exception.
   */
  // main & Background threads
  // supports any thread
  public void interrupt(final InterruptCause cause) throws IOException {
    if (cmd_ping != null) cmd_ping.end();
  }

  /**
   * Computes the RTT to the target.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  // Queue thread
  // supports any thread
  public void invoke() throws IOException, InterruptedException {
    if (isDisposed() == true) return;

    try {
      super.invoke();

      if (getGUI().getConfig().getDebugLevel() == 1) {
        synchronized (getGUI().getSynchro()) {
          synchronized (getGUI().sync_tree) {
            final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try {
              session.update(this);
//              log.debug("ActionPing::invoke(): starting adding a bunch of events");
              for (int i = 1; i < 50; i++)
                getTarget().addEvent(new EventReachable(true, 10));
//              log.debug("ActionPing::invoke(): FINISHED");
              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }

      } else {

        if (TargetIPv4.class.isInstance(getTarget())) {
          final TargetIPv4 target = (TargetIPv4) getTarget();
          String address = target.getAddress().toString();
          // remplacer tous les substring(1) par cette forme avec indexof
          address = address.substring(1 + address.indexOf('/'));

          final String [] cmdLine = new String [] {
              "ping", getGUI().getConfig().getProperty("ping.countparameter"), "1", address
//            "sleep", "10000"
          };
          String cmd_line = "";
          for (final String part : cmdLine) cmd_line += part + " ";
          // getGUI().setStatus("forking external command: " + cmd_line);
          getGUI().setStatus(getGUI().getConfig().getPattern("forking", cmd_line));
          cmd_ping = new ExternalCommand(cmdLine, true);
          cmd_ping.fork();

          final String cmd_output = cmd_ping.readStdout();
          final Matcher match =
            Pattern.compile(address + getGUI().getConfig().getProperty("ping.regex")).matcher(cmd_output);

          synchronized (getGUI().getSynchro()) {
            synchronized (getGUI().sync_tree) {
              final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
              session.beginTransaction();
              try {
                session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente
                if (match.find() == true) {
                  final int delay = new Integer(match.group(2)).intValue();

                  getTarget().addEvent(new EventReachable(true, new Integer(match.group(2)).intValue()));
                  getGUI().setStatus(getGUI().getConfig().getPattern("received_icmp", address));
                  setDescription("rtt: " + delay + " ms");

                } else {

                  getTarget().addEvent(new EventReachable(false));
                  getGUI().setStatus(getGUI().getConfig().getPattern("received_icmp_timeout", address));
                  setDescription(getGUI().getConfig().getString("unreachable"));
                }

                session.getTransaction().commit();
              } catch (final Exception ex) {
                log.error("Exception", ex);
                session.getTransaction().rollback();
              }
            }
          }

          cmd_ping.end();
        }

        if (TargetIPv6.class.isInstance(getTarget())) {
          final TargetIPv6 target = (TargetIPv6) getTarget();
          String address = target.getAddress().toString();
          // remplacer tous les substring(1) par cette forme avec indexof
          address = address.substring(1 + address.indexOf('/'));

          final String [] cmdLine = new String [] {
              "ping6", getGUI().getConfig().getProperty("ping.countparameter"), "1", address
//            "sleep", "10000"
          };
          String cmd_line = "";
          for (final String part : cmdLine) cmd_line += part + " ";
          // getGUI().setStatus("forking external command: " + cmd_line);
          getGUI().setStatus(getGUI().getConfig().getPattern("forking", cmd_line));
          cmd_ping = new ExternalCommand(cmdLine, true);
          cmd_ping.fork();

          final String cmd_output = cmd_ping.readStdout();
          final Matcher match =
            Pattern.compile(getGUI().getConfig().getProperty("ping.regex")).matcher(cmd_output);
          synchronized (getGUI().getSynchro()) {
            synchronized (getGUI().sync_tree) {
              final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
              session.beginTransaction();
              try {
                session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente
                                      // pour ce update, lors d'un mergeEventsSimple() : Illegal attempt to associate a collection with two open sessions
                if (match.find() == true) {
                  final int delay = new Integer(match.group(2)).intValue();

                  getTarget().addEvent(new EventReachable(true, new Integer(match.group(2)).intValue()));
                  getGUI().setStatus(getGUI().getConfig().getPattern("received_icmp", address));
                  setDescription("rtt: " + delay + " ms");

                } else {

                  getTarget().addEvent(new EventReachable(false));
                  getGUI().setStatus(getGUI().getConfig().getPattern("received_icmp_timeout", address));
                  setDescription(getGUI().getConfig().getString("unreachable"));

                }

                session.getTransaction().commit();
              } catch (final Exception ex) {
                log.error("Exception", ex);
                session.getTransaction().rollback();
              }
            }
          }

/*
          final String cmd_output = cmd_ping.readStdout();
          // pattern could by a better matcher...
          final Matcher match =
            Pattern.compile("(.|\r|\n)*:.*?([0-9]+)[^0-9]*ms(.|\r|\n)*").matcher(cmd_output);
          if (match.find() == true) {
            final int delay = new Integer(match.group(2)).intValue();

            getTarget().addEvent(new EventReachable(true, new Integer(match.group(2)).intValue()));
            // mettre un setstatus comme pour IPv4
            setDescription("rtt: " + delay + " ms");

          } else {

            getTarget().addEvent(new EventReachable(false));
            // mettre un setstatus comme pour IPv4
            setDescription(getGUI().getConfig().getString("unreachable"));

          }
*/

          cmd_ping.end();
        }
      }
    } catch (final InterruptedException ex) {

      synchronized (getGUI().getSynchro()) {
        synchronized (getGUI().sync_tree) {
          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
          session.beginTransaction();
          try {
            session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente

            getTarget().addEvent(new EventReachable(false));
            // mettre un setstatus comme pour IPv4
            setDescription(getGUI().getConfig().getString("unreachable"));

            session.getTransaction().commit();
          } catch (final Exception ex2) {
            log.error("Exception", ex2);
            session.getTransaction().rollback();
          }
        }
      }

      throw ex;
    }
  }

  /**
   * Called when this element is being removed.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    // remove us from the action queue
    super.disposed();

    // interrupt if currently running
    try {
      interrupt(InterruptCause.removed);
    } catch (final IOException ex) {
      log.error("Exception", ex);
    }
  }
}
