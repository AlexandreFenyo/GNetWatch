
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

package net.fenyo.gnetwatch.activities;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.actions.ExternalCommand;
import net.fenyo.gnetwatch.data.EventReachable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.*;
import org.dom4j.io.*;

/*
 * XPATH doc: http://xmlfr.org/w3c/TR/xpath/
 * XPATH ex.: IP src address : "string(//field[@name='ip.addr'][1]/@show)"
 * PDML packet example:
  <packet>
  <proto name="geninfo" pos="0" showname="General information" size="74">
    <field name="num" pos="0" show="1" showname="Number" value="1" size="74"/>
    <field name="len" pos="0" show="74" showname="Packet Length" value="4a" size="74"/>
    <field name="caplen" pos="0" show="74" showname="Captured Length" value="4a" size="74"/>
    <field name="timestamp" pos="0" show="Dec 27, 2006 18:30:17.657143000" showname="Captured Time" value="1167240617.657143000" size="74"/>
  </proto>
  <proto name="frame" showname="Frame 1 (74 bytes on wire, 74 bytes captured)" size="74" pos="0">
    <field name="frame.marked" showname="Frame is marked: False" hide="yes" size="0" pos="0" show="0"/>
    <field name="frame.time" showname="Arrival Time: Dec 27, 2006 18:30:17.657143000" size="0" pos="0" show="Dec 27, 2006 18:30:17.657143000"/>
    <field name="frame.time_delta" showname="Time delta from previous packet: 0.000000000 seconds" size="0" pos="0" show="0.000000000"/>
    <field name="frame.time_relative" showname="Time since reference or first frame: 0.000000000 seconds" size="0" pos="0" show="0.000000000"/>
    <field name="frame.number" showname="Frame Number: 1" size="0" pos="0" show="1"/>
    <field name="frame.pkt_len" showname="Packet Length: 74 bytes" size="0" pos="0" show="74"/>
    <field name="frame.cap_len" showname="Capture Length: 74 bytes" size="0" pos="0" show="74"/>
    <field name="frame.protocols" showname="Protocols in frame: eth:ip:tcp" size="0" pos="0" show="eth:ip:tcp"/>
  </proto>
  <proto name="eth" showname="Ethernet II, Src: Netgear_fb:43:4f (00:0f:b5:fb:43:4f), Dst: AsustekC_54:cf:69 (00:11:d8:54:cf:69)" size="14" pos="0">
    <field name="eth.dst" showname="Destination: AsustekC_54:cf:69 (00:11:d8:54:cf:69)" size="6" pos="0" show="00:11:d8:54:cf:69" value="0011d854cf69"/>
    <field name="eth.src" showname="Source: Netgear_fb:43:4f (00:0f:b5:fb:43:4f)" size="6" pos="6" show="00:0f:b5:fb:43:4f" value="000fb5fb434f"/>
    <field name="eth.addr" showname="Source or Destination Address: AsustekC_54:cf:69 (00:11:d8:54:cf:69)" hide="yes" size="6" pos="0" show="00:11:d8:54:cf:69" value="0011d854cf69"/>
    <field name="eth.addr" showname="Source or Destination Address: Netgear_fb:43:4f (00:0f:b5:fb:43:4f)" hide="yes" size="6" pos="6" show="00:0f:b5:fb:43:4f" value="000fb5fb434f"/>
    <field name="eth.type" showname="Type: IP (0x0800)" size="2" pos="12" show="0x0800" value="0800"/>
  </proto>
  <proto name="ip" showname="Internet Protocol, Src: 192.168.0.53 (192.168.0.53), Dst: 192.168.0.29 (192.168.0.29)" size="20" pos="14">
    <field name="ip.version" showname="Version: 4" size="1" pos="14" show="4" value="45"/>
    <field name="ip.hdr_len" showname="Header length: 20 bytes" size="1" pos="14" show="20" value="45"/>
    <field name="ip.dsfield" showname="Differentiated Services Field: 0x00 (DSCP 0x00: Default; ECN: 0x00)" size="1" pos="15" show="0" value="00">
      <field name="ip.dsfield.dscp" showname="0000 00.. = Differentiated Services Codepoint: Default (0x00)" size="1" pos="15" show="0x00" value="0" unmaskedvalue="00"/>
      <field name="ip.dsfield.ect" showname=".... ..0. = ECN-Capable Transport (ECT): 0" size="1" pos="15" show="0" value="0" unmaskedvalue="00"/>
      <field name="ip.dsfield.ce" showname=".... ...0 = ECN-CE: 0" size="1" pos="15" show="0" value="0" unmaskedvalue="00"/>
    </field>
    <field name="ip.len" showname="Total Length: 60" size="2" pos="16" show="60" value="003c"/>
    <field name="ip.id" showname="Identification: 0xad9f (44447)" size="2" pos="18" show="0xad9f" value="ad9f"/>
    <field name="ip.flags" showname="Flags: 0x04 (Don&apos;t Fragment)" size="1" pos="20" show="0x04" value="40">
      <field name="ip.flags.rb" showname="0... = Reserved bit: Not set" size="1" pos="20" show="0" value="0" unmaskedvalue="40"/>
      <field name="ip.flags.df" showname=".1.. = Don&apos;t fragment: Set" size="1" pos="20" show="1" value="1" unmaskedvalue="40"/>
      <field name="ip.flags.mf" showname="..0. = More fragments: Not set" size="1" pos="20" show="0" value="0" unmaskedvalue="40"/>
    </field>
    <field name="ip.frag_offset" showname="Fragment offset: 0" size="2" pos="20" show="0" value="4000"/>
    <field name="ip.ttl" showname="Time to live: 64" size="1" pos="22" show="64" value="40"/>
    <field name="ip.proto" showname="Protocol: TCP (0x06)" size="1" pos="23" show="0x06" value="06"/>
    <field name="ip.checksum" showname="Header checksum: 0x0b7a [correct]" size="2" pos="24" show="0x0b7a" value="0b7a"/>
    <field name="ip.src" showname="Source: 192.168.0.53 (192.168.0.53)" size="4" pos="26" show="192.168.0.53" value="c0a80035"/>
    <field name="ip.addr" showname="Source or Destination Address: 192.168.0.53 (192.168.0.53)" hide="yes" size="4" pos="26" show="192.168.0.53" value="c0a80035"/>
    <field name="ip.src_host" showname="Source Host: 192.168.0.53" hide="yes" size="4" pos="26" show="192.168.0.53" value="c0a80035"/>
    <field name="ip.host" showname="Source or Destination Host: 192.168.0.53" hide="yes" size="4" pos="26" show="192.168.0.53" value="c0a80035"/>
    <field name="ip.dst" showname="Destination: 192.168.0.29 (192.168.0.29)" size="4" pos="30" show="192.168.0.29" value="c0a8001d"/>
    <field name="ip.addr" showname="Source or Destination Address: 192.168.0.29 (192.168.0.29)" hide="yes" size="4" pos="30" show="192.168.0.29" value="c0a8001d"/>
    <field name="ip.dst_host" showname="Destination Host: 192.168.0.29" hide="yes" size="4" pos="30" show="192.168.0.29" value="c0a8001d"/>
    <field name="ip.host" showname="Source or Destination Host: 192.168.0.29" hide="yes" size="4" pos="30" show="192.168.0.29" value="c0a8001d"/>
  </proto>
  <proto name="tcp" showname="Transmission Control Protocol, Src Port: 34604 (34604), Dst Port: 6001 (6001), Seq: 0, Ack: 0, Len: 0" size="40" pos="34">
    <field name="tcp.srcport" showname="Source port: 34604 (34604)" size="2" pos="34" show="34604" value="872c"/>
    <field name="tcp.dstport" showname="Destination port: 6001 (6001)" size="2" pos="36" show="6001" value="1771"/>
    <field name="tcp.port" showname="Source or Destination Port: 34604" hide="yes" size="2" pos="34" show="34604" value="872c"/>
    <field name="tcp.port" showname="Source or Destination Port: 6001" hide="yes" size="2" pos="36" show="6001" value="1771"/>
    <field name="tcp.len" showname="TCP Segment Len: 0" hide="yes" size="4" pos="34" show="0" value="872c1771"/>
    <field name="tcp.seq" showname="Sequence number: 0    (relative sequence number)" size="4" pos="38" show="0" value="d64b4fd4"/>
    <field name="tcp.hdr_len" showname="Header length: 40 bytes" size="1" pos="46" show="40" value="a0"/>
    <field name="tcp.flags" showname="Flags: 0x00c2 (SYN, ECN, CWR)" size="1" pos="47" show="0xc2" value="c2">
      <field name="tcp.flags.cwr" showname="1... .... = Congestion Window Reduced (CWR): Set" size="1" pos="47" show="1" value="1" unmaskedvalue="c2"/>
      <field name="tcp.flags.ecn" showname=".1.. .... = ECN-Echo: Set" size="1" pos="47" show="1" value="1" unmaskedvalue="c2"/>
      <field name="tcp.flags.urg" showname="..0. .... = Urgent: Not set" size="1" pos="47" show="0" value="0" unmaskedvalue="c2"/>
      <field name="tcp.flags.ack" showname="...0 .... = Acknowledgment: Not set" size="1" pos="47" show="0" value="0" unmaskedvalue="c2"/>
      <field name="tcp.flags.push" showname=".... 0... = Push: Not set" size="1" pos="47" show="0" value="0" unmaskedvalue="c2"/>
      <field name="tcp.flags.reset" showname=".... .0.. = Reset: Not set" size="1" pos="47" show="0" value="0" unmaskedvalue="c2"/>
      <field name="tcp.flags.syn" showname=".... ..1. = Syn: Set" size="1" pos="47" show="1" value="1" unmaskedvalue="c2"/>
      <field name="tcp.flags.fin" showname=".... ...0 = Fin: Not set" size="1" pos="47" show="0" value="0" unmaskedvalue="c2"/>
    </field>
    <field name="tcp.window_size" showname="Window size: 5840" size="2" pos="48" show="5840" value="16d0"/>
    <field name="tcp.checksum" showname="Checksum: 0xfd91 [correct]" size="2" pos="50" show="0xfd91" value="fd91"/>
    <field show="Options: (20 bytes)" size="20" pos="54" value="020405b40402080a0a39e24b0000000001030300">
      <field name="tcp.options.mss" showname="TCP MSS Option: True" hide="yes" size="4" pos="54" show="1" value="020405b4"/>
      <field name="tcp.options.mss_val" showname="Maximum segment size: 1460 bytes" size="4" pos="54" show="1460" value="020405b4"/>
      <field show="SACK permitted" size="2" pos="58" value="0402"/>
      <field name="tcp.options.time_stamp" showname="TCP Time Stamp Option: True" hide="yes" size="10" pos="60" show="1" value="080a0a39e24b00000000"/>
      <field show="Time stamp: tsval 171565643, tsecr 0" size="10" pos="60" value="080a0a39e24b00000000"/>
      <field show="NOP" size="1" pos="70" value="01"/>
      <field name="tcp.options.wscale" showname="TCP Window Scale Option: True" hide="yes" size="3" pos="71" show="1" value="030300"/>
      <field name="tcp.options.wscale_val" showname="Window scale: 0 (multiply by 1)" size="3" pos="71" show="0" value="030300"/>
    </field>
  </proto>
</packet>
 */

