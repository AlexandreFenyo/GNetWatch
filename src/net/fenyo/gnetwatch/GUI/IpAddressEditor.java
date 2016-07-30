
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

import net.fenyo.gnetwatch.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

/**
 * This class implements an IPv4 address editor.
 * @author Alexandre Fenyo
 * @version $Id: IpAddressEditor.java,v 1.10 2008/04/15 23:58:17 fenyo Exp $
 */

public class IpAddressEditor implements KeyListener {
  public static final long serialVersionUID = 1L;

  private static Log log = LogFactory.getLog(IpAddressEditor.class);

  private final Text text;
  private final Config config;

  /**
   * Constructor.
   * @param text control that is managed by this editor.
   */
  // GUI thread
  public IpAddressEditor(final Config config, final Text text) {
    this.text = text;
    this.config = config;
    text.setText("000.000.000.000");
    text.addKeyListener(this);
  }

  /**
   * Handles a key event.
   * @param e event.
   * @return void.
   */
  private void handleKey(KeyEvent e) {
    String content = text.getText();
    int position = text.getCaretPosition();
    // 000.000.000.000
    if (e.character == '.') {
      if (position <= 3) position = 4;
      else if (position > 3 && position <= 7) position = 8;
      else if (position > 7 && position <= 11) position = 12;
      text.setSelection(position, position);
      return;
    }
    if (e.character == '\10') {
      if (position > 0) {
        position--;
        text.setSelection(position, position);
      }
      return;
    }
    if (e.character == '\177') {
      if (position < 15) {
        position++;
        text.setSelection(position, position);
      }
      return;
    }
    if (position >= 15) position = 14;
    if (position == 3 || position == 7 || position == 11) position++;
    String new_content = content.substring(0, position) + e.character + content.substring(position + 1); 
    if (new_content.matches("^[0-9][0-9][0-9]\\.[0-9][0-9][0-9]\\.[0-9][0-9][0-9]\\.[0-9][0-9][0-9]$") == false) return;
    if (new Integer(new_content.substring(0, 3)).intValue() > 255 ||
      new Integer(new_content.substring(4, 7)).intValue() > 255 ||
      new Integer(new_content.substring(8, 11)).intValue() > 255 ||
      new Integer(new_content.substring(12, 15)).intValue() > 255) {
      if (position > 13) return;
      new_content = content.substring(0, position) + e.character + '0' + content.substring(position + 2);
      if (new Integer(new_content.substring(0, 3)).intValue() > 255 ||
          new Integer(new_content.substring(4, 7)).intValue() > 255 ||
          new Integer(new_content.substring(8, 11)).intValue() > 255 ||
          new Integer(new_content.substring(12, 15)).intValue() > 255)
        return;
      text.setText(new_content);
      if (position < 15) position++;
      text.setSelection(position, position);
      return;
    }
    text.setText(new_content);
    if (position < 15) position++;
    text.setSelection(position, position);
  }

  /**
   * Handles a key press event.
   * @param e event.
   * @return void.
   */
  // GUI thread
	public void keyPressed(KeyEvent e) {
    if (config.getProperty("ipaddresseditor.insertonkeypressed").equals("true"))
      handleKey(e);
	}

  /**
   * Handles a key release event.
   * @param e event.
   * @return void.
   */
  // GUI thread
	public void keyReleased(KeyEvent e) {
    if (!config.getProperty("ipaddresseditor.insertonkeypressed").equals("true"))
      handleKey(e); 
  }

}
