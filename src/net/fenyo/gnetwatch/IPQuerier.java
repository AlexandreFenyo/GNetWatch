
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

package net.fenyo.gnetwatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.io.*;

/**
 * Instances of this class maintain IP options associated to IP targets.
 * @author Alexandre Fenyo
 * @version $Id: IPQuerier.java,v 1.15 2008/04/23 00:46:42 fenyo Exp $
 */

public class IPQuerier {
  private static Log log = LogFactory.getLog(IPQuerier.class);

  // persistent
  private Long id;

  // persistent - not null
  private InetAddress address;

  // persistent - not null
  private int tos = 0;
  // persistent - not null
  private int port_src = 10000;
  // persistent - not null
  private int port_dst = 10000;
  // persistent - not null
  private int pdu_max_size = 1400;

  // persistent - not null
  private boolean use_proxy = false;
  // persistent - not null
  private boolean reconnect = false;
  // persistent - not null
  private String proxy_host = "";
  // persistent - not null
  private int proxy_port = 3128;
  // persistent - not null
  private String URL = "";
  // persistent - not null
  private int nparallel = 1;

  /**
   * Constructor.
   * @param address target address.
   */
  public IPQuerier(final InetAddress address) {
    this.address = address;
    if (Inet4Address.class.isInstance(address))
      this.URL = "http://" + address.toString().substring(1) + ":80/";
    if (Inet6Address.class.isInstance(address))
      this.URL = "http://[" + address.toString().substring(1) + "]:80/";
    parametersHaveChanged();
  }

  /**
   * Default constructor.
   * @param none.
   */
  public IPQuerier() {
    address = null;
    URL = null;
  }

  public Long getId() {
    return id;
  }

  protected void setId(final Long id) {
    this.id = id;
  }

  /**
   * Must be called after some setters have been called.
   * @param none.
   * @return void.
   */
  public void update() {
    parametersHaveChanged();
  }

  /**
   * Must be called after some setters have been called.
   * @param none.
   * @return void.
   */
  // final since it is invoked from the constructor
  private final void parametersHaveChanged() {}

  /**
   * Returns the TOS attribute.
   * @param none.
   * @return int TOS attribute.
   */
  public int getTos() {
    return tos;
  }

  /**
   * Returns the address attribute.
   * @param none.
   * @return InetAddress address attribute.
   */
  public InetAddress getAddress() {
    return address;
  }

  public void setAddress(final InetAddress address) {
    this.address = address;
  }

  /**
   * Returns the source port attribute.
   * @param none.
   * @return int source port attribute.
   */
  public int getPortSrc() {
    return port_src;
  }

  /**
   * Returns the destination port attribute.
   * @param none.
   * @return int destination port attribute.
   */
  public int getPortDst() {
    return port_dst;
  }

  /**
   * Returns the PDU maximum size attribute.
   * @param none.
   * @return int PDU maximum size attribute.
   */
  public int getPDUMaxSize() {
    return pdu_max_size;
  }

  /**
   * Checks that we must use the proxy.
   * @param none.
   * @return boolean true if we must use the proxy.
   */
  public boolean getUseProxy() {
    return use_proxy;
  }

  /**
   * Checks that we must make one connection per GET.
   * @param none.
   * @return boolean true if we must make one connection per GET.
   */
  public boolean getReconnect() {
    return reconnect;
  }

  /**
   * Returns the proxy host name.
   * @param none.
   * @return String proxy host name.
   */
  public String getProxyHost() {
    return proxy_host;
  }

  /**
   * Returns the TCP proxy port.
   * @param none.
   * @return String TCP proxy port.
   */
  public int getProxyPort() {
    return proxy_port;
  }

  /**
   * Returns the URL to connect to.
   * @param none.
   * @return String URL to connect to.
   */
  public String getURL() {
    return URL;
  }

  /**
   * Returns the number of simultaneous sessions.
   * @param none.
   * @return int number of simultaneous sessions..
   */
  public int getNParallel() {
    return nparallel;
  }

  /**
   * Set the TOS attribute.
   * @param tos TOS attribute.
   * @return void.
   */
  public void setTos(final int tos) {
    this.tos = tos;
  }

  /**
   * Set the source port attribute.
   * @param port_src source_port attribute.
   * @return void.
   */
  public void setPortSrc(final int port_src) {
    this.port_src = port_src;
  }

  /**
   * Set the destination port attribute.
   * @param port_dst destination_port attribute.
   * @return void.
   */
  public void setPortDst(final int port_dst) {
    this.port_dst = port_dst;
  }

  /**
   * Set the PDU maximum size attribute.
   * @param pdu_max_size PDU maximum size attribute.
   * @return void.
   */
  public void setPDUMaxSize(final int pdu_max_size) {
    this.pdu_max_size = pdu_max_size;
  }

  /**
   * Sets it to true if we must use the proxy.
   * @param use_proxy true if we must use the proxy.
   * @return void.
   */
  public void setUseProxy(final boolean use_proxy) {
    this.use_proxy = use_proxy;
  }

  /**
   * Sets it to true if we must make a new connection for each GET.
   * @param reconnect true if we must make a new connection for each GET.
   * @return void.
   */
  public void setReconnect(final boolean reconnect) {
    this.reconnect = reconnect;
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
   * Sets the TCP proxy port.
   * @param proxy_port TCP proxy port.
   * @return void.
   */
  public void setProxyPort(final int proxy_port) {
    this.proxy_port = proxy_port;
  }

  /**
   * Sets the URL to connect to.
   * @param URL URL to connect to.
   * @return void.
   */
  public void setURL(final String URL) {
    this.URL = URL;
  }

  /**
   * Sets the number of simultaneous sessions.
   * @param nparallel number of simultaneous sessions.
   * @return void. 
   */
  public void setNParallel(final int nparallel) {
    this.nparallel = nparallel;
  }

  public Object clone() {
    final IPQuerier result = new IPQuerier();

    result.setId(getId());
    result.setTos(getTos());
    result.setAddress(getAddress());
    result.setPortSrc(getPortSrc());
    result.setPortDst(getPortDst());
    result.setPDUMaxSize(getPDUMaxSize());
    result.setUseProxy(getUseProxy());
    result.setReconnect(getReconnect());
    result.setProxyHost(getProxyHost());
    result.setProxyPort(getProxyPort());
    result.setURL(getURL());
    result.setNParallel(getNParallel());

    return result;
  }
}