/**
 * This class captures Ethernet frames using tethereal on a single layer-2 interface.
 * The frames are parsed with SAX.
 * @author Alexandre Fenyo
 * @version $Id: Capture.java,v 1.14 2008/04/15 23:58:17 fenyo Exp $
 */

public class Capture implements Runnable {
  private static Log log = LogFactory.getLog(Capture.class);

  final SAXReader reader = new SAXReader();

  private final Config config;
  private ExternalCommand cmd;
  private CaptureManager manager;

  private boolean forked = false;

  private Thread capture_thread = null;

  private boolean must_end = false;

  /**
   * Constructor.
   * GUI thread.
   * @param config configuration.
   * @param manager capture manager this instance works for.
   * @param device device this instance captures frames from.
   * @param filter capture filter to apply.
   */
  // while debugging, sub processes may not die, so use the following DOS command line to terminate all running tethereal.exe :
  // wmic process where Name='tethereal.exe' call terminate
  public Capture(final Config config, final CaptureManager manager, final int device, final String filter) {
    this.config = config;
    this.manager = manager;
    // to convert device to a string, we use the following expression : "" + device
//    cmd = new ExternalCommand(new String [] { "tethereal", "-i", "" + device, "-T", "psml" }, true);
    cmd = new ExternalCommand(new String [] { "tethereal", "-i", "" + device, "-T", "pdml", "-R", "\"" + filter + "\"" }, true);
  }

