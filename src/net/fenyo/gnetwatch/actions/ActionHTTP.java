
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

package net.fenyo.gnetwatch.actions;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.actions.Action.InterruptCause;
import net.fenyo.gnetwatch.activities.Background;
import net.fenyo.gnetwatch.data.*;
import net.fenyo.gnetwatch.targets.*;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * Instances of this action class can load any HTTP server
 * and create events of type EventHTTP to log the throughput.
 * @author Alexandre Fenyo
 * @version $Id: ActionHTTP.java,v 1.14 2008/04/27 21:44:21 fenyo Exp $
 */

public class ActionHTTP extends Action {
  private static Log log = LogFactory.getLog(ActionHTTP.class);

  private boolean interrupted = false;

  private String error_string = "";

  /**
   * Constructor.
   * @param target target this action works on.
   * @param background queue manager by which this action will add events.
   */
  // GUI thread
  // supports any thread
  public ActionHTTP(final Target target, final Background background) {
    super(target, background);
    setItem("http");
  }

  /**
   * Default constructor.
   * @param none.
   */
  // GUI thread
  // supports any thread
  public ActionHTTP() {
    setItem("http");
  }

  /**
   * Returns the preferred queue.
   * @param none.
   * @return String preferred queue.
   */
  // any thread
  public String getQueueName() {
    return "http";
  }

  /**
   * Returns the timeout associated with this action.
   * @param none.
   * @return long timeout.
   */
  // any thread
  // bug : au bout de ce tps en ms ca s'arrete
  public long getMaxDelay() {
    return 30000000;
  }

  /**
   * Asks this action to stop rapidely.
   * @param cause cause.
   * @return void.
   * @throws IOException IO exception.
   */
  // main & Background threads
  // supports any thread
  public void interrupt(final InterruptCause reason) {
    interrupted = true;
  }

