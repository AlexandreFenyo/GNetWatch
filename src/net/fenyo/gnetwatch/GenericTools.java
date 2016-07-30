
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

import java.net.*;
import java.util.*;

import net.fenyo.gnetwatch.GUI.VisualElement;

import org.apache.commons.logging.*;
import org.apache.log4j.xml.*;

/**
 * General methods not dedicated to a particular application.
 * @author Alexandre Fenyo
 * @version $Id: GenericTools.java,v 1.16 2008/04/20 14:40:37 fenyo Exp $
 */

public class GenericTools {
  private static Log log = LogFactory.getLog(GenericTools.class);

  /**
   * Configure the global logging rules.
   * @param config Reference to the general configuration instance.
   * @return void.
   */
  static public void initLogEngine(final Config config) {
    // configure log4j
    DOMConfigurator.configure(config.getProperty("log4j"));
  }

  /**
   * Converts a string to an Inet4Address.
   * @param str source string.
   * @return Inet4Address address representing this string.
   */
  static public Inet4Address stringToInet4Address(final String str) throws UnknownHostException {
    /*
    final Matcher match = 
      Pattern.compile("([0-9]*)\\.([0-9]*)\\.([0-9]*)\\.([0-9]*)").matcher(str);
    if (match.find() == true) {
      return (Inet4Address) InetAddress.getByAddress(new byte [] {
          new Integer(match.group(1)).byteValue(), 
          new Integer(match.group(2)).byteValue(), 
          new Integer(match.group(3)).byteValue(), 
          new Integer(match.group(4)).byteValue(), 
      });
    } else return stringToInet4Address("0.0.0.0");
    */
    // shortest alternative to the previous code:
    return (Inet4Address) InetAddress.getByName(str);
  }

  /**
   * Converts a string to an Inet6Address.
   * @param str source string.
   * @return Inet6Address address representing this string.
   */
  static public Inet6Address stringToInet6Address(final String str) throws UnknownHostException {
    try {
      return (Inet6Address) InetAddress.getByName(str);
    } catch (final ClassCastException ex) {
      return null;
    }
  }

  /**
   * Converts a unsigned byte (encoded into a java signed byte) to a positive signed short.
   * @param ub unsigned byte encoded into a java signed byte.
   * @return short positive signed short.
   */
  static public short unsignedByteToShort(final byte ub) {
    final int foo = ub;
    return (short) (foo < 0 ? foo + 256 : foo);
  }

  /**
   * Converts an IP address to its string representation.
   * @param addr ipv4 address.
   * @return String ascii representation.
   */
  static public String inet4AddressToString(final Inet4Address addr) {
    try {
      byte bytes[] = addr.getAddress();
      return InetAddress.getByAddress(bytes).toString().substring(1);
    } catch (final UnknownHostException ex) {
      log.error("Exception", ex);
    }
    return "";
  }

  /**
   * Converts an IP address to its string representation.
   * @param addr ipv6 address.
   * @return String ascii representation.
   */
  static public String inet6AddressToString(final Inet6Address addr) {
    try {
      byte bytes[] = addr.getAddress();
      return InetAddress.getByAddress(bytes).toString().substring(1);
    } catch (final UnknownHostException ex) {
      log.error("Exception", ex);
    }
    return "";
  }

  /**
   * Returns the class A/B/C network address containing an IP address.
   * @param addr_str IPv4 address.
   * @return String network address.
   */
  static public String getNetFromAddress(final String addr_str) {
    try {
      final InetAddress addr;

      addr = InetAddress.getByName(addr_str);

      if (Inet6Address.class.isInstance(addr)) {
        // rfc-4291
        return "IPv6 range";
      } else if (Inet4Address.class.isInstance(addr)) {
        byte bytes[] = ((Inet4Address) addr).getAddress();
        if (unsignedByteToShort(bytes[0]) < 128) {
          // class A
          bytes[1] = 0;
          bytes[2] = 0;
          bytes[3] = 0;
          return InetAddress.getByAddress(bytes).toString().substring(1) + "/8";
        } else if (unsignedByteToShort(bytes[0]) < 192) {
          // class B
          bytes[2] = 0;
          bytes[3] = 0;
          return InetAddress.getByAddress(bytes).toString().substring(1) + "/16";
        } else if (unsignedByteToShort(bytes[0]) < 224) {
          // class C
          bytes[3] = 0;
          return InetAddress.getByAddress(bytes).toString().substring(1) + "/24";
        } else if (unsignedByteToShort(bytes[0]) < 248) {
          // class D
          return "224.0.0.0/4";
        } else {
          // class E
          return "248.0.0.0/4";
        }
      } else return null;
    } catch (final UnknownHostException ex) {
      log.error("Exception (addr_str=" + addr_str + ")", ex);
      return null;
    }
  }

  /**
   * Removes the part of a graph that is covered by another graph.
   * Note that the second graph need not to be a subgraph of the first one.
   * @param addr g1 initial graph.
   * @param addr g2 graph defining links to remove to the initial graph.
   * @return void.
   */
  static public void substractGraph(java.util.List<Pair<VisualElement, VisualElement>> g1, java.util.List<Pair<VisualElement, VisualElement>> g2) {
    final java.util.List<Pair<VisualElement, VisualElement>> gtemp = new LinkedList<Pair<VisualElement, VisualElement>>(g1);
    for (final Pair<VisualElement, VisualElement> p : gtemp)
      if (g2.contains(p)) g1.remove(p);
  }

  /**
   * Returns the full stack trace.
   * This function is a replacement to Throwable.getStackTrace() that writes "... XX [lines] more"
   * when there are too many entries in the stack trace.
   * @param ex exception.
   * @return String exception description with full stack trace.
   */
  static public String getFullExceptionStackTrace(final Throwable ex) {
    String result = "";
    
    for (final StackTraceElement st : ex.getStackTrace())
      result += st.toString();

    if (ex.getCause() != null) {
      result += "caused by\n";
      for (final StackTraceElement st : ex.getCause().getStackTrace())
        result += st.toString();
    }

    return result;
  }

  static public String formatNumericString(final Config config, final String value) {
    final StringBuffer result = new StringBuffer();
    for (int idx = value.length() - 1; idx >= 0; idx--) {
      if (idx != value.length() - 1 && (value.length() - 1 - idx) % 3 == 0) result.insert(0, config.getString("numeric_separator"));
      result.insert(0, value.charAt(idx));
    }
    return result.toString();
  }
}
