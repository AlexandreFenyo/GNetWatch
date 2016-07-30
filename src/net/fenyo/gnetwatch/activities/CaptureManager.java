
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.GUI.GUI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.collections.*;

import org.dom4j.*;
import org.dom4j.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * This class captures Ethernet frames using tethereal.
 * A tethereal instance is spawned through a Capture instance, for each layer-2 interface.
 * @author Alexandre Fenyo
 * @version $Id: CaptureManager.java,v 1.9 2008/04/15 23:58:17 fenyo Exp $
 */

public class CaptureManager {
  private static Log log = LogFactory.getLog(CaptureManager.class);

  private final Config config;
  private GUI gui;

  private final List<Capture> capture_list = new LinkedList<Capture>();
  // deprecated donc changer de type
  private MultiMap listeners = new MultiHashMap();

  public interface HandlePacket {
    public void document(final Document packet);
  }

  /**
   * Constructor.
   * main thread.
   * @param config configuration.
   */
  public CaptureManager(final Config config) {
    this.config = config;
  }

  /**
   * Defines the GUI instance.
   * @param GUI gui.
   * @return void.
   */
  public void setGUI(final GUI gui) {
    this.gui = gui;
  }

  /**
   * Creates captures instances for each available interface.
   * @param filter tethereal filter.
   * @return void.
   * @throws InterruptedException exception.
   */
  // GUI thread
  private void startCapture(final String filter) throws InterruptedException {
    synchronized (capture_list) {
      final String [] devices = Capture.listDevices();

      if (devices == null) {
        final MessageBox dialog = new MessageBox(gui.getShell(), SWT.ICON_ERROR | SWT.OK);
        dialog.setText(config.getString("thethereal_error"));
        dialog.setMessage(config.getString("thethereal_error_long"));
        dialog.open();
        return;
      }

      for (final String device : devices) {
        final Matcher match = Pattern.compile("^([0-9]*)\\. ").matcher(device);
        if (match.find() == true) {
          final Capture capture = new Capture(config, this, new Integer(match.group(1)).intValue(), filter);
          capture_list.add(capture);
          capture.createCaptureThread();
        }
      }
    }
  }

  /**
   * Stops every capture instances.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // GUI thread
  private void stopCapture() throws InterruptedException {
    synchronized (capture_list) {
      for (final Capture capture : capture_list) capture.end();
      capture_list.clear();
    }
  }

  /**
   * Creates a filter that integrates every individual listener filters.
   * @param none.
   * @return String global filter.
   */
  // GUI thread
  private String getGlobalFilter() {
    String global_filter = "";
    synchronized (listeners) {
      if (!listeners.keySet().contains(""))
        for (final String individual_filter : new HashSet<String>(listeners.keySet())) {
          if (!individual_filter.equals("")) {
            if (global_filter.equals("")) global_filter = "(" + individual_filter + ")";
            else global_filter += " or (" + individual_filter + ")";
          }
        }
      }
    return global_filter;
  }

  /**
   * Register a frame listener.
   * @param filter filter this listener is interested in.
   * @param callback entry point for asynchronous call.
   * @throws InterruptedException exception.
   */
  // the methods registerListener, unRegisterListener and unRegisterAllListeners are synchronized
  // to be sure only one is executed at a time
  // GUI thread
  public synchronized void registerListener(final String filter, final HandlePacket callback) throws InterruptedException {
    final boolean does_contain;

    synchronized (listeners) {
      does_contain = listeners.containsKey(filter);
      if (does_contain) listeners.put(filter, callback);
    }

    if (!does_contain) { 
      stopCapture();

      synchronized (listeners) {
        listeners.put(filter, callback);
      }

      startCapture(getGlobalFilter());
    }
  }

  /**
   * Removes a frame listener.
   * @param filter filter this listener was interested in.
   * @param callback entry point for asynchronous call.
   * @return void.
   * @throws InterruptedException exception.
   */
  // GUI thread
  public synchronized void unRegisterListener(final String filter, final HandlePacket callback) throws InterruptedException {
    final boolean does_contain;
    final int size;

    synchronized (listeners) {
      listeners.remove(filter, callback);
      does_contain = listeners.containsKey(filter);
      size = listeners.size();
    }

    if (!does_contain) {
      stopCapture();
      if (size > 0) startCapture(getGlobalFilter());
    }
  }

  /**
   * Removes every listeners.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // main thread
  public synchronized void unRegisterAllListeners() throws InterruptedException {
    synchronized (listeners) {
      listeners.clear();
    }

    stopCapture();
  }

  /**
   * Inform every listeners about the next frame.
   * @param packet next frame.
   * @return void.
   */
  // Capture thread
  public void handlePacket(final Document packet) {
    synchronized (listeners) {
      for (final HandlePacket callback : new ArrayList<HandlePacket>(listeners.values()))
        callback.document(packet);
    }
  }
}
