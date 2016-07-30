
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
 * This class manages the "IP options" dialog.
 * @author Alexandre Fenyo
 * @version $Id: DialogIPOptions.java,v 1.14 2008/04/15 23:58:17 fenyo Exp $
 */

/*
 * port src
 * port dst
 * DSCP
 * taille des paquets (cf. setSendBufferSize()) pdu max size
 *
 * proxy yes/no
 * proxy host
 * proxy port
 * URL
 * reconnect after each GET yes/no
 * number of parallel sessions
 */

public class DialogIPOptions extends Dialog {
  private static Log log = LogFactory.getLog(DialogIPOptions.class);

  private final GUI gui;

  private final int bglevel = 100;

  private String [] dscp = new String [] {
      "000000 - CS0 - default",
      "000001",
      "000010",
      "000011",
      "000100",
      "000101",
      "000110",
      "000111",
      "001000 - CS1",
      "001001",
      "001010 - AF11",
      "001011",
      "001100 - AF12",
      "001101",
      "001110 - AF13",
      "001111",
      "010000 - CS2",
      "010001",
      "010010 - AF21",
      "010011",
      "010100 - AF22",
      "010101",
      "010110 - AF23",
      "010111",
      "011000 - CS3",
      "011001",
      "011010 - AF31",
      "011011",
      "011100 - AF32",
      "011101",
      "011110 - AF33",
      "011111",
      "100000 - CS4",
      "100001",
      "100010  - AF41",
      "100011",
      "100100  - AF42",
      "100101",
      "100110  - AF43",
      "100111",
      "101000 - CS5",
      "101001",
      "101010",
      "101011",
      "101100",
      "101101",
      "101110 - EF",
      "101111",
      "110000 - CS6",
      "110001",
      "110010",
      "110011",
      "110100",
      "110101",
      "110110",
      "110111",
      "111000 - CS7",
      "111001",
      "111010",
      "111011",
      "111100",
      "111101",
      "111110",
      "111111",
  };

  private GridLayout layout = null;
  private Composite groups_composite = null;
  private Composite bottom_composite = null;
  private RowLayout groups_composite_layout = null;
  private RowLayout bottom_composite_layout = null;
  private GridData groups_composite_grid_data = null;
  private Group group_network_parameters = null;
  private GridLayout group_network_parameters_layout = null;

  private int tos = 0;
  private int port_src = 10000;
  private int port_dst = 10000;
  private int pdu_max_size = 1400;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param parent parent shell.
   */
  public DialogIPOptions(final GUI gui, final Shell parent) {
    super(parent, 0);
    this.gui = gui;
  }

  /**
   * Gets the IP type of service.
   * @param none.
   * @return int type of service.
   */
  public int getTOS() {
    return tos;
  }

  /**
   * Gets the source port.
   * @param none.
   * @return int source port.
   */
  public int getPortSrc() {
    return port_src;
  }

  /**
   * Gets the destination port.
   * @return int destination port.
   */
  public int getPortDst() {
    return port_dst;
  }

  /**
   * Gets the PDU maximum size.
   * @param none.
   * @return int PDU maximum size.
   */
  public int getPDUMaxSize() {
    return pdu_max_size;
  }

  /**
   * Sets the type of service.
   * @param tos type of service.
   * @return void.
   */
  public void setTOS(final int tos) {
    this.tos = tos;
  }

  /**
   * Sets the source port.
   * @param port_src source port.
   * @return void.
   */
  public void setPortSrc(final int port_src) {
    this.port_src = port_src;
  }

  /**
   * Sets the destination port.
   * @param port_dst destination port.
   * @return void.
   */
  public void setPortDst(final int port_dst) {
    this.port_dst = port_dst;
  }

  /**
   * Sets the PDU maximum size.
   * @param pdu_max_size PDU maximum size.
   * @return void.
   */
  public void setPDUMaxSize(final int pdu_max_size) {
    this.pdu_max_size = pdu_max_size;
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
    shell.setText(gui.getConfig().getString("gnetwatch_ip_options"));
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

    // Group for network parameters

    group_network_parameters = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_network_parameters_layout = new GridLayout();
    group_network_parameters_layout.numColumns = 2;
    group_network_parameters.setLayout(group_network_parameters_layout);
    group_network_parameters.setText(gui.getConfig().getString("network_parameters"));

    // RFC 3260
    final Label label_tos = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_tos.setText(gui.getConfig().getString("dscp_name"));
    label_tos.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Combo ctos = new Combo(group_network_parameters, SWT.SHADOW_IN | SWT.READ_ONLY);
    ctos.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    for (final String tos : dscp) ctos.add(tos);
    ctos.setText(dscp[tos]);

    final Label label_packet_size = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_packet_size.setText(gui.getConfig().getString("pdu_max_size"));
    label_packet_size.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner packet_size = new Spinner(group_network_parameters, SWT.WRAP);
    packet_size.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    packet_size.setMinimum(0);
    packet_size.setMaximum(10000);
    packet_size.setSelection(this.pdu_max_size);

    final Label label_port_src = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_port_src.setText(gui.getConfig().getString("source_port"));
    label_port_src.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner port_src = new Spinner(group_network_parameters, SWT.WRAP);
    port_src.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    port_src.setMinimum(1);
    port_src.setMaximum(65535);
    port_src.setSelection(this.port_src);

    final Label label_port_dst = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_port_dst.setText(gui.getConfig().getString("destination_port"));
    label_port_dst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner port_dst = new Spinner(group_network_parameters, SWT.WRAP);
    port_dst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    port_dst.setMinimum(0);
    port_dst.setMaximum(65535);
    port_dst.setSelection(this.port_dst);

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
    final DialogIPOptions _this = this;
    button_ok.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        _this.tos = ctos.getSelectionIndex();
        _this.port_src = port_src.getSelection();
        _this.port_dst = port_dst.getSelection();
        _this.pdu_max_size = packet_size.getSelection();

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

    shell.pack(true);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch()) display.sleep();
  }
}