  /**
   * Establishes the connections to the server.
   * @param idx number of connections to establish.
   * @param querier http/ftp parameters.
   * @param connections array of connections established.
   * @param streams streams associated to the connections.
   * @param sizes data sizes ready to be read on the connections.
   * @param url url to connect to.
   * @param proxy proxy to use.
   * @return number of bytes received.
   * @throws IOException IO exception.
   */
  private int connect(final int idx, final IPQuerier querier, final URLConnection [] connections,
      final InputStream [] streams, final int [] sizes, final URL url, final Proxy proxy) throws IOException {
    error_string = "";
    try {
      connections[idx] = querier.getUseProxy() ? url.openConnection(proxy) : url.openConnection();
      connections[idx].setUseCaches(false);
      connections[idx].connect();
      streams[idx] = connections[idx].getInputStream();
      sizes[idx] = connections[idx].getContentLength();

    } catch (final IOException ex) {

      streams[idx] = null;
      sizes[idx] = 0;

      int response_code = 0;
      try {
        response_code = ((HttpURLConnection) connections[idx]).getResponseCode();
      } catch (final ConnectException ex2) {
        getGUI().appendConsole(ex2.toString() + "<BR/>");
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException ex3) {}

        throw ex2;
      }

      error_string = "(http error " + response_code + ")";
      final InputStream error_stream = ((HttpURLConnection) connections[idx]).getErrorStream();
      if (error_stream == null) return 0;
      int nread, nread_tot = 0;
      String error_str = "";
      final byte [] error_buf = new byte [65536];
      while ((nread = error_stream.read(error_buf)) > 0) {
//        log.debug("error: " + new String(error_buf).substring(0, nread - 1));
        error_str += new String(error_buf);
        nread_tot += nread;
      }
      error_stream.close();
      return nread_tot;
    }
    return 0;
  }

  /**
   * Loads the server.
   * @param none.
   * @return void.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   * @see http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
   */
  // Queue thread
  // supports any thread
  public void invoke() throws IOException, InterruptedException {
    if (isDisposed() == true) return;

    try {
      super.invoke();

      IPQuerier querier;
      synchronized (getGUI().getSynchro()) {
        if (TargetIPv4.class.isInstance(getTarget())) {
          querier = (IPQuerier) ((TargetIPv4) getTarget()).getIPQuerier().clone();
        } else if (TargetIPv6.class.isInstance(getTarget())) {
          querier = (IPQuerier) ((TargetIPv6) getTarget()).getIPQuerier().clone();
        } else return;
      }

      final URL url = new URL(querier.getURL());
      final Proxy proxy = querier.getUseProxy() ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(querier.getProxyHost(), querier.getProxyPort())) : null;

      URLConnection [] connections = new URLConnection[querier.getNParallel()];
      InputStream [] streams = new InputStream[querier.getNParallel()];
      int [] sizes = new int [querier.getNParallel()];

      for (int idx = 0; idx < querier.getNParallel(); idx++)
        connect(idx, querier, connections, streams, sizes, url, proxy);

      final byte [] buf = new byte [65536];
      long last_time = System.currentTimeMillis();
      int bytes_received = 0;
      int pages_received = 0;

      while (true) {
        int available_for_every_connections = 0;

        for (int idx = 0; idx < querier.getNParallel(); idx++) {

          final int available = (streams[idx] != null) ? streams[idx].available() : 0;
          available_for_every_connections += available;

          if (available == 0) {
            if (sizes[idx] == 0) {
              if (streams[idx] != null) streams[idx].close();
              bytes_received += connect(idx, querier, connections, streams, sizes, url, proxy);
              pages_received++;
            }
          } else {
            final int nread = streams[idx].read(buf);
            switch (nread) {
            case -1:
              streams[idx].close();
              connect(idx, querier, connections, streams, sizes, url, proxy);
              pages_received++;
            break;

            case 0:
              log.error("0 byte read");
              for (InputStream foo : streams) if (foo != null) foo.close();
              return;

            default:
//              log.debug("read: " + new String(buf).substring(0, nread - 1));
              bytes_received += nread;
              sizes[idx] -= nread;
            }
          }

          if (System.currentTimeMillis() - last_time > 1000) {
            synchronized (getGUI().getSynchro()) {
              synchronized (getGUI().sync_tree) {
                final Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
                session.beginTransaction();
                try {
                  session.update(this);

                  getTarget().addEvent(new EventHTTP(new Double(((double) 8 * 1000 * bytes_received) /
                      (System.currentTimeMillis() - last_time)).intValue()));

                  getTarget().addEvent(new EventHTTPPages(new Double(((double) 1000 * pages_received) /
                      (System.currentTimeMillis() - last_time)).intValue()));

                  setDescription(
                        GenericTools.formatNumericString(getGUI().getConfig(),
                            "" + new Double(((double) 8 * 1000 * bytes_received) / (System.currentTimeMillis() - last_time)).intValue())
                        + " bit/s (" +
                        GenericTools.formatNumericString(getGUI().getConfig(), "" + new Double(((double) 1000 * pages_received) / (System.currentTimeMillis() - last_time)).intValue())
                        + " pages/sec)");
                  getGUI().setStatus(getGUI().getConfig().getPattern("bytes_http", bytes_received, querier.getAddress().toString().substring(1)) + " " + error_string);

                  last_time = System.currentTimeMillis();
                  bytes_received = 0;
                  pages_received = 0;

                  session.getTransaction().commit();
                  } catch (final Exception ex) {
                    log.error("Exception", ex);
                    session.getTransaction().rollback();
                  }
              }
            }
          }

          if (interrupted == true) {
            for (InputStream foo : streams) if (foo != null) foo.close();
            return;
          }
        }
        if (available_for_every_connections == 0) Thread.sleep(10);
      }
    } catch (final InterruptedException ex) {
      log.error("Exception", ex);
    }
  }

  /**
   * Called when this element is being removed.
   * @param none.
   * @return void.
   */
  protected void disposed() {
    // remove us from the flood queue
    super.disposed();

    // interrupt if currently running
    interrupt(InterruptCause.removed);
  }
}
