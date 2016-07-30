
package net.fenyo.gnetwatch.GUI;

import java.net.SocketException;
import java.net.UnknownHostException;

import net.fenyo.gnetwatch.AlgorithmException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*; 

public class TransientGUI {
  private static Log log = LogFactory.getLog(TransientGUI.class);
  private JFrame frame;
  private boolean finish = false;
  private Timer timer;
  private int cnt = 0;
  private int size = 0;
  JButton button;

  public TransientGUI() {}

  public boolean isFinished() {
    return finish;
  }

  public void setSize(final int size) {
    this.size = size;
  }

  public void setInfo(final String info) {
    button.setText(info);
  }

  public void begin() {
    frame = new JFrame();
    button = new JButton("           interrupt operation           ");
    frame.add(button);
    frame.pack();
    frame.setLocationRelativeTo(null);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        finish = true;
      }
    });
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(final WindowEvent e) {
        finish = true;
      }
    });
    frame.setResizable(false);
    frame.setAlwaysOnTop(true);
    timer = new Timer(1000, new ActionListener() {
      public void actionPerformed(final ActionEvent evt) {
        if (finish == true) {
          timer.stop();
          frame.dispose();
          } else if (frame.isVisible() == false) frame.setVisible(true);
      }
    });
    timer.start();
  }

  public void inc() {
    cnt++;
    if (size == 0)
      frame.setTitle("step " + cnt);
    else frame.setTitle("step " + cnt + " on " + size);
  }

  public void end() {
    finish = true;
  }
}
