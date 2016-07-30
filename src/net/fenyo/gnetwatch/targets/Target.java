
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

package net.fenyo.gnetwatch.targets;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.data.*;

import org.apache.commons.collections.map.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

// import com.sun.jmx.snmp.Timestamp;

/**
 * Target is the base class for every types of targets.
 * @author Alexandre Fenyo
 * @version $Id: Target.java,v 1.77 2008/12/05 13:17:38 fenyo Exp $
 */

public class Target extends VisualElement {
  private static Log log = LogFactory.getLog(Target.class);

  // no longer final because of the setter for Hibernate
  // persistent
  /* final */ private String name; // not null

  private MultiValueMap registered_components = new MultiValueMap();

  // persistent
  public Map<String, EventList> eventLists = new HashMap<String, EventList>();

  /**
   * Constructor.
   * @param name target name.
   * @throws AlgorithmException exception.
   */
  // GUI thread
  public Target(final String name) throws AlgorithmException {
    if (name == null) throw new AlgorithmException("name is null");
    this.name = name;
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  public Target() {}

  public void setName(final String name) {
    this.name = name;
  }

  public Map<String, EventList> getEventLists() {
    return eventLists;
  }

  public void setEventLists(Map<String, EventList> eventLists) {
    this.eventLists = eventLists;
  }

  /**
   * Informs this target that this component is interested in this type of events.
   * @param component component to register.
   * @param clazz type of events.
   * @return void.
   */
  // AWT thread
  // lock survey: sync_value_per_vinterval << sync_update << HERE
  public void registerComponent(final BasicComponent component, final Class clazz) {
    synchronized (registered_components) {
      // lock survey: sync_value_per_vinterval << sync_update << registered_components << HERE
      if (!registered_components.containsValue(clazz, component))
        registered_components.put(clazz, component);
    }
  }

  /**
   * Unregister a component.
   * @param component component to unregister.
   * @param clazz type of events.
   */
  // AWT thread
  public void unregisterComponent(final BasicComponent component, final Class clazz) {
    synchronized (registered_components) {
      if (registered_components.remove(clazz, component) == null)
        log.error("component was not registered");
    }
  }

  /**
   * Returns the last event.
   * @param clazz type of events.
   * @return EventGeneric last event.
   */
  public EventGeneric getLastEvent(Class clazz) {
    EventGeneric result = null;

    final Synchro synchro = getGUI().getSynchro();
    synchronized (synchro) {
      final Session session = synchro.getSessionFactory().getCurrentSession();
      session.beginTransaction();
      try {
        session.update(this);

        // get events inside the range
        final EventList el = eventLists.get(clazz.toString());
        java.util.List results =
          session.createQuery("from EventGeneric as ev where ev.eventList = :event_list " +
          "order by ev.date desc")
        .setString("event_list", el.getId().toString())
        .list();
        if (results.size() > 0) result = (EventGeneric) results.get(0);

        session.getTransaction().commit();
      } catch (final Exception ex) {
        log.error("Exception", ex);
        session.getTransaction().rollback();
      }
    }

    return result;
  }

  /**
   * Returns events from the first BEFORE begin (or at begin) to the last AFTER end (or at end).
   * @param begin start time.
   * @param end end time.
   * @param clazz type of events.
   * @return List<EventGeneric> list of selected events.
   */
  // AWT thread
  // lock survey: synchro << sync_tree << HERE
  //              sync_value_per_vinterval << sync_update << synchro << sync_tree << HERE
  public List<EventGeneric> getEvents(final Date begin, final Date end, Class clazz) {
    final List<EventGeneric> selected_events = new LinkedList<EventGeneric>();

    final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
    session.beginTransaction();

    try {
      // get events inside the range
        // j'ai eu ça ici et plus loin : Illegal attempt to associate a collection with two open sessions
      session.update(this);
      final EventList el = eventLists.get(clazz.toString());

      java.util.List results =
        session.createQuery("from EventGeneric as ev where ev.eventList = :event_list " +
        "and ev.date >= :start_date and ev.date <= :stop_date " +
        "order by ev.date asc")
      .setString("event_list", el.getId().toString())
      .setString("start_date", new java.sql.Timestamp(begin.getTime()).toString())
      .setString("stop_date", new java.sql.Timestamp(end.getTime()).toString())
      .list();

      for (final EventGeneric event : (java.util.List<EventGeneric>) results)
        selected_events.add(event);

      // get event just before the beginning of the range
      results =
        session.createQuery("from EventGeneric as ev where ev.eventList = :event_list " +
        "and ev.date < :start_date " +
        "order by ev.date desc")
      .setString("event_list", el.getId().toString())
      .setString("start_date", new java.sql.Timestamp(begin.getTime()).toString())
      .list();
      if (results.size() >= 1)
        selected_events.add(0, (EventGeneric) results.get(0));

      // get event just after the last of the range
      results =
        session.createQuery("from EventGeneric as ev where ev.eventList = :event_list " +
        "and ev.date > :stop_date " +
        "order by ev.date asc")
      .setString("event_list", el.getId().toString())
      .setString("stop_date", new java.sql.Timestamp(end.getTime()).toString())
      .list();
      if (results.size() >= 1)
        selected_events.add((EventGeneric) results.get(0));

      session.getTransaction().commit();
      } catch (final Exception ex) {
        log.error("Exception", ex);
        session.getTransaction().rollback();
      }

    return selected_events;
  }

  /**
   * Returns the name of this target.
   * @param none.
   * @return String target name.
   */
  public String getName() {
    return name;
  }

  /**
   * Adds a new event.
   * @param event event to add.
   * @return void.
   */
  // Queue thread
  // lock survey: synchro << sync_tree << HERE  
  public void addEvent(final EventGeneric event) {
    // build a list of graphic components interested in this type of event
    final List<BasicComponent> components = new ArrayList<BasicComponent>();
    synchronized (registered_components) {
      if (registered_components.containsKey(event.getClass()))
        for (final BasicComponent component : (ArrayList<BasicComponent>)
            registered_components.getCollection(event.getClass()))
          components.add(component);
    }

    // inform graphic components about this new event
    for (final BasicComponent component : components) component.updateValues(event);

    // create a view if this is the first event of this type
    boolean found = false;
      for (final VisualElement child : getChildren())
        if (DataView.class.isInstance(child) &&
            ((DataView) child).browserEventClass().isInstance(event))
          found = true;
    if (found == false) getGUI().informTargetHasNewEventClass(this, event.getClass());

    // save this event into persistent area
    final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
    // j'ai eu ça ici et avant : Illegal attempt to associate a collection with two open sessions
    session.update(this);
    EventList el = eventLists.get(event.getClass().toString());
    if (el == null) eventLists.put(event.getClass().toString(), el = new EventList());
    event.setEventList(el);
    session.save(event);
  }

  public void addEvent(final Synchro synchro, final EventGeneric event) {
    // create a view if this is the first event of this type
    boolean found = false;
      for (final VisualElement child : getChildren())
        if (DataView.class.isInstance(child) &&
            ((DataView) child).browserEventClass().isInstance(event))
          found = true;
    if (found == false) new GenericSrcView(null, this);

    // save this event into persistent area
    final Session session = synchro.getSessionFactory().getCurrentSession();
    session.update(this);
    EventList el = eventLists.get(event.getClass().toString());
    if (el == null) eventLists.put(event.getClass().toString(), el = new EventList());
    event.setEventList(el);
    session.save(event);
  }

  /**
   * Called when this target is disposed.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void disposed() {
    super.disposed();
    getGUI().dropTargetInstance(this);
  }

  /**
   * Checks that this target can be attached to a specific parent element.
   * @param parent parent element.
   * @return true if this target can be attached to this parent element.
   */
  public boolean canAddTarget(final VisualElement parent) {
    if (parent != null) synchronized (getGUI().getGUICreated()) {
      if (getGUI().getGUICreated()[0] == true) {
        // group "local host" is readonly
        if (parent.equals(getGUI().getVisualThisHost())) return false;

        // group "every host" is readonly
        if (parent.equals(getGUI().getVisualTransientAll())) return false;

        if (getGUI().getVisualTransientNetworks() != null) {
          // group "networks" and its childs are readonly
          if (parent.equals(getGUI().getVisualTransientNetworks()) ||
              getGUI().getVisualTransientNetworks().getChildren().contains(parent))
            return false;
        }

        // avoid twins
        if (parent.getChildren().contains(this)) return false;

        // avoid self reference
        if (parent.equals(this)) return false;

        // avoid loops
        if (parent.getAllParents(getClass()).contains(this)) return false;
      }
    }

    return true; 
  }

  /**
   * Attaches this target to a specific parent element.
   * @param gui current GUI instance.
   * @param parent parent element.
   * @return true if this target has been succesfully attached to this parent element.
   */
  // lock survey : sync_tree << synchro << HERE
  public boolean addTarget(final GUI gui, final Target parent) {
    initialize(gui);

    if (!canAddTarget(parent)) return false;
    if (parent != null && !parent.canManageThisChild(this)) return false;

    if (parent != null) {
      final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
      session.saveOrUpdate(getGUI().getCanonicalInstance(this));
      session.update(parent);
      getGUI().getCanonicalInstance(this).setParent(getGUI(), parent);
      if (getGUI().getCanonicalInstance(this) == this) return true;
    }
    return false;
  }

  /**
   * Compares two targets.
   * @param o target to compare to.
   * @return true if the targets are equal.
   */
  // any thread
  public boolean equals(final Object o) {
    if (this == o) return true;
    if ((o == null) || (o.getClass() != getClass())) return false;
    final Target target = (Target) o;
    return getName().equals(target.getName());
  }

  /**
   * Returns the hashcode for this target.
   * @param none.
   * @return int hashcode.
   */
  // any thread
  public int hashCode() {
    return getName().hashCode();
  }
}