
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

import net.fenyo.gnetwatch.targets.Target;

/**
 * This utility class groups objects by pairs.
 * @author Alexandre Fenyo
 * @version $Id: Pair.java,v 1.7 2008/04/15 23:58:17 fenyo Exp $
 */

public class Pair<E, F> {
  private final E e;
  private final F f;

  /**
   * Constructor.
   * @param e first object.
   * @param f last object.
   */
  public Pair(final E e, final F f) {
    this.e = e;
    this.f = f;
  }

  /**
   * Returns the first object.
   * @param none.
   * @return E first object.
   */
  public E former() {
    return e;
  }

  /**
   * Returns the last object.
   * @param none.
   * @return F last object.
   */
  public F latter() {
    return f;
  }

  /**
   * Two pairs are equal if their respective first and last objects are equal.
   * @param o another pair.
   * @return boolean true in case of equality.
   */
  public boolean equals(final Object o) {
    if (this == o) return true;
    if ((o == null) || (o.getClass() != getClass())) return false;
    final Pair<E, F> pair = (Pair<E, F>) o;
    if (e == null && f == null) return pair.former() == null && pair.latter() == null;
    if (e == null) return pair.former() == null && f.equals(pair.latter());
    if (f == null) return pair.latter() == null && e.equals(pair.former());
    return e.equals(pair.former()) && f.equals(pair.latter());
  }

  /**
   * Returns a hashcode.
   * @param none.
   * @return int hashcode.
   */
  public int hashCode() {
    if (e == null && f == null) return 0;
    if (e != null) return e.hashCode();
    if (f != null) return f.hashCode();
    return e.hashCode() ^ f.hashCode();
  }
}
