
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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.*;
import java.security.cert.*;
import java.text.*;

import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.ExternalCommand;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.data.EventGenericSrc;
import net.fenyo.gnetwatch.data.EventReachable;
import net.fenyo.gnetwatch.targets.*;

import org.dom4j.*;
import org.dom4j.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.hibernate.Session;

import org.apache.commons.logging.*;

/*
  synchronisation

occurences de synchronized (sur m�thodes et instances) :
  net/fenyo/gnetwatch/actions/ActionNmap.java
  net/fenyo/gnetwatch/actions/ActionSNMP.java
  net/fenyo/gnetwatch/actions/ExternalCommand.java
  net/fenyo/gnetwatch/activities/Background.java
  net/fenyo/gnetwatch/activities/Capture.java
  net/fenyo/gnetwatch/activities/CaptureManager.java
  net/fenyo/gnetwatch/activities/Queue.java
  net/fenyo/gnetwatch/data/DataView.java
  net/fenyo/gnetwatch/data/NmapView.java
  net/fenyo/gnetwatch/data/Views.java
  net/fenyo/gnetwatch/GUI/AwtGUI.java
  net/fenyo/gnetwatch/GUI/BasicComponent.java
  net/fenyo/gnetwatch/GUI/BytesReceivedComponent.java
  net/fenyo/gnetwatch/GUI/BytesSentComponent.java
  net/fenyo/gnetwatch/GUI/ChartComponent.java
  net/fenyo/gnetwatch/GUI/FloodComponent.java
  net/fenyo/gnetwatch/GUI/GUI.java
  net/fenyo/gnetwatch/GUI/HTTPComponent.java
  net/fenyo/gnetwatch/GUI/HTTPPagesComponent.java
  net/fenyo/gnetwatch/GUI/VisualElement.java
  net/fenyo/gnetwatch/SNMPQuerier.java
  net/fenyo/gnetwatch/targets/TargetIPv4.java
  net/fenyo/gnetwatch/targets/TargetIPv6.java
  net/fenyo/gnetwatch/targets/Target.java

verrous :
  GUI.sync_tree
  GUI.tab_folder
  GUI.GUI_created
  synchro
  ExternalCommand
  CaptureManager
  CaptureManager.capture_list
  CaptureManager.listeners
  Queue
  Queue.actions
  AwtGUI.frame_list
  BasicComponent.sync_value_per_vinterval
  BasicComponent.sync_update
  BasicComponent.events
  VisualElement.initialized
  SNMPQuerier.getSysDescr().invoked
  registered_components

threads :
  main: CommandLine.main()
  Interrupt: Background.run()
  GUI: GUI.run()
  Capture-*: Capture.run()
  Repaint: AwtGUI.run()
  icmp-*: PingQueue.run()
  snmp-*: SNMPQueue.run()
  flood-*: FloodQueue.run()
  http-*: HTTPQueue.run()
  nmap-*: NmapQueue.run()
  merge-1: MergeQueue.run()
  hsqldb: library thread (HSQLDB)
  DefaultUDPTransportMapping: library thread (SNMP4J)
  Timer-0: system thread

 */

/**
 * Manage command line arguments.
 * @author Alexandre Fenyo
 * @version $Id: CommandLine.java,v 1.43 2008/11/17 01:39:19 fenyo Exp $
 *
 * @todo nothing more.
 */

public class CommandLine {
  private static Log log = LogFactory.getLog(CommandLine.class);

  static public class NoCheckTrustManager implements X509TrustManager {
    private X509TrustManager myTrustManager;

    public NoCheckTrustManager() throws java.security.NoSuchAlgorithmException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        try {
//          trustManagerFactory.init((KeyStore) null);
          trustManagerFactory.init(KeyStore.getInstance(KeyStore.getDefaultType()));
          myTrustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
        } catch (KeyStoreException ex) {
          log.error("Exception", ex);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
      return myTrustManager.getAcceptedIssuers();
    }

    public void checkClientTrusted(final X509Certificate chain[], final String authType) throws CertificateException {}

    public void checkServerTrusted(final X509Certificate chain[], final String authType) throws CertificateException {}
  }

