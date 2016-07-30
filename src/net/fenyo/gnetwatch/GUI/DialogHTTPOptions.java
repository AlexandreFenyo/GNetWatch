
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
 * @version $Id: DialogHTTPOptions.java,v 1.3 2008/05/21 16:46:06 fenyo Exp $
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

public class DialogHTTPOptions extends Dialog {
  private static Log log = LogFactory.getLog(DialogHTTPOptions.class);

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

  Label label = null;
  Label label_port = null;

  private boolean use_proxy = false;
  private String proxy_host = "";
  private int proxy_port = 3128;
  private String URL = "";
  // private boolean reconnect = false;
  private int nparallel = 1;

  /**
   * Constructor.
   * @param gui current GUI instance.
   * @param parent parent shell.
   */
  public DialogHTTPOptions(final GUI gui, final Shell parent) {
    super(parent, 0);
    this.gui = gui;
  }

  /**
   * Checks that we use a proxy.
   * @param none.
   * @return boolean true if using a proxy.
   */
  public boolean getUseProxy() {
    return use_proxy;
  }

  /**
   * Gets the proxy name.
   * @param none.
   * @return String proxy name.
   */
  public String getProxyHost() {
    return proxy_host;
  }

  /**
   * Gets the TCP proxy port.
   * @param none.
   * @return int TCP proxy port.
   */
  public int getProxyPort() {
    return proxy_port;
  }

  /**
   * Gets the URL to connect to.
   * @param none.
   * @return String URL to connect to.
   */
  public String getURL() {
    return URL;
  }

  /**
   * Checks that we should reconnect after each HTTP transaction.
   * @param none.
   * @return boolean true if we should reconnect after each HTTP transaction.
   */
/*
  public boolean getReconnect() {
    return reconnect;
  }
*/

  /**
   * Returns the number of concurrent HTTP sessions.
   * @param none.
   * @return int number of concurrent HTTP sessions.
   */
  public int getNParallel() {
    return nparallel;
  }

  /**
   * Sets it to true to use a proxy.
   * @param use_proxy true to use a proxy.
   * @return void.
   */
  public void setUseProxy(final boolean use_proxy) {
    this.use_proxy = use_proxy;
  }

  /**
   * Sets the proxy host name.
   * @param proxy_host proxy host name.
   * @return void.
   */
  public void setProxyHost(final String proxy_host) {
    this.proxy_host = proxy_host;
  }

  /**
   * Sets the proxy TCP port.
   * @param proxy_port proxy TCP port.
   * @return void.
   */
  public void setProxyPort(final int proxy_port) {
    this.proxy_port = proxy_port;
  }

  /**
   * Sets the destination URL.
   * @param URL destination URL.
   * @return void.
   */
  public void setURL(final String URL) {
    this.URL = URL;
  }

  /**
   * Sets it to true to reconnect after each transaction.
   * @param reconnect true to reconnect after each transaction.
   * @return void.
   */
/*
  public void setReconnect(final boolean reconnect) {
    this.reconnect = reconnect;
  }
*/

  /**
   * Sets the number of concurrent HTTP transactions.
   * @param nparallel number of concurrent transactions.
   * @return void.
   */
  public void setNParallel(final int nparallel) {
    this.nparallel = nparallel;
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
    shell.setText(gui.getConfig().getString("gnetwatch_http_options"));
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
    group_network_parameters.setText(gui.getConfig().getString("http_parameters"));

    final Label label2 = new Label(group_network_parameters, SWT.SHADOW_IN);
    label2.setText("URL");
    label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_URL_value = new Text(group_network_parameters, SWT.SINGLE);
    group_URL_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_URL_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_URL_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_URL_value.setText(URL);
    final GC gc = new GC(group_URL_value);
    ((GridData) (group_URL_value.getLayoutData())).widthHint = gc.stringExtent("                                            ").x;

    label = new Label(group_network_parameters, SWT.SHADOW_IN);
    label.setText(gui.getConfig().getString("proxy_host"));
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Text group_proxyhost_value = new Text(group_network_parameters, SWT.SINGLE);
    group_proxyhost_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_proxyhost_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    group_proxyhost_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    group_proxyhost_value.setText(proxy_host);
    ((GridData) (group_proxyhost_value.getLayoutData())).widthHint = gc.stringExtent("                                            ").x;

    label_port = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_port.setText(gui.getConfig().getString("proxy_port"));
    label_port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner group_port_value = new Spinner(group_network_parameters, SWT.WRAP);
    group_port_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_port_value.setMinimum(1);
    group_port_value.setMaximum(65535);
    group_port_value.setSelection(this.proxy_port);
    ((GridData) (group_port_value.getLayoutData())).widthHint = gc.stringExtent("                                       ").x;

    final Label label_nparallel = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_nparallel.setText(gui.getConfig().getString("nparallel"));
    label_nparallel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Spinner group_parallel_value = new Spinner(group_network_parameters, SWT.WRAP);
    group_parallel_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    group_parallel_value.setMinimum(1);
    group_parallel_value.setMaximum(1000);
    group_parallel_value.setSelection(this.nparallel);
    ((GridData) (group_parallel_value.getLayoutData())).widthHint = gc.stringExtent("                                       ").x;

    final Label label_useproxy = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_useproxy.setText(gui.getConfig().getString("use_proxy"));
    label_useproxy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Button group_useproxy_value = new Button(group_network_parameters, SWT.CHECK);
    group_useproxy_value.setSelection(use_proxy);
    group_useproxy_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));

    /*
    final Label label_reconnect = new Label(group_network_parameters, SWT.SHADOW_IN);
    label_reconnect.setText(gui.getConfig().getString("reconnect"));
    label_reconnect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Button group_reconnect_value = new Button(group_network_parameters, SWT.CHECK);
    group_reconnect_value.setSelection(reconnect);
    group_reconnect_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    */

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
    final DialogHTTPOptions _this = this;
    button_ok.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        _this.use_proxy = group_useproxy_value.getSelection();
        _this.proxy_host = group_proxyhost_value.getText();
        _this.proxy_port = group_port_value.getSelection();
        _this.URL = group_URL_value.getText();
        _this.nparallel = group_parallel_value.getSelection();
        _this.use_proxy = group_useproxy_value.getSelection();
        // _this.reconnect = group_reconnect_value.getSelection();

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

    group_useproxy_value.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(final SelectionEvent e) {
        widgetSelected(e);
      }
      public void widgetSelected(final SelectionEvent e) {
        group_proxyhost_value.setEnabled(group_useproxy_value.getSelection());
        label.setEnabled(group_useproxy_value.getSelection());
        label_port.setEnabled(group_useproxy_value.getSelection());
        group_port_value.setEnabled(group_useproxy_value.getSelection());
      }
    });

    group_proxyhost_value.setEnabled(getUseProxy());
    label.setEnabled(getUseProxy());
    label_port.setEnabled(getUseProxy());
    group_port_value.setEnabled(getUseProxy());

    shell.pack(true);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch()) display.sleep();
  }
}
