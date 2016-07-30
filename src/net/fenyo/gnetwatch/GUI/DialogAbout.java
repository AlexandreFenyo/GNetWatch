
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.browser.*;

/**
 * This class displays the "about" dialog.
 * @author Alexandre Fenyo
 * @version $Id: DialogAbout.java,v 1.7 2008/04/15 23:58:17 fenyo Exp $
 */

public class DialogAbout extends Dialog {
  private static Log log = LogFactory.getLog(DialogAbout.class);

  private GridLayout layout = null;
  private Composite groups_composite = null;
  private Composite bottom_composite = null;
  private RowLayout groups_composite_layout = null;
  private RowLayout bottom_composite_layout = null;
  private GridData groups_composite_grid_data = null;
  private Label label_image = null;
  private GUI gui;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param parent parent shell.
   */
  public DialogAbout(final GUI gui, final Shell parent) {
    super(parent, 0);
    this.gui = gui;
  }

  /**
   * Displays the dialog.
   * @param none.
   * @return void.
   */
  public void open() {
    final Shell parent = getParent();
    final Display display = parent.getDisplay();
    final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
    shell.setText("GNetWatch - About");

    // Your code goes here (widget creation, set result, etc).
    // Composite for groups at left
    layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginHeight = 2;
    layout.marginWidth = 2;
    layout.verticalSpacing = 1;
    shell.setLayout(layout);

    groups_composite = new Composite(shell, SWT.FLAT);
    groups_composite_layout = new RowLayout(SWT.VERTICAL);
    groups_composite_layout.fill = true;
    groups_composite_layout.marginTop = 0;
    groups_composite_layout.marginBottom = 0;
    groups_composite.setLayout(groups_composite_layout);
    groups_composite_grid_data = new GridData(GridData.FILL_VERTICAL);
    groups_composite.setLayoutData(groups_composite_grid_data);

    // Group for network parameters

    label_image = new Label(groups_composite, SWT.SHADOW_ETCHED_IN | SWT.BORDER);
    label_image.setText("Network parameters");
    label_image.setImage(new Image(display, "pictures/about.png"));

    // bottom buttons

    bottom_composite = new Composite(shell, SWT.FLAT);
    bottom_composite_layout = new RowLayout();
    bottom_composite_layout.fill = true;
    bottom_composite_layout.marginTop = 0;
    bottom_composite_layout.marginBottom = 0;
    bottom_composite_layout.wrap = false;
    bottom_composite_layout.pack = false;
    bottom_composite_layout.justify = true;
    bottom_composite_layout.type = SWT.HORIZONTAL;
    bottom_composite_layout.marginLeft = 5;
    bottom_composite_layout.marginTop = 5;
    bottom_composite_layout.marginRight = 5;
    bottom_composite_layout.marginBottom = 5;
    bottom_composite_layout.spacing = 0;
    bottom_composite.setLayout(bottom_composite_layout);
    final GridData bottom_composite_grid_data = new GridData(GridData.FILL_HORIZONTAL);
    bottom_composite.setLayoutData(bottom_composite_grid_data);

    final Button button_ok = new Button(bottom_composite, SWT.PUSH);
    button_ok.setText(gui.getConfig().getString("license"));

    button_ok.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        shell.dispose();
      }

      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    shell.pack(true);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch()) display.sleep();
  }
}
