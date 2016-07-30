
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

import java.net.UnknownHostException;

import net.fenyo.gnetwatch.AlgorithmException;
import net.fenyo.gnetwatch.GenericTools;
import net.fenyo.gnetwatch.targets.TargetIPv4Subnet;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.browser.*;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.tree.xpath.*;
import java.util.*;

/**
 * This class manages the "IP options" dialog.
 * @author Alexandre Fenyo
 * @version $Id: DialogGeneric.java,v 1.3 2008/05/25 17:10:37 fenyo Exp $
 */

/*
 * title
 * command line
 * filename
 * working directory
 * unit
 */

public class DialogGeneric extends Dialog {
  private static Log log = LogFactory.getLog(DialogGeneric.class);

  private final GUI gui;

  private final int bglevel = 100;

  private GridLayout layout = null;
  private Composite groups_composite = null;
  private Composite bottom_composite = null;
  private RowLayout groups_composite_layout = null;
  private RowLayout bottom_composite_layout = null;
  private GridData groups_composite_grid_data = null;
  private Group group_network_parameters = null;
  private GridLayout group_network_parameters_layout = null;

  private boolean ok_clicked = false;

  private String title = "";
  private String cmdline = "";
  private String filename = "";
  private String workdir = "";
  private String unit = "";

  public boolean isOK() {
    return ok_clicked;
  }

  private java.util.List<String> parseConfigFile() {
    java.util.List<String> values = new ArrayList();

    try {
      final XMLConfiguration initial = new XMLConfiguration(gui.getConfig().getProperty("genericconffile"));
      initial.setExpressionEngine(new XPathExpressionEngine());
      
      for (final HierarchicalConfiguration subconf : (java.util.List<HierarchicalConfiguration>)
          initial.configurationsAt("/generic/template")) {
        final String name = subconf.getString("name");
        if (name != null) values.add(name);
      }
    } catch (final ConfigurationException ex) {
      log.error("Exception", ex);
    }

    return values;
  }

  private Map<String, String> parseConfigFile(final String name) {
    Map<String, String> values = new HashMap();

    try {
      final XMLConfiguration initial = new XMLConfiguration(gui.getConfig().getProperty("genericconffile"));
      initial.setExpressionEngine(new XPathExpressionEngine());

      for (final HierarchicalConfiguration subconf : (java.util.List<HierarchicalConfiguration>)
          initial.configurationsAt("/generic/template")) {
        final String subconf_name = subconf.getString("name");
        if (subconf_name != null && subconf_name.equals(name)) {
          for (final String key : new String [] { "name", "title", "cmdline", "filename", "workdir", "unit" }) 
            values.put(key, subconf.getString(key));
          return values;
        }
      }
    } catch (final ConfigurationException ex) {
      log.error("Exception", ex);
    }

    return null;
  }

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param parent parent shell.
   */
  public DialogGeneric(final GUI gui, final Shell parent) {
    super(parent, 0);
    this.gui = gui;
  }

  public String getTitle() {
    return title;
  }

  public String getCommandLine() {
    return cmdline;
  }

  public String getFilename() {
    return filename;
  }

  public String getWorkdir() {
    return workdir;
  }

