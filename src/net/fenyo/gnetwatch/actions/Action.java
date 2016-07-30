
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

import net.fenyo.gnetwatch.GeneralException;
import net.fenyo.gnetwatch.activities.Background;
import net.fenyo.gnetwatch.targets.*;
import net.fenyo.gnetwatch.GUI.*;

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action is the base class for any action: ActionFlood, ActionPing, ActionSNMP, etc.
 * An action applies an operation to a target and adds events to this target depending
 * on the result of the operation.
 * @author Alexandre Fenyo
 * @version $Id: Action.java,v 1.24 2008/04/15 23:58:18 fenyo Exp $
 */

public class Action extends VisualElement {
  private static Log log = LogFactory.getLog(Action.class);

  // GUI & Queue threads
  // supports any thread
  // persistent
  private Target target;

  private Background background;

  public enum InterruptCause { timeout, exiting, removed }; 

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  protected Action(final Target target, final Background background) {
    this.target = target;
    this.background = background;
    setType("action");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  protected Action() {
    setType("action");
    target = null;
    background = null;
  }

  /**
   * Sets the target.
   * @param target target.
   * @return void.
   */
  public void setTarget(final Target target) {
    this.target = target;
  }

  /**
   * Sets the background manager.
   * @param background background manager.
   * @return void.
   */
  public void setBackground(final Background background) {
    this.background = background;
  }

  public Background getBackground() {
    return background;
  }

  /**
   * Called to inform about the current GUI.
   * @param gui current GUI instance.
   * @return void.
   */
  protected void initialize(final GUI gui) {
    super.initialize(gui);
    if (gui != null) setImageExec();
  }

  /**
   * Returns the associated target.
   * @param none.
   * @return Target associated target.
   */
  // Queue thread
  public Target getTarget() {
    return target;
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "standard";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  public long getMaxDelay() {
    return 0;
  }

  /**
   * Asks this action to stop rapidely.
   * @param cause cause.
   * @return void.
   * @throws IOException IO exception.
   */
  // main & Background threads
  public void interrupt(final InterruptCause cause) throws IOException {}

  /**
   * Asks this action to do its job.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  // Queue thread
  public void invoke() throws IOException, InterruptedException {}

  /**
   * Checks that another visual element type can be under this one.
   * @param visual_element element to check against.
   * @return boolean true if this element can be under this action.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    return false;
  }

  /**
   * Called when this element is being removed.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    super.disposed();
    try {
// PB : this.invoke() pourra qd meme etre invoqu� 1 fois ou peut meme etre en cours d'invocation
      background.removeActionQueue(this);
    } catch (final GeneralException ex) {
      log.error("Exception", ex);
    }
  }
}
