
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

import java.util.*;
import java.io.*;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Queue is the base class for any queue.
 * It implements the main queueing process.
 * @author Alexandre Fenyo
 * @version $Id: Queue.java,v 1.27 2008/04/28 23:00:57 fenyo Exp $
 */

public abstract class Queue extends VisualElement implements Runnable {
  private static Log log = LogFactory.getLog(Queue.class);

  private final String name;
  private final Config config;
  private final Thread thread;
  private long max_time = 0;

  // many threads access to actions
  private final List<Action> actions =
    Collections.synchronizedList(new LinkedList<Action>());
  
  // only the Queue thread accesses to localActions
  private Vector<Action> actions_copy = null;

  private Action current_action = null;

  /**
   * Constructor.
   * @param name queue name.
   * @param config configuration.
   */
  // main thread
  protected Queue(final String name, final Config config) {
    this.name = name;
    this.config = config;
    setType("task queue");
    setItem(name);
    setDescription("iterative queue");
    thread = new Thread(this, name);
    thread.start();
  }

  /**
   * Sets the current GUI instance.
   * @param GUI current GUI instance.
   * @return void.
   */
  // final because called by constructor (by means of setItem())
  protected final void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageQueue();
  }

  /**
   * Returns the size of the queue.
   * @param none.
   * @return int size.
   */
  public int size() {
    return actions.size();
  }

  /**
   * Adds a new action to this queue.
   * @param action new action to add.
   * @return void.
   */
  // GUI thread
  public void addAction(final Action action) {
    actions.add(action);
  }

  /**
   * Removes an action from this queue.
   * @param action action to remove.
   * @return void.
   */
  // GUI thread
  public void removeAction(final Action action) {
    actions.remove(action);
  }

  /**
   * Called after each cycle.
   * @param none.
   * @return void.
   */
  // Queue thread
  protected void informCycle() {}

  public void inform(final Object obj) {}

  /**
   * Returns the time to wait after each cycle.
   * @param none.
   * @return int time to wait.
   */
  // Queue thread
  abstract protected int getCycleDelay();

  /**
   * Returns the time to wait between empty cycles.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  abstract protected int getEmptyCycleDelay();

  /**
   * Returns the time to wait between two actions.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  abstract protected int getActionDelay();

  /**
   * Loops among the actions currently in this queue.
   * @param none.
   * @return void.
   */
  // Queue thread
  public void run() {
    int nactions = 0;
    int ncurrent = 0;

    if (actions_copy == null || actions_copy.isEmpty())
      synchronized (actions) {
        actions_copy = new Vector<Action>(actions);
        nactions = actions_copy.size();
        ncurrent = 0;
        setProgress(0);
      }

    while (!config.isEnd()) {
      if (!actions_copy.isEmpty()) {
        Action action = actions_copy.get(0);
        actions_copy.remove(0);

        if (nactions != 0) setProgress(Math.min(100 * ++ncurrent / nactions, 100));

        try {
          synchronized (this) {
            max_time = new Date().getTime() + action.getMaxDelay();
            current_action = action;
          }

          // this thread may be interrupted by the Background thread only from here ... 
          action.invoke();

        } catch (final IOException ex) {
          log.warn("Exception", ex);
        } catch (final InterruptedException ex) {
        } finally {
          synchronized (this) {
            max_time = 0;
            current_action = null;
            // ... to there
          }
        }
      }

      if (actions_copy.isEmpty()) {
        informCycle();
        synchronized (actions) {
          actions_copy = new Vector<Action>(actions);
          nactions = actions_copy.size();
          ncurrent = 0;
          setProgress(0);
        }
        try {
          Thread.sleep(actions_copy.size() != 0 ? getCycleDelay() : getEmptyCycleDelay());
        } catch (final InterruptedException ex) {
          // each time a timeout occurs, the background thread interrupts this thread
        }
      }

      try {
        Thread.sleep(getActionDelay());
      } catch (final InterruptedException ex) {
        // each time a timeout occurs, the background thread interrupts this thread
      }
    }
  }

  /**
   * Checks that the timeout occured.
   * @param none.
   * @return boolean true if timeout occured.
   */
  // Background thread
  public synchronized boolean isExhausted() {
    return max_time == 0 ? false : new Date().getTime() > max_time;
  }

  /**
   * Stops this queue thread.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // main thread
  public void end() throws InterruptedException {
    try {
      interrupt(Action.InterruptCause.exiting);
    }
    catch (final IOException ex) {
      log.warn("Exception", ex);
    }

    thread.join();
  }

  /**
   * Stops this queue thread.
   * @param cause cause.
   * @return void.
   * @throws InterruptedException exception.
   */
  // main & Background thread
  public void interrupt(final Action.InterruptCause cause) throws IOException {
    if (thread != null) {
      thread.interrupt();
      synchronized (this) {
        if (current_action != null) current_action.interrupt(cause);
      }
    }
  }

  /**
   * Detaches this queue from the selected parent.
   * @param visual_parent selected parent.
   * @return void.
   */
  public void removeVisualElements(final VisualElement visual_parent) {
    return;
  }

  protected Config getConfig() {
    return config;
  }
}
