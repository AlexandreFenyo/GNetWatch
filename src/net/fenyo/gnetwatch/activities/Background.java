
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

package net.fenyo.gnetwatch.activities;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.VisualElement;
import net.fenyo.gnetwatch.actions.Action;
import net.fenyo.gnetwatch.targets.Target;

import java.util.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

// every queues must be created at startup, no addition after.

/**
 * This class creates queues at startup and manages them through a background thread:
 * long active actions in a queue are interrupted after a timeout.
 * @author Alexandre Fenyo
 * @version $Id: Background.java,v 1.28 2008/08/03 20:56:03 fenyo Exp $
 */

public class Background implements Runnable {
  private static Log log = LogFactory.getLog(Background.class);

  private final Config config;

  private Thread interrupt_thread = null;

  // gérer des queues (loops) qui sont chacune associée à un thread particulier,
  // le nombre de loops est dynamique
  // many threads access to queues
  private final Map<String, Queue> queues =
    Collections.synchronizedMap(new HashMap<String, Queue>());

  /**
   * Constructor.
   * main thread.
   * @param config configuration.
   */
  public Background(final Config config) {
    this.config = config;

    // create default queues
//    queues.put("standard", new Queue("standard", config));
//    queues.put("debug", new DebugQueue("debug", config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.icmp")); cnt++)
      queues.put("icmp-" + cnt, new PingQueue("icmp-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.process")); cnt++)
      queues.put("process-" + cnt, new GenericProcessQueue("process-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.source")); cnt++)
      queues.put("source-" + cnt, new GenericProcessQueue("source-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.snmp")); cnt++)
      queues.put("snmp-" + cnt, new SNMPQueue("snmp-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.flood")); cnt++)
      queues.put("flood-" + cnt, new FloodQueue("flood-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.http")); cnt++)
      queues.put("http-" + cnt, new HTTPQueue("http-" + cnt, config));
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count.nmap")); cnt++)
      queues.put("nmap-" + cnt, new NmapQueue("nmap-" + cnt, config));
    queues.put("merge-1", new MergeQueue("merge-1", config));
    // never add nor remove queues after this constructor returns,
    // since we may iterate on queues in run() 
  }

  public void informQueue(final String name, final Object obj) {
    queues.get(name).inform(obj);
  }

  public MergeQueue getMergeQueue() {
    return (MergeQueue) queues.get("merge-1");
  }

  /**
   * Terminates background threads.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // main thread
  public void end() throws InterruptedException {
    // terminate each background queue thread
    for (final Queue queue : queues.values()) queue.end();

    // terminate the background interrupt thread
    interrupt_thread.interrupt();
    interrupt_thread.join();
  }

  /**
   * Returns the list of background queues.
   * @param none.
   * @return Map<String, Queue> list of queues.
   */
  // main & GUI thread
  public Map<String, Queue> getQueues() {
    return queues;
  }

  /**
   * Adds an action to a running queue.
   * @param action action to add.
   * @return void.
   * @throws GeneralException queue does not exist.
   */
  // GUI thread
  public void addActionQueue(final Action action) throws GeneralException {
    if (!queues.containsKey(action.getQueueName() + "-1"))
      throw new GeneralException("queue does not exist");

    int smallest_queue = 1;
    int smallest_size = queues.get(action.getQueueName() + "-1").size();
    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count." + action.getQueueName())); cnt++) {
      if (queues.get(action.getQueueName() + "-" + cnt).size() < smallest_size)
        smallest_queue = cnt;
    }
    queues.get(action.getQueueName() + "-" + smallest_queue).addAction(action);
  }

  /**
   * Removes an action from a running queue.
   * @param action action to remove.
   * @return void.
   * @throws GeneralException queue does not exist.
   */
  // GUI thread
  public void removeActionQueue(final Action action) throws GeneralException {
    if (!queues.containsKey(action.getQueueName() + "-1"))
      throw new GeneralException("queue does not exist");

    for (int cnt = 1; cnt <= new Integer(config.getProperty("queues.count." + action.getQueueName())); cnt++)
      queues.get(action.getQueueName() + "-" + cnt).removeAction(action);
  }

  /**
   * Starts the background thread.
   * @param none.
   * @return void.
   */
  // main thread
  public void createBackgroundThread() {
    interrupt_thread = new Thread(this, "Interrupt Thread");
    interrupt_thread.start();
  }

  /**
   * Interrupts long actions.
   * @param none.
   * @return void.
   */
  // Background thread
  public void run() {
    while (!config.isEnd())
      try {
        Thread.sleep(1000);
        for (final Queue queue : queues.values())
          if (queue.isExhausted()) {
            queue.interrupt(Action.InterruptCause.timeout);
          }
      } catch (final InterruptedException ex) {
        // this thread is interrupted when the application is terminated
      } catch (final IOException ex) {
        log.warn("Exception", ex);
      }
  }
}
