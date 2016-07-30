
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
 * Instances of this action class can connect to external sources (by reading files)
 * and create events of type EventGenericSrc.
 * @author Alexandre Fenyo
 * @version $Id: ActionGenericSrc.java,v 1.6 2008/10/31 19:00:20 fenyo Exp $
 */

public class ActionGenericSrc extends Action {
  private static Log log = LogFactory.getLog(ActionGenericSrc.class);

  private long last_file_size = -1;

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
  public ActionGenericSrc(final Target target, final Background background) {
    super(target, background);
    setItem("external source");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionGenericSrc() {
    setItem("external source");
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
    return "source";
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
   * Get data from the external source.
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

        getGUI().setStatus(getGUI().getConfig().getPattern("reading_file", querier.getFileName()));

        if (last_file_size == -1) {
          last_file_size = new File(querier.getFileName()).length();
          return;
        }

        // lire le contenu du fichier
        int value = -10;
        int value1 = -10;
        int value2 = -10;
        int value3 = -10;
        int value4 = -10;
        int value5 = -10;
        int value6 = -10;
        int value7 = -10;
        int value8 = -10;
        int value9 = -10;
        int value10 = -10;
        String units = "";
        boolean value_ok = false;

        final File file = new File(querier.getFileName());
        final long new_file_size = file.length();
        if (last_file_size == new_file_size) return;

        last_file_size = new_file_size;

        FileInputStream byte_stream = null;
        try {
          byte_stream = new FileInputStream(file);
          byte_stream.skip(Math.max(0, last_file_size - 300));
          // we want each char having 1 byte length, so we choose US-ASCII character encoding
          final InputStreamReader char_stream = new InputStreamReader(byte_stream, "US-ASCII");
          final char ctab [] = new char [300];
          final int nchars = char_stream.read(ctab, 0, 300);

          if (nchars > 0) {
            Matcher match =
              Pattern.compile("<([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([0-9-]*);([^>]*)>.*?$").matcher(new String(ctab, 0, nchars));
            if (match.find()) {
              value1 = new Integer(match.group(1));
              value2 = new Integer(match.group(2));
              value3 = new Integer(match.group(3));
              value4 = new Integer(match.group(4));
              value5 = new Integer(match.group(5));
              value6 = new Integer(match.group(6));
              value7 = new Integer(match.group(7));
              value8 = new Integer(match.group(8));
              value9 = new Integer(match.group(9));
              value10 = new Integer(match.group(10));
              units = match.group(11);
              value_ok = true;
            } else {
              match =
                Pattern.compile("([0-9]+).*?$").matcher(new String(ctab, 0, nchars));
              if (match.find()) {
                value = new Integer(match.group(1));
                value_ok = true;
              }
            }
          }

        } finally {
          if (byte_stream != null) byte_stream.close();
        }

        synchronized (getGUI().getSynchro()) {
          synchronized (getGUI().sync_tree) {
            final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try {
              session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente
              if (value_ok) {
                getTarget().addEvent(new EventGenericSrc(true, value, value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, units));
                getGUI().setStatus(getGUI().getConfig().getPattern("new_file_value", GenericTools.formatNumericString(getGUI().getConfig(), new Integer(value < 0 ? value1 : value).toString())));
                setDescription("" + GenericTools.formatNumericString(getGUI().getConfig(), new Integer(value < 0 ? value1 : value).toString()) + " " + querier.getUnit());

              } else {

                // getTarget().addEvent(new EventGenericSrc(false));
                getGUI().setStatus(getGUI().getConfig().getString("new_file_value_incorrect"));
                setDescription(getGUI().getConfig().getString("new_file_value_incorrect"));
              }

              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }
      }

    } catch (final InterruptedException ex) {

      synchronized (getGUI().getSynchro()) {
        synchronized (getGUI().sync_tree) {
          final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
          session.beginTransaction();
          try {
            session.update(this); // pour le setDescription() => à enlever qd la description ne sera plus persistente

            getTarget().addEvent(new EventGenericSrc(false));
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
