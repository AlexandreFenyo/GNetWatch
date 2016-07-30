
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
import net.fenyo.gnetwatch.activities.Background;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * Instances of this action class can flood their target with UDP
 * and create events of type EventFlood to log the throughput.
 * @author Alexandre Fenyo
 * @version $Id: ActionFlood.java,v 1.27 2008/05/25 22:57:39 fenyo Exp $
 */

public class ActionFlood extends Action {
  private static Log log = LogFactory.getLog(ActionPing.class);

  private boolean interrupted = false;

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionFlood(final Target target, final Background background) {
    super(target, background);
// localiser
    setItem("flood");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionFlood() {
    setItem("flood");
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "flood";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  // bug : au bout de ce tps en ms ca s'arrete
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
   * Floods the target.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  public void invoke() throws IOException, InterruptedException {
    if (isDisposed() == true) return;

    try {
      super.invoke();

      // il faudrait copier les queriers à la création de l'action
      final IPQuerier querier;

      DatagramSocket socket;
      final byte [] buf;
      final DatagramPacket packet;
      // on invoque un champ persistent depuis potentiellement un thread autre que celui qu gère une session qui rend cet objet persistent, on doit éviter cet accès concurrent (les sessions ne sont pas thread safe)
      synchronized (getGUI().getSynchro()) {
        if (TargetIPv4.class.isInstance(getTarget())) {
          querier = ((TargetIPv4) getTarget()).getIPQuerier();
        } else if (TargetIPv6.class.isInstance(getTarget())) {
          querier = ((TargetIPv6) getTarget()).getIPQuerier();
        } else return;

      socket = new DatagramSocket(querier.getPortSrc());
      socket.setTrafficClass(querier.getTos() << 2);
      socket.setBroadcast(true);

      buf = new byte [querier.getPDUMaxSize()];
      Arrays.fill(buf, (byte) 0);
      packet = new DatagramPacket(buf, buf.length,
          new InetSocketAddress(querier.getAddress(), querier.getPortDst()));
      }

      long last_time = System.currentTimeMillis();
      int bytes_sent = 0;
      while (true) {
        socket.send(packet);
        bytes_sent += buf.length;

          if (System.currentTimeMillis() - last_time > 1000) {

          synchronized (getGUI().getSynchro()) {
            synchronized (getGUI().sync_tree) {
              final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
              session.beginTransaction();
              try {
                session.update(this);

                getTarget().addEvent(new EventFlood(new Double(((double) 8 * 1000 * bytes_sent) / (System.currentTimeMillis() - last_time)).intValue()));

                setDescription(GenericTools.formatNumericString(getGUI().getConfig(), "" + new Double(((double) 8 * 1000 * bytes_sent) / (System.currentTimeMillis() - last_time)).intValue()) + " bit/s");

                session.getTransaction().commit();
                } catch (final Exception ex) {
                  log.error("Exception", ex);
                  session.getTransaction().rollback();
                }
              }
          }

          synchronized (getGUI().getSynchro()) {
            getGUI().setStatus(getGUI().getConfig().getPattern("bytes_flooded", bytes_sent, querier.getAddress().toString().substring(1)));
          }

          last_time = System.currentTimeMillis();
          bytes_sent = 0;
        }


        if (interrupted == true) {
          socket.close();
          return;
        }
      }
    } catch (final InterruptedException ex) {}
  }

  /**
   * Called when this element is being removed.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    // remove us from the flood queue
    super.disposed();

    // interrupt if currently running
    interrupt(InterruptCause.removed);
  }
}