  public String getUnit() {
    return unit;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setCommandLine(final String cmdline) {
    this.cmdline = cmdline;
  }

  public void setFilename(final String filename) {
    this.filename = filename;
  }

  public void setWorkdir(final String workdir) {
    this.workdir = workdir;
  }

  public void setUnit(final String unit) {
    this.unit = unit;
  }

  /**
   * Displays the dialog.
   * @param none.
   * @return void.
   */
  public void open() {
    final Shell parent = getParent();
    final Display display = parent.getDisplay();
    final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    shell.setText(gui.getConfig().getString("gnetwatch_generic_options"));
//    shell.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));

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

    // Group for HTTP parameters

    group_network_parameters = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_network_parameters_layout = new GridLayout();
    group_network_parameters_layout.numColumns = 2;
    group_network_parameters.setLayout(group_network_parameters_layout);
    group_network_parameters.setText(gui.getConfig().getString("generic_parameters"));
    
    final Label label_template = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_template.setText(gui.getConfig().getString("template"));
    label_template.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Combo ctemplate = new Combo(group_network_parameters, SWT.SHADOW_IN | SWT.READ_ONLY);
    ctemplate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    ctemplate.add(gui.getConfig().getString("select_to_apply_template"));
    ctemplate.setText(gui.getConfig().getString("select_to_apply_template"));
    for (final String name : parseConfigFile()) ctemplate.add(name);

    final Label label2 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label2.setText(gui.getConfig().getString("title"));
    label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_title_value = new Text(group_network_parameters, SWT.SINGLE);
    group_title_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_title_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_title_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_title_value.setText(title);
    final GC gc2 = new GC(group_title_value);
    ((GridData) (group_title_value.getLayoutData())).widthHint = gc2.stringExtent("aaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbcccccccccccccccccccc                                            ").x;

    final Label label3 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label3.setText(gui.getConfig().getString("cmdline"));
    label3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_cmdline_value = new Text(group_network_parameters, SWT.SINGLE);
    group_cmdline_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_cmdline_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_cmdline_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_cmdline_value.setText(cmdline);
    final GC gc3 = new GC(group_cmdline_value);
    ((GridData) (group_cmdline_value.getLayoutData())).widthHint = gc3.stringExtent("aaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbcccccccccccccccccccc                                            ").x;

    final Label label4 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label4.setText(gui.getConfig().getString("filename"));
    label4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_filename_value = new Text(group_network_parameters, SWT.SINGLE);
    group_filename_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_filename_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_filename_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_filename_value.setText(filename);
    final GC gc4 = new GC(group_filename_value);
    ((GridData) (group_filename_value.getLayoutData())).widthHint = gc4.stringExtent("aaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbcccccccccccccccccccc                                            ").x;

    final Label label5 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label5.setText(gui.getConfig().getString("workdir"));
    label5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_workdir_value = new Text(group_network_parameters, SWT.SINGLE);
    group_workdir_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_workdir_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_workdir_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_workdir_value.setText(workdir);
    final GC gc5 = new GC(group_workdir_value);
    ((GridData) (group_workdir_value.getLayoutData())).widthHint = gc5.stringExtent("aaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbcccccccccccccccccccc                                            ").x;

    final Label label6 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label6.setText(gui.getConfig().getString("unit"));
    label6.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_unit_value = new Text(group_network_parameters, SWT.SINGLE);
    group_unit_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_unit_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_unit_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_unit_value.setText(unit);
    final GC gc6 = new GC(group_unit_value);
    ((GridData) (group_unit_value.getLayoutData())).widthHint = gc6.stringExtent("aaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbcccccccccccccccccccc                                            ").x;

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
    button_ok.setText("Ok");
    final DialogGeneric _this = this;
    button_ok.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        _this.title = group_title_value.getText();
        _this.cmdline = group_cmdline_value.getText();
        _this.filename = group_filename_value.getText();
        _this.workdir = group_workdir_value.getText();
        _this.unit = group_unit_value.getText();

        ok_clicked = true;

        shell.dispose();
      }

      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });


    final Button button_cancel = new Button(bottom_composite, SWT.PUSH);
    button_cancel.setText(gui.getConfig().getString("cancel"));
    button_cancel.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        shell.dispose();
      }

      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    ctemplate.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        final Map<String, String> values = parseConfigFile(ctemplate.getText());
        if (values != null) {
          group_title_value.setText(values.get("title") != null ? values.get("title") : "");
          group_cmdline_value.setText(values.get("cmdline") != null ? values.get("cmdline") : "");
          group_filename_value.setText(values.get("filename") != null ? values.get("filename") : "");
          group_workdir_value.setText(values.get("workdir") != null ? values.get("workdir") : "");
          group_unit_value.setText(values.get("unit") != null ? values.get("unit") : "");
        } else {
          group_title_value.setText("");
          group_cmdline_value.setText("");
          group_filename_value.setText("");
          group_workdir_value.setText("");
          group_unit_value.setText("");
        }
        title = group_title_value.getText();
        cmdline = group_cmdline_value.getText();
        filename = group_filename_value.getText();
        workdir = group_workdir_value.getText();
        unit = group_unit_value.getText();
      }
    });

    shell.pack(true);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch()) display.sleep();
  }
}
