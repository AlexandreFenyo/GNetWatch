
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

/**
 * An exception for errors specific to this software.
 * @author Alexandre Fenyo
 * @version $Id: AlgorithmException.java,v 1.6 2008/04/15 23:58:17 fenyo Exp $
 */

public class AlgorithmException extends GeneralException {
  public static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * Creates an AlgorithmException instance.
   * @param none.
   */
  public AlgorithmException() {
   super(); 
  }

  /**
   * Constructor.
   * Creates an AlgorithmException instance.
   * @param message message associated with this exception.
   * @param none.
   */
  public AlgorithmException(final String message) {
    super(message); 
   }

  /**
   * Constructor.
   * Creates an AlgorithmException instance.
   * @param message message associated with this exception.
   * @param exception exception associated with this exception.
   */
  public AlgorithmException(final String message, final Throwable cause) {
    super(message, cause); 
   }

  /**
   * Constructor.
   * Creates an AlgorithmException instance.
   * @param exception exception associated with this exception.
   */
  public AlgorithmException(final Throwable cause) {
    super(cause); 
   }
}
