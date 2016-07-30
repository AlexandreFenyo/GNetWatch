
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

package net.fenyo.gnetwatch.data;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.targets.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This event stores the last RTT computed.
 * @author Alexandre Fenyo
 * @version $Id: EventGenericSrc.java,v 1.4 2008/05/30 15:49:29 fenyo Exp $
 */

public class EventGenericSrc extends EventGeneric {
  private static Log log = LogFactory.getLog(EventGenericProcess.class);

  // persistent - not null
  private boolean reachable;

  // persistent - not null
  private int delay;

  // persistent - not null
  private int value1 = -1;

  // persistent - not null
  private int value2 = -1;

  // persistent - not null
  private int value3 = -1;

  // persistent - not null
  private int value4 = -1;

  // persistent - not null
  private int value5 = -1;

  // persistent - not null
  private int value6 = -1;

  // persistent - not null
  private int value7 = -1;

  // persistent - not null
  private int value8 = -1;

  // persistent - not null
  private int value9 = -1;

  // persistent - not null
  private int value10 = -1;

  // persistent - not null
  private String units = "";

  /**
   * Constructor.
   * @param reachable true if RTT < +inft
   * @param delay RTT if reachable is true
   */
  // Queue thread
  public EventGenericSrc(final boolean reachable, final int delay) {
    this.reachable = reachable;
    this.delay = delay;
  }

  public EventGenericSrc(final Date date, final boolean reachable, final int delay, final int value1, final int value2, final int value3, final int value4, final int value5, final int value6, final int value7, final int value8, final int value9, final int value10, final String units) {
    super(date);
    this.reachable = reachable;
    this.delay = delay;
    this.value1 = value1;
    this.value2 = value2;
    this.value3 = value3;
    this.value4 = value4;
    this.value5 = value5;
    this.value6 = value6;
    this.value7 = value7;
    this.value8 = value8;
    this.value9 = value9;
    this.value10 = value10;
    this.units = units;
  }

  public EventGenericSrc(final boolean reachable, final int delay, final int value1, final int value2, final int value3, final int value4, final int value5, final int value6, final int value7, final int value8, final int value9, final int value10, final String units) {
    this.reachable = reachable;
    this.delay = delay;
    this.value1 = value1;
    this.value2 = value2;
    this.value3 = value3;
    this.value4 = value4;
    this.value5 = value5;
    this.value6 = value6;
    this.value7 = value7;
    this.value8 = value8;
    this.value9 = value9;
    this.value10 = value10;
    this.units = units;
  }

  /**
   * Constructor.
   * @param reachable true if RTT < +inft
   */
  // Queue thread
  public EventGenericSrc(final boolean reachable) {
    this.reachable = reachable;
    this.delay = -1;
  }

  /**
   * Default constructor.
   * @param none.
   */
  public EventGenericSrc() {
    this.reachable = false;
    this.delay = -1;
  }

  /**
   * Returns an integer representation of the performance counter associated whith this event.
   * @param events every event.
   * @return int performance counter.
   */
  public int getIntValue() {
    return getDelay();
  }

  public void setIntValue(int value) {
    setDelay(value);
    setReachable(value < 0 ? false : true);
  }

  /**
   * Returns the RTT associated to this event.
   * @param none.
   * @return int RTT.
   */
  // Queue & AWT thread
  public int getDelay() {
    return delay;
  }

  public void setDelay(final int delay) {
    this.delay = delay;
  }

  public boolean isReachable() {
    return reachable;
  }

  public void setReachable(final boolean reachable) {
    this.reachable = reachable;
  }

  public int getValue1() {
    return value1;
  }

  public void setValue1(final int value) {
    this.value1 = value;
  }

  public int getValue2() {
    return value2;
  }

  public void setValue2(final int value) {
    this.value2 = value;
  }

  public int getValue3() {
    return value3;
  }

  public void setValue3(final int value) {
    this.value3 = value;
  }

  public int getValue4() {
    return value4;
  }

  public void setValue4(final int value) {
    this.value4 = value;
  }

  public int getValue5() {
    return value5;
  }

  public void setValue5(final int value) {
    this.value5 = value;
  }

  public int getValue6() {
    return value6;
  }

  public void setValue6(final int value) {
    this.value6 = value;
  }

  public int getValue7() {
    return value7;
  }

  public void setValue7(final int value) {
    this.value7 = value;
  }

  public int getValue8() {
    return value8;
  }

  public void setValue8(final int value) {
    this.value8 = value;
  }

  public int getValue9() {
    return value9;
  }

  public void setValue9(final int value) {
    this.value9 = value;
  }

  public int getValue10() {
    return value10;
  }

  public void setValue10(final int value) {
    this.value10 = value;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(final String units) {
    this.units = units;
  }
}
