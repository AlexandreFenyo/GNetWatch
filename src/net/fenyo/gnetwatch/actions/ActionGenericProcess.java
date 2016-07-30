
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
 * Instances of this action class can spawn external processes
 * and create events of type EventGenericProcess.
 * @author Alexandre Fenyo
 * @version $Id: ActionGenericProcess.java,v 1.6 2008/05/26 19:13:18 fenyo Exp $
 */

public class ActionGenericProcess extends Action {
  private static Log log = LogFactory.getLog(ActionGenericProcess.class);

  // main, Background & Queue threads
  // supports any thread
  private ExternalCommand cmd_generic = null;

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionGenericProcess(final Target target, final Background background) {
    super(target, background);
    setItem("external process");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionGenericProcess() {
    setItem("external process");
  }

  /**
   * Called to inform about the current GUI.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setItem(((TargetGroup) getTarget()).getGenericQuerier().getTitle());
    if (gui != null) setImageExec();
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "process";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  public long getMaxDelay() {
    return 30000;
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
    if (cmd_generic != null) cmd_generic.end();
  }

  /**
   * Get data from the external process.
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

      if (TargetGroup.class.isInstance(getTarget())) {

        GenericQuerier querier;
        synchronized (getGUI().getSynchro()) {
          querier = (GenericQuerier) ((TargetGroup) getTarget()).getGenericQuerier().clone();
        }

        final String [] cmdLine = querier.getCommandLine().split(" ");
        getGUI().setStatus(getGUI().getConfig().getPattern("forking", querier.getCommandLine()));
        if (querier.getWorkingDirectory().equals(""))
          cmd_generic = new ExternalCommand(cmdLine, false, new File(".").getCanonicalPath());
        else cmd_generic = new ExternalCommand(cmdLine, false, querier.getWorkingDirectory());
        cmd_generic.fork();

        final String cmd_output = cmd_generic.readStdout();
        final Matcher match =
          Pattern.compile("([0-9]+)").matcher(cmd_output);
        synchronized (getGUI().getSynchro()) {
          synchronized (getGUI().sync_tree) {
            final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try {
              session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente
              if (match.find()) {
                final int value = new Integer(match.group(1)).intValue();
                getTarget().addEvent(new EventGenericProcess(true, value));
                getGUI().setStatus(getGUI().getConfig().getPattern("process_returned", GenericTools.formatNumericString(getGUI().getConfig(), new Integer(value).toString())));
                setDescription("" + GenericTools.formatNumericString(getGUI().getConfig(), new Integer(value).toString()) + " " + querier.getUnit());

              } else {

                // getTarget().addEvent(new EventGenericProcess(false));
                getGUI().setStatus(getGUI().getConfig().getString("process_returned_nothing"));
                setDescription(getGUI().getConfig().getString("unparsable"));
                log.error("bad output: [" + cmd_output + "]");
              }

              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }

        cmd_generic.end();
      }

    } catch (final InterruptedException ex) {

      synchronized (getGUI().getSynchro()) {
        synchronized (getGUI().sync_tree) {
          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
          session.beginTransaction();
          try {
            session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente

            getTarget().addEvent(new EventGenericProcess(false));
            // mettre un setstatus comme pour IPv4
//            setDescription(getGUI().getConfig().getString("unreachable"));

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