  /**
   * Lists all available devices.
   * @param none.
   * @return list of device names.
   * @throws InterruptedException exception.
   */
  // GUI thread
  public static String [] listDevices() throws InterruptedException {
    final String devices =
      new ExternalCommand(new String [] { "tethereal", "-D" }, true).runStdout();
    return devices == null ? null : devices.split("\n");
  }

  /**
   * Starts the capture thread.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void createCaptureThread() {
    capture_thread = new Thread(this, "Capture Thread");
    capture_thread.start();
  }

  /**
   * Stops the capture thread and waits for its end.
   * @param none.
   * @return void.
   * @throws InterruptedException exception.
   */
  // if InterruptedException is thrown, the thread may not be ended
  // GUI thread
  public void end() throws InterruptedException {
    must_end = true;
    if (capture_thread != null) {
      capture_thread.interrupt();
    }

    while (!forked) Thread.sleep(100);

    try {
      cmd.end();
    } catch (final IOException ex) {
      log.error("Exception", ex);
    }

    capture_thread.join();
  }

  /**
   * Gives the next frame to the manager.
   * @param packet next frame.
   * @return void.
   * @throws DocumentException SAX parse exception.
   */
  // must be called from the Capture thread since SAXReader is not synchronized
  // Capture thread
  private void handlePacket(final StringBuffer packet) throws DocumentException {
    try {
      final Document document = reader.read(new StringReader(packet.toString()));
      manager.handlePacket(document);
    } catch (final DocumentException ex) {
      log.warn("packet: {" + packet + "}");
      throw ex;
    }
  }

  /**
   * Reads tethereal standard output and extracts frames one by one.
   * @param none.
   * @return void.
   */
  // Capture thread
  public void run() {
    final StringBuffer packet = new StringBuffer();

    try {
      cmd.fork();
      forked = true;

      while (!config.isEnd() && !must_end) {
        // on doit pouvoir optimiser en utilisant un StringBuffer pour str
        final String str = cmd.readLineStdout();
        if (str == null) break;
        packet.append(str);
        // log.debug("[" + str + "]");
        // replaces invalid XML characters with ' '
        for (int idx = 0; idx < packet.length(); idx++)
          // http://www.w3.org/TR/REC-xml/#charsets
          if (packet.charAt(idx) != 9 && packet.charAt(idx) != 10 &&
              packet.charAt(idx) != 13 && packet.charAt(idx) < 32)
            packet.setCharAt(idx, ' ');

        if (str.contains("</packet>")) {
          try {
            handlePacket(packet);
          } catch (final DocumentException ex) {
            log.warn("Exception", ex);
          }
          packet.setLength(0);
        }
      }
    } catch (final IOException ex) {
      log.warn("Exception", ex);
    } catch (final InterruptedException ex) {
      // terminate the thread
    } finally {
      forked = true;
    }
  }
}
