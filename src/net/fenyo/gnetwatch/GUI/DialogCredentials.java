
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

import org.snmp4j.security.*;

/**
 * This class manages the "credentials" dialog.
 * @author Alexandre Fenyo
 * @version $Id: DialogCredentials.java,v 1.17 2008/04/15 23:58:17 fenyo Exp $
 */

/*
 *  snmp :
 *  version
 *  security level
 *  retries
 *  timeout
 *  port
 *  max size request pdu
 *  v1 et v2c : community
 *         v3 : username / password pour authentication, password pour privacy
 */

public class DialogCredentials extends Dialog {
  private static Log log = LogFactory.getLog(DialogCredentials.class);

  private final GUI gui;

  private final int bglevel = 100;

  private GridLayout layout = null;
  private Composite groups_composite = null;
  private Composite bottom_composite = null;
  private RowLayout groups_composite_layout = null;
  private RowLayout bottom_composite_layout = null;
  private GridData groups_composite_grid_data = null;
  private Group group_network_parameters = null, group_credentials = null, group_credentials_v2c = null;
  private GridLayout group_network_parameters_layout = null, group_credentials_layout = null, group_credentials_v2c_layout = null;
  private Text group_credentials_value = null;
  private Text group_credentials_value2 = null;
  private Text group_credentials_value3 = null;
  private Text group_credentials_v2c_value = null;

  private int version = 0; // SNMPv1
  private int sec = SecurityLevel.AUTH_PRIV;
  private int retries = 3;
  private int timeout = 1500; // microsec
  private int port = 161;
  private String community = "public";
  private String username = "";
  private String password_auth = "";
  private String password_priv = "";
  private int pdu_max_size = 1000;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param parent parent shell.
   */
  public DialogCredentials(final GUI gui, final Shell parent) {
    super(parent, 0);
    this.gui = gui;
  }

  /**
   * Gets SNMP version.
   * @param none.
   * @return int SNMP version.
   */
  public int getVersion() {
    return version;
  }

  /**
   * Gets security level.
   * @param none.
   * @return int security level.
   */
  public int getSec() {
    return sec;
  }

  /**
   * Gets number of retries.
   * @param none.
   * @return int retries.
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Gets SNMP timeout per try.
   * @param none.
   * @return int timeout.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Returns the SNMP agent UDP port.
   * @param none.
   * @return int SNMP agent UDP port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the community string.
   * @param none.
   * @return String community string.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Returns the username.
   * @param none.
   * @return String username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the password authentication.
   * @param none.
   * @return String password authentication.
   */
  public String getPasswordAuth() {
    return password_auth;
  }

  /**
   * Returns the password privacy.
   * @param none.
   * @return String password privacy.
   */
  public String getPasswordPriv() {
    return password_priv;
  }

  /**
   * Returns the maximum PDU size.
   * @param none.
   * @return int maximum PDU size.
   */
  public int getPDUMaxSize() {
    return pdu_max_size;
  }

  /**
   * Sets the SNMP version.
   * @param version SNMP version.
   * @return void.
   */
  public void setVersion(final int version) {
    this.version = version;
  }

  /**
   * Sets the security level.
   * @param sec security level.
   * @return void.
   */
  public void setSec(final int sec) {
    this.sec = sec;
  }

  /**
   * Sets the number of SNMP retries.
   * @param int number of SNMP retries.
   * @return void.
   */
  public void setRetries(final int retries) {
    this.retries = retries;
  }

  /**
   * Sets the SNMP timeout per try.
   * @param timeout timeout per try.
   * @return void.
   */
  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

  /**
   * Sets the SNMP agent UDP port.
   * @param port SNMP agent UDP port.
   * @return void.
   */
  public void setPort(final int port) {
    this.port = port;
  }

  /**
   * Sets the community string.
   * @param community community string.
   * @return void.
   */
  public void setCommunity(final String community) {
    this.community = community;
  }

  /**
   * Sets the username.
   * @param username username.
   * @return void.
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * Sets the password authentication.
   * @param password_auth password authentication.
   * @return void.
   */
  public void setPasswordAuth(final String password_auth) {
    this.password_auth = password_auth;
  }

