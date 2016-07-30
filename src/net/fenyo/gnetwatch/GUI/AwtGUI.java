
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

package net.fenyo.gnetwatch.GUI;

import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.activities.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class creates JFrame instances outside of SWT, to allow use of Java2D API.
 * @author Alexandre Fenyo
 * @version $Id: AwtGUI.java,v 1.16 2008/10/30 12:01:27 fenyo Exp $
 */

public class AwtGUI implements Runnable {
  private static Log log = LogFactory.getLog(AwtGUI.class);

  private final Config config;

  private Thread repaint_thread;
  private java.util.List<JFrame> frame_list = new ArrayList<JFrame>();

  /**
   * Constructor.
   * @param config configuration.
   */
  // GUI thread
  public AwtGUI(final Config config) {
    this.config = config;
  }

  /**
   * Removes an AWT frame.
   * @param frame frame to remove.
   * @return void.
   */
  // GUI thread
  public void dropFrame(final JFrame frame) {
    synchronized (frame_list) {
      frame_list.remove(frame);
    }
    frame.dispose();
  }

  /**
   * Creates a thread that will repaint each frame regularly.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void createAwtGUI() {
    JFrame.setDefaultLookAndFeelDecorated(false);
    createRepaintThread();
  }

  /**
   * Creates a thread that will repaint each frame regularly.
   * @param none.
   * @return void.
   */
  // GUI thread
  private void createRepaintThread() {
    repaint_thread = new Thread(this, "Repaint Thread");
    repaint_thread.start();
  }

  /**
   * Adds a component to a frame and displays the frame.
   * This method must be run from the AWT thread.
   * @param frame frame.
   * @param component component to add.
   */
  // AWT thread
  private void _createFrame(final JFrame frame, final BasicComponent component) {
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(component);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
    frame.toFront();
  }

  /**
   * Adds a component to a frame and displays the frame.
   * This method can be run from any thread, and particularly the SWT thread.
   * @param frame frame.
   * @param component component to add.
   */
  // GUI thread
  public JFrame createFrame(final String name, final BasicComponent component) throws InterruptedException, InvocationTargetException {
    final JFrame frame = new JFrame(name);
    frame.addWindowListener(component);
    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        _createFrame(frame, component);
      }
    });
    synchronized (frame_list) {
      frame_list.add(frame);
    }
    return frame;
  }

  /**
   * Refreshes the frames regularly.
   * @param none.
   * @return void.
   */
  // repaint thread
  public void run() {
    int cnt = 0;
    while (!config.isEnd())
      try {
        Thread.sleep(20);
        cnt++;
        synchronized (frame_list) {
          for (final Frame frame : frame_list) {
            final BasicComponent component = ((BasicComponent) ((JFrame) frame).getContentPane().getComponent(0));
            if ((component.isManualMode() == false && component.pixelsOffsetChanged() == true) || cnt == 50) frame.repaint();
          }
        }
        if (cnt == 50) cnt = 0;
      } catch (final InterruptedException ex) {
        // this thread is interrupted when the application is terminated
      }
  }

  /**
   * Terminates the repaint thread and closes any frame.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // main thread
  public void end() throws InterruptedException {
    // terminate the repaint thread
    repaint_thread.interrupt();
    repaint_thread.join();

    synchronized (frame_list) {
      for (final Frame frame : frame_list) frame.dispose();
    }
  }

  public void createDialog() {
    try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          final JTextField button = new JTextField();
          button.setText("close this window to stop the foreground action");
          final JFrame frame = new JFrame("working...");
          frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
              // XXX
            }
          });

        frame.getContentPane().add(button, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        }
      });
    } catch (final InvocationTargetException ex) {
      log.error("exception", ex);
    }
    catch (final InterruptedException ex) {
      log.error("exception", ex);
    }
  }
}
