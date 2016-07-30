
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

/**
 * An SNMP querier maintains SNMP parameters needed for the manager to talk to an agent.
 * @author Alexandre Fenyo
 * @version $Id: GenericQuerier.java,v 1.2 2008/05/22 19:46:21 fenyo Exp $
 */

public class GenericQuerier {
  private static Log log = LogFactory.getLog(GenericQuerier.class);

  // persistent
  private Long id;

  // persistent - not null
  private String title = "";

  // persistent - not null
  private String cmdline = "";

  // persistent - not null
  private String filename = "";

  // persistent - not null
  private String workdir = "";

  // persistent - not null
  private String unit = "";

  /**
   * Default constructor.
   * @param none.
   */
  public GenericQuerier() {}

  public Long getId() {
    return id;
  }

  protected void setId(final Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getCommandLine() {
    return cmdline;
  }

  public void setCommandLine(final String cmdline) {
    this.cmdline = cmdline;
  }

  public String getFileName() {
    return filename;
  }

  public void setFileName(final String filename) {
    this.filename = filename;
  }

  public String getWorkingDirectory() {
    return workdir;
  }

  public void setWorkingDirectory(final String workdir) {
    this.workdir = workdir;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(final String unit) {
    this.unit = unit;
  }

  public Object clone() {
    final GenericQuerier result = new GenericQuerier();

    result.setId(getId());
    result.setTitle(getTitle());
    result.setCommandLine(getCommandLine());
    result.setFileName(getFileName());
    result.setWorkingDirectory(getWorkingDirectory());
    result.setUnit(getUnit());

    return result;
  }
}