  /**
   * Sets the password privacy.
   * @param password_priv password privacy.;
   * @return void.
   */
  public void setPasswordPriv(final String password_priv) {
    this.password_priv = password_priv;
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
    shell.setText(gui.getConfig().getString("gnetwatch_cred"));
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

    final Label label_version = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_version.setText(gui.getConfig().getString("protocol_version"));
    label_version.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Combo cversion = new Combo(group_network_parameters, SWT.SHADOW_IN | SWT.READ_ONLY);
    cversion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    version.setBackground(new Color(display, bglevel, bglevel, bglevel));
//    version.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    cversion.add("SNMP v1");
    cversion.add("SNMP v2c");
    cversion.add("SNMP v3");
    if (version == 0) cversion.setText("SNMP v1");
    else if (version == 1) cversion.setText("SNMP v2c");
    else cversion.setText("SNMP v3");

    final Label label_sec = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_sec.setText(gui.getConfig().getString("security_level"));
    label_sec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Combo csec = new Combo(group_network_parameters, SWT.SHADOW_IN | SWT.READ_ONLY);
    csec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    csec.add("open");
    csec.add("auth only");
    csec.add("auth+priv");
    if (sec == SecurityLevel.AUTH_PRIV) csec.setText("auth+priv");
    else if (sec == SecurityLevel.AUTH_NOPRIV) csec.setText("auth only");
    else csec.setText("open");
    label_sec.setEnabled(version == 2);
    csec.setEnabled(version == 2);

    final Label label_packet_size = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_packet_size.setText(gui.getConfig().getString("pdu_max_size"));
    label_packet_size.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner packet_size = new Spinner(group_network_parameters, SWT.WRAP);
    packet_size.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    packet_size.setMinimum(0);
    packet_size.setMaximum(10000);
    packet_size.setSelection(this.pdu_max_size);
    //    packet_size.setBackground(new Color(display, bglevel, bglevel, bglevel));
    //    packet_size.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

    final Label label_retries = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_retries.setText(gui.getConfig().getString("retries"));
    label_retries.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner retries = new Spinner(group_network_parameters, SWT.WRAP);
    retries.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    retries.setMinimum(0);
    retries.setMaximum(15);
    retries.setSelection(this.retries);

    final Label label_timeout = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_timeout.setText(gui.getConfig().getString("timeout_microsec"));
    label_timeout.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner timeout = new Spinner(group_network_parameters, SWT.WRAP);
    timeout.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    timeout.setMinimum(0);
    timeout.setMaximum(10000);
    timeout.setSelection(this.timeout);

    final Label label_port = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_port.setText(gui.getConfig().getString("destination_port"));
    label_port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner port = new Spinner(group_network_parameters, SWT.WRAP);
    port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    port.setMinimum(1);
    port.setMaximum(65535);
    port.setSelection(this.port);

    // Group for SNMPv1 & SNMPv2c credentials

    group_credentials_v2c = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_credentials_v2c_layout = new GridLayout();
    group_credentials_v2c_layout.numColumns = 2;
    group_credentials_v2c.setLayout(group_credentials_v2c_layout);
    group_credentials_v2c.setText(gui.getConfig().getString("v1_v2c_cred"));
    group_credentials_v2c.setEnabled(version != 2);

    final Label label1_v2c = new Label(group_credentials_v2c, SWT.SHADOW_IN);
    label1_v2c.setText(gui.getConfig().getString("community_string"));
    label1_v2c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label1_v2c.setEnabled(version != 2);
    group_credentials_v2c_value = new Text(group_credentials_v2c, SWT.SINGLE);
    group_credentials_v2c_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_credentials_v2c_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_credentials_v2c_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_credentials_v2c_value.setEnabled(version != 2);

    group_credentials_v2c_value.setText(community);
    final GC gc = new GC(group_credentials_v2c_value);
    gc.setFont(group_credentials_v2c_value.getFont());
    ((GridData) (group_credentials_v2c_value.getLayoutData())).widthHint = gc.stringExtent("                      ").x;

    // Group for SNMPv3 credentials

    group_credentials = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_credentials_layout = new GridLayout();
    group_credentials_layout.numColumns = 2;
    group_credentials.setLayout(group_credentials_layout);
    group_credentials.setText(gui.getConfig().getString("v3_cred"));
    group_credentials.setEnabled(version == 2);

    final Label label1 = new Label(group_credentials, SWT.SHADOW_IN);
    label1.setText(gui.getConfig().getString("username"));
    label1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label1.setEnabled(version == 2);
    group_credentials_value = new Text(group_credentials, SWT.SINGLE);
    group_credentials_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_credentials_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_credentials_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_credentials_value.setText(username);
    ((GridData) (group_credentials_value.getLayoutData())).widthHint = gc.stringExtent("                      ").x;
    group_credentials_value.setEnabled(version == 2);

    final Label label2 = new Label(group_credentials, SWT.SHADOW_IN);
    label2.setText(gui.getConfig().getString("authentication_password"));
    label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label2.setEnabled(version == 2);
    group_credentials_value2 = new Text(group_credentials, SWT.SINGLE);
    group_credentials_value2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_credentials_value2.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_credentials_value2.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_credentials_value2.setText(password_auth);
    ((GridData) (group_credentials_value2.getLayoutData())).widthHint = gc.stringExtent("                      ").x;
    group_credentials_value2.setEnabled(version == 2);

    final Label label3 = new Label(group_credentials, SWT.SHADOW_IN);
    label3.setText(gui.getConfig().getString("privacy_password"));
    label3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label3.setEnabled(version == 2);
    group_credentials_value3 = new Text(group_credentials, SWT.SINGLE);
    group_credentials_value3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_credentials_value3.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_credentials_value3.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_credentials_value3.setText(password_priv);
    ((GridData) (group_credentials_value3.getLayoutData())).widthHint = gc.stringExtent("                      ").x;
    group_credentials_value3.setEnabled(version == 2);

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
    final DialogCredentials _this = this;
    button_ok.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        _this.version = cversion.getSelectionIndex();

        if (csec.getSelectionIndex() == 0) _this.sec = SecurityLevel.NOAUTH_NOPRIV;
        else if (csec.getSelectionIndex() == 1) _this.sec = SecurityLevel.AUTH_NOPRIV;
        else _this.sec = SecurityLevel.AUTH_PRIV;

        _this.retries = retries.getSelection();
        _this.timeout = timeout.getSelection();
        _this.port = port.getSelection();
        _this.community = group_credentials_v2c_value.getText();
        _this.username = group_credentials_value.getText();
        _this.password_auth = group_credentials_value2.getText();
        _this.password_priv = group_credentials_value3.getText();
        _this.pdu_max_size = packet_size.getSelection();

        shell.dispose();
      }

      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    final Button button_cancel = new Button(bottom_composite, SWT.PUSH);
    button_cancel.setText("Cancel");
    button_cancel.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        shell.dispose();
      }

      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    cversion.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        if (cversion.getSelectionIndex() == 2) {
          group_credentials_v2c.setEnabled(false);
          label1_v2c.setEnabled(false);
          group_credentials_v2c_value.setEnabled(false);

          label_sec.setEnabled(true);
          csec.setEnabled(true);

          group_credentials.setEnabled(true);
          label1.setEnabled(true);
          label2.setEnabled(true);
          label3.setEnabled(true);
          group_credentials_value.setEnabled(true);
          group_credentials_value2.setEnabled(true);
          group_credentials_value3.setEnabled(true);

          groups_composite.pack(true);
        } else {
          group_credentials_v2c.setEnabled(true);
          label1_v2c.setEnabled(true);
          group_credentials_v2c_value.setEnabled(true);

          label_sec.setEnabled(false);
          csec.setEnabled(false);

          group_credentials.setEnabled(false);
          label1.setEnabled(false);
          label2.setEnabled(false);
          label3.setEnabled(false);
          group_credentials_value.setEnabled(false);
          group_credentials_value2.setEnabled(false);
          group_credentials_value3.setEnabled(false);

          groups_composite.pack(true);
        }
      }
    });

    shell.pack(true);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch()) display.sleep();
  }
}
