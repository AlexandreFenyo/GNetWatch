
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.hibernate.Session;

/**
 * Instances of this action class use NMAP to explore their target
 * and create events of type EventReachable.
 * @author Alexandre Fenyo
 * @version $Id: ActionNmap.java,v 1.14 2008/04/27 21:44:21 fenyo Exp $
 */

// on devrait utiliser l'option -oX file pour traiter l'output en XML...

public class ActionNmap extends Action {
  private static Log log = LogFactory.getLog(ActionNmap.class);

  // main, Background & Queue threads
  // supports any thread
  private ExternalCommand cmd_nmap = null;

  private String address = "";
  private boolean invoked = false;

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionNmap(final Target target, final Background background) {
    super(target, background);
    setItem("nmap");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionNmap() {
    setItem("nmap");
  }

  /**
   * Called to inform about the current GUI.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "nmap";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  public long getMaxDelay() {
    return new Long(getGUI().getConfig().getProperty("nmap.timeout"));
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
    if (cmd_nmap != null) {
      cmd_nmap.end();
      if (cause == InterruptCause.timeout)
        getGUI().appendConsole(getGUI().getConfig().getPattern("nmap_interrupted", address) + "<BR/>");
    }
  }

  /**
   * Computes the signature of the target.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  // lock survey: Queue thread
  // ATTENTION : que se passe t il si on enleve la target mais que cette action s'effectue après ???
  // à vérifier pour toutes les actions (peut etre faut il yn synchronized() ?
  public void invoke() throws IOException, InterruptedException {
    if (isDisposed() == true) return;

    super.invoke();

    if (invoked == true) return;
    invoked = true;

    try {
      final String [] cmdLine;

      synchronized (getGUI().getSynchro()) {
        if (TargetIPv4.class.isInstance(getTarget())) {
          final TargetIPv4 target = (TargetIPv4) getTarget();
          address = target.getAddress().toString();
          address = address.substring(1 + address.indexOf('/'));
          cmdLine = new String [] { "nmap", "-A", address };
        } else if (TargetIPv6.class.isInstance(getTarget())) {
          final TargetIPv6 target = (TargetIPv6) getTarget();
          address = target.getAddress().toString();
          address = address.substring(1 + address.indexOf('/'));
          cmdLine = new String [] { "nmap", "-6", "-A", address };
        } else return;
      }

      String cmd_line = "";
      for (final String part : cmdLine) cmd_line += part + " ";
      getGUI().setStatus(getGUI().getConfig().getPattern("forking", cmd_line));

      cmd_nmap = new ExternalCommand(cmdLine, true);
      try {
        cmd_nmap.fork();
      } catch (final IOException ex) {
        // on laisse le ifneeded car pas de synchronized() à l'intérieur
        getGUI().asyncExecIfNeeded(new Runnable() {
          public void run() {
            final MessageBox dialog = new MessageBox(getGUI().getShell(), SWT.ICON_ERROR | SWT.OK);
            dialog.setText(getGUI().getConfig().getString("nmap_error"));
            dialog.setMessage(getGUI().getConfig().getString("nmap_error_long"));
            dialog.open();
          }
        });

        throw ex;
      }

      final String cmd_output = cmd_nmap.readStdout();
      cmd_nmap.end();

      final Matcher match = Pattern.compile("(\r|\n)Running: (.*)").matcher(cmd_output);
      if (match.find() == true) if (match.group(2) != null)
        getParents().get(0).setType(match.group(2));

      synchronized (getGUI().getSynchro()) {
        synchronized (getGUI().sync_tree) {
          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
          session.beginTransaction();
          try {
            getTarget().addEvent(new EventNmap(cmd_output));
            session.getTransaction().commit();
          } catch (final Exception ex) {
            log.error("Exception", ex);
            session.getTransaction().rollback();
          }
        }
      }

    } finally {

      // comme il y a des synchronized, on fait pas de asyncExecIfNeeded mais un asyncExec
      // pkoi ce asyncexec ?
      getGUI().asyncExec(new Runnable() {
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
      });
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