  // java -cp GNetWatchBundle.jar import source sandbox d:\import\gnetwatch.log
  // 20080530162420:<1;2;3;4;5;6;7;8;9;10;foo;bar>
  private static void importGenericSrc(final String[] args) throws IOException {
    // Get configuration properties
    final Config config = new Config();
    // Set debug level
    config.setDebugLevel(0);
    // Read general logging rules
    GenericTools.initLogEngine(config);
    // Initialize Object-Relational mapping
    final Synchro synchro = new Synchro(config);

    synchronized (synchro) {
      final Session session = synchro.getSessionFactory().getCurrentSession();
      session.beginTransaction();

      final java.util.List results =
        session.createQuery("from TargetGroup as group where group.item = ?").
        setString(0, args[2]).list();

      if (results.size() == 0) log.error("no such group");
      else {
        final TargetGroup group = (TargetGroup) results.get(0);
        final BufferedReader reader = new BufferedReader(new FileReader(args[3]));
        String line;
        while ((line = reader.readLine()) != null) {
          log.debug("[" + line + "]");

          Matcher match =
            Pattern.compile(".*?([0-9]{14})[^0-9]*?<([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([0-9]*);([^>]*)>.*?$").matcher(line);
          if (match.find()) {
            final String time = new String(match.group(1));
            final int value1 = new Integer(match.group(2));
            final int value2 = new Integer(match.group(3));
            final int value3 = new Integer(match.group(4));
            final int value4 = new Integer(match.group(5));
            final int value5 = new Integer(match.group(6));
            final int value6 = new Integer(match.group(7));
            final int value7 = new Integer(match.group(8));
            final int value8 = new Integer(match.group(9));
            final int value9 = new Integer(match.group(10));
            final int value10 = new Integer(match.group(11));
            final String units = match.group(12);

            final SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            final Date date;
            try {
              date = fmt.parse(time);
            } catch (final ParseException ex) {
              log.warn("exception while parsing date from line [" + line + "]", ex);
              continue;
            }

/*            final java.util.List results2 =
              session.createQuery("from EventGenericSrc as ev where ev.eventList = :event_list " +
                  "and ev.date = :date")
              .setLong("event_list",
                  group.getEventLists().get(EventGenericSrc.class.toString()).getId())
              .setDate("date", date)
              .list();*/
            final java.util.List results2 =
              session.createQuery("from EventGenericSrc as ev where ev.date = :date")
//              .setString("date", "2008-05-30 16:24:20.000000000")
              .setString("date", new java.sql.Date(date.getTime()).toString())
              .list();
log.debug("date en ascii:" + new java.sql.Date(date.getTime()).toString());

            if (results2.size() > 0) log.debug("Deja un evt comme ca");
            else group.addEvent(synchro, new EventGenericSrc(date, true, -1, value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, units));
          }

        }
      }

      session.getTransaction().commit();
    }

    // stop synchronizing
    synchro.end(); 
  }

  /**
   * General entry point.
   * @param args command line arguments.
   * @return void.
   * @throws IOException io exception.
   * @throws FileNotFoundException file not found.
   */
  public static void main(final String[] args)
    throws IOException, FileNotFoundException, InterruptedException, AlgorithmException {
    Config config = null;
    Synchro synchro = null;
    Background background = null;
    GUI gui = null;
    Main main = null;
    SNMPManager snmp_manager = null;
    CaptureManager capture_mgr = null;

    if (args.length > 0) {
      if (args.length == 4 && args[0].equals("import") && args[1].equals("source")) {
        importGenericSrc(args);
        return;
      }
      log.error("invalid arguments");
      System.exit(1);
    }

    // Get configuration properties
    config = new Config();

    // Set debug level
    // debug level 1: simulate hundreds of ping per second to check the DB and hibernate abilities to handle lots of events
    config.setDebugLevel(0);

    // Read general logging rules
    GenericTools.initLogEngine(config);
    log.info(config.getString("log_engine_initialized"));
    log.info(config.getString("begin"));

    /*
    final MessageBox dialog = new MessageBox(new Shell(new org.eclipse.swt.widgets.Display()),
        SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    // traduire
    dialog.setText("GNetWatch startup");
    dialog.setMessage("Database Selection:\ndo you want to erase the current database content ?");
    dialog.open();
    */

    // Initialize Object-Relational mapping
    synchro = new Synchro(config);

    // Do not check SSL certificates
    SSLContext ssl_context = null;
    try {
      ssl_context = SSLContext.getInstance("SSL");
      ssl_context.init(null, new TrustManager [] { new NoCheckTrustManager() }, new SecureRandom());
    } catch (final NoSuchAlgorithmException ex) {
      log.error("Exception", ex);
    } catch (final KeyManagementException ex) {
      log.error("Exception", ex);
    }
    HttpsURLConnection.setDefaultSSLSocketFactory(ssl_context.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier () {
      public final boolean verify(String hostname, SSLSession session) { 
        return true;
      }
    });
    
    // Initialize background processes management
    background = new Background(config);
    background.createBackgroundThread();

    // Initialize packet capture on every interface
    capture_mgr = new CaptureManager(config);

    // Initialize main processes management
    main = new Main(config, capture_mgr);

    // Build SNMP Manager
    snmp_manager = new SNMPManager();

    // Build GUI
    gui = new GUI(config, background, main, snmp_manager, synchro);
    main.setGUI(gui);
    capture_mgr.setGUI(gui);
    gui.waitForCreation();

    // Initial configuration
    gui.createFromXML(gui.getConfig().getProperty("initialobjects"));

    // Move the GUI to the top of the drawing order
    gui.showGUI();

    // merge events at startup
    background.informQueue("merge-1", gui);

    // Wait for the GUI to terminate
    gui.join();
    // The GUI is now closed
    log.info(config.getString("end"));

    // Stop every application thread
    config.setEnd();
    gui.end();
    background.end();
    capture_mgr.unRegisterAllListeners();

    // stop synchronizing
    synchro.end();
  }
}