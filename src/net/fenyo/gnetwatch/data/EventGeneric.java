
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

package net.fenyo.gnetwatch.data;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.targets.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This base class for every events stores the date of the event.
 * @author Alexandre Fenyo
 * @version $Id: EventGeneric.java,v 1.15 2008/08/05 16:42:08 fenyo Exp $
 */

public abstract class EventGeneric {
  private static Log log = LogFactory.getLog(EventGeneric.class);

  // persistent
  private Long id;

  // persistent
  private Date date;

  // persistent
  private Long merged;

  // persistent
  private EventList eventList;

  /**
   * Constructor.
   * @param date date of creation.
   */
  protected EventGeneric(final Date date) {
    this.date = date;
  }

  /**
   * Default constructor.
   * @param none.
   */
  // Queue thread
  public EventGeneric() {
    date = new Date(System.currentTimeMillis());
    merged = 0L;
  }

  public Long getId() {
    return id;
  }

  protected void setId(final Long id) {
    this.id = id;
  }

  public Long getMerged() {
    return merged;
  }

  public void setMerged(final Long merged) {
    this.merged = merged;
  }

  public EventList getEventList() {
    return eventList;
  }

  public void setEventList(final EventList eventList) {
    this.eventList = eventList;
  }

  /**
   * Returns an integer representation of the performance counter associated whith this event.
   * @return int performance counter.
   */
  public int getIntValue() {
    return -1;
  }

  public void setIntValue(int value) {}

  /**
   * Returns the date of creation of this event.
   * @param none.
   * @return Date date of creation.
   */
  // AWT & Queue thread
  public Date getDate() {
    return date;
  }

  public void setDate(final Date date) {
    this.date = date;
  }
}
