
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

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.*;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

  // memory usage: see jconsole, jps and '-Dcom.sun.management.jmxremote -XX:+AggressiveHeap'

public class MergeQueue extends Queue implements Runnable {
  private static Log log = LogFactory.getLog(MergeQueue.class);

  /**
   * Constructor.
   * @param name queue name.
   * @param config configuration.
   */
  // main thread
  public MergeQueue(final String name, final Config config) {
    super(name, config);
    setDescription(config.getString("dedicated_for_merging"));
  }

  /**
   * Returns the time to wait after each cycle.
   * @param none.
   * @return int time to wait.
   */
  // Queue thread
  protected int getCycleDelay() {
    return 0;
  }

  /**
   * Returns the time to wait between empty cycles.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  protected int getEmptyCycleDelay() {
    return 60 * 60 * 1000; // every one hour
//    return 5000;
  }

  /**
   * Returns the time to wait between two actions.
   * @param none.
   * @return time to wait.
   */
  // Queue thread
  protected int getActionDelay() {
    return 0;
  }

  private void mergeEventsChunk(final List<EventGeneric> events, final java.util.Date date) {
    final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
    if (events.size() < 2) return;
    double sum = 0D;
    int cnt = 0;
    boolean neg = true;
    boolean first = true;
    for (final EventGeneric event : events) {
      if (event.getIntValue() >= 0) {
        neg = false;
        cnt++;
        sum += event.getIntValue();
      }
      if (first == false) session.delete(event);
      first = false;
    }

    events.get(0).setDate(new java.sql.Date(date.getTime()));

    if (neg == true) events.get(0).setIntValue(-1);
    else events.get(0).setIntValue((int) (sum / cnt));
  }

  final void mergeEventsOfTarget(final Target target, final String event_type, final Long s, final EventGeneric ev_first) {
    if (ev_first == null) return;

    final org.hibernate.Query query =
      getGUI().getSynchro().getSessionFactory().getCurrentSession().createQuery("from EventGeneric as ev " +
        "where ev.eventList = :event_list " +
        "and ev.date < :stop_date " +
        "and ev.merged != :s order by ev.date asc")
        .setString("event_list", target.getEventLists().get(event_type).getId().toString())
        .setLong("s", s)
        .setString("stop_date", ev_first.getDate().toString());
//    log.debug("stop date : " + ev_first.getDate().toString());
    final List<EventGeneric> results = query.list();
    if (results.size() > 0) {
      EventGeneric prev_event = null;
      final ArrayList<EventGeneric> events = new ArrayList<EventGeneric>();
      for (final EventGeneric event : results) {
        event.setMerged(s);
        if (prev_event != null) {
          if (event.getDate().getTime() - event.getDate().getTime() % (s * 1000) ==
            prev_event.getDate().getTime() - prev_event.getDate().getTime() % (s * 1000)) {
            if (!events.contains(prev_event)) events.add(prev_event);
            if (!events.contains(event)) events.add(event);
          } else {
            if (events.size() > 0)
              mergeEventsChunk(events, new java.util.Date(events.get(0).getDate().getTime() - events.get(0).getDate().getTime() % (s * 1000) + s * 500));
            events.clear();
            events.add(event);
          }
        }
        prev_event = event;
      }
      if (events.size() > 0)
        mergeEventsChunk(events, new java.util.Date(events.get(0).getDate().getTime() - events.get(0).getDate().getTime() % (s * 1000) + s * 500));
      events.clear();
    }
  }

  final EventGeneric getEventByIndex(final int index, final Long targetId) {
    final org.hibernate.Query query =
      getGUI().getSynchro().getSessionFactory().getCurrentSession().createQuery("from EventGeneric as ev " +
    "where ev.eventList = :event_list order by ev.date desc")
    .setString("event_list", targetId.toString())
    .setFirstResult(index - 1).setMaxResults(1);
    return (query.list().size() > 0) ? (EventGeneric) query.uniqueResult() : null;
  }

  private void mergeEvents() {
    // should be localized
    final long start_time = System.currentTimeMillis();
    getGUI().appendConsole("<HR><B>Merging old events</B><BR/>start time: " + new Date(start_time) + "<BR/>");

    org.hibernate.Query query;
    synchronized (getGUI().getSynchro()) {
      final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
      session.beginTransaction();

      try  {
        for (final VisualElement elt : getGUI().getVisualTransient().getSubElements(Target.class)) {
          final Target target = (Target) elt;
          session.update(target);
          for (final String event_type : target.getEventLists().keySet()) {
            final Integer count = (Integer) session.createQuery("select count(*) from EventGeneric ev " +
                "where ev.eventList = :event_list")
                .setString("event_list", target.getEventLists().get(event_type).getId().toString()).uniqueResult();

            // merge old events
            if (count > new Integer(getConfig().getProperty("events.merge.threshold.0"))) {
              int idx = 1;
              while (getConfig().getProperty("events.merge.delay." + idx) != null) {
                mergeEventsOfTarget(target, event_type, new Long(getConfig().getProperty("events.merge.delay." + idx)),
                    getEventByIndex(new Integer(getConfig().getProperty("events.merge.threshold." + (idx - 1))),
                        target.getEventLists().get(event_type).getId()));
                idx++;
              }

              // remove very old events
              final EventGeneric ev =
                getEventByIndex(new Integer(getConfig().getProperty("events.merge.threshold." + (idx - 1))),
                    target.getEventLists().get(event_type).getId());
              if (ev != null) {
                query = session.createQuery("delete EventGeneric as ev " +
                "where ev.eventList = :event_list and ev.date < :stop_date")
                .setString("event_list", target.getEventLists().get(event_type).getId().toString())
                .setString("stop_date", ev.getDate().toString());
                query.executeUpdate();
              }
            }
          }
        }

        session.getTransaction().commit();
      } catch (final Exception ex) {
        log.error("Exception", ex);
        session.getTransaction().rollback();
      }
    }

    // should be localized
    getGUI().appendConsole("finished in " + (System.currentTimeMillis() - start_time) / 1000 + " seconds");
  }

  // synchronized : protéger l'accès à setGUI()
  public synchronized void inform(final Object obj) {
    setGUI((GUI) obj);
  }

  // synchronized : protéger l'accès à getGUI()
  public synchronized void informCycle() {
    while (getGUI() == null) {
      try {
        Thread.sleep(1000);
      } catch (final InterruptedException ex) {
        log.warn("Exception", ex);
      }
    }

    if (getGUI() != null && getGUI().getSynchro() != null) mergeEvents();
  }
}
