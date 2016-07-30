
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
import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.targets.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * This class creates and manages views, depending on the events that are created.
 * @author Alexandre Fenyo
 * @version $Id: Views.java,v 1.26 2008/05/21 16:46:06 fenyo Exp $
 */

public class Views {
  private static Log log = LogFactory.getLog(Views.class);

  private final GUI gui;

  /**
   * Constructor.
   * @param gui GUI instance.
   */
  // main thread
  public Views(final GUI gui) {
    this.gui = gui;
  }

  /**
   * Each time a target gets a new event type, this method creates the associated view.
   * @param target new target.
   * @return void.
   */
  // add data views to this target if needed
  // GUI thread
  public void refreshDataViews(final Target target, final Class event_type) throws Exception {
    synchronized (gui.getSynchro()) {
      // blocage ici
      synchronized (gui.sync_tree) {
        final Session session = gui.getSynchro().getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try  {
          if (event_type == EventGeneric.class) {
            // nothing to do : no view can handle generic events
          }

          // manage events handled by ReachableView
          if (event_type == EventReachable.class
            // || event_type == A_CLASS_ReachableView_CAN_HANDLE
          ) {
            ReachableView reachable_view = null;
            // look for an instance of ReachableView
            for (final VisualElement child : target.getChildren())
              if (child instanceof ReachableView) {
                reachable_view = (ReachableView) child;
                break;
              }
            if (reachable_view == null) new ReachableView(gui, target);
          }

          // manage events handled by GenericProcessView
          if (event_type == EventGenericProcess.class
          ) {
            GenericProcessView process_view = null;
            // look for an instance of GenericProcessView
            for (final VisualElement child : target.getChildren())
              if (child instanceof GenericProcessView) {
                process_view = (GenericProcessView) child;
                break;
              }
            if (process_view == null) new GenericProcessView(gui, target);
          }

          // manage events handled by GenericSrcView
          if (event_type == EventGenericSrc.class
          ) {
            GenericSrcView src_view = null;
            // look for an instance of GenericProcessView
            for (final VisualElement child : target.getChildren())
              if (child instanceof GenericSrcView) {
                src_view = (GenericSrcView) child;
                break;
              }
            if (src_view == null) new GenericSrcView(gui, target);
          }

          // manage events handled by FloodView
          if (event_type == EventFlood.class) {
            FloodView flood_view = null;
            // look for an instance of ReachableView
            for (final VisualElement child : target.getChildren())
              if (child instanceof FloodView) {
                flood_view = (FloodView) child;
                break;
              }
            if (flood_view == null) new FloodView(gui, target);
          }

          // manage events handled by HTTPView
          if (event_type == EventHTTP.class) {
            HTTPView http_view = null;
            // look for an instance of HTTPView
            for (final VisualElement child : target.getChildren())
              if (child instanceof HTTPView) {
                http_view = (HTTPView) child;
                break;
              }
            if (http_view == null) new HTTPView(gui, target);
          }

          // manage events handled by HTTPPagesView
          if (event_type == EventHTTPPages.class) {
            HTTPPagesView http_pages_view = null;
            // look for an instance of HTTPView
            for (final VisualElement child : target.getChildren())
              if (child instanceof HTTPPagesView) {
                http_pages_view = (HTTPPagesView) child;
                break;
              }
            if (http_pages_view == null) new HTTPPagesView(gui, target);
          }

          // manage events handled by NmapView
          if (event_type == EventNmap.class) {
            NmapView nmap_view = null;
            // look for an instance of NmapView
            for (final VisualElement child : target.getChildren())
              if (child instanceof NmapView) {
                nmap_view = (NmapView) child;
                break;
            }
            if (nmap_view == null) new NmapView(gui, target);
          }

          // manage events handled by BytesReceivedView
          if (event_type == EventBytesReceived.class) {
            BytesReceivedView bytes_received_view = null;
            // look for an instance of BytesReceivedView
            for (final VisualElement child : target.getChildren())
              if (child instanceof BytesReceivedView) {
                bytes_received_view = (BytesReceivedView) child;
                break;
              }
            if (bytes_received_view == null) new BytesReceivedView(gui, target);
          }

          // manage events handled by BytesSentView
          if (event_type == EventBytesSent.class) {
            BytesSentView bytes_sent_view = null;
            // look for an instance of BytesSentView
            for (final VisualElement child : target.getChildren())
              if (child instanceof BytesSentView) {
                bytes_sent_view = (BytesSentView) child;
                break;
              }
            if (bytes_sent_view == null) new BytesSentView(gui, target);
          }
          
          session.getTransaction().commit();
        } catch (final Exception ex) {
          session.getTransaction().rollback();
          throw ex;
        }
      }
    }
  }

  /**
   * Checks that the parameter can be a child of this visual element.
   * @param visual_element visual element.
   * @return boolean true is the parameter can be a child of this visual element.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    return false;
  }
}
