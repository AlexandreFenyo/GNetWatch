
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


import org.hibernate.*;
import org.hibernate.cfg.*;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.targets.*;
import net.fenyo.gnetwatch.actions.*;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.data.*;

import org.apache.commons.logging.*;

/**
 * This class maintains object-relational mappings.
 * @author Alexandre Fenyo
 * @version $Id: Synchro.java,v 1.12 2008/04/21 23:07:12 fenyo Exp $
 */

public class Synchro {
  private static Log log = LogFactory.getLog(Synchro.class);

  private final Config config;
  private final SessionFactory sessionFactory;

  /**
   * Constructor.
   * Reads the configuration properties from the initialization file.
   * main thread
   * @param config configuration.
   */
  public Synchro(final Config config) {
	  this.config = config;

	  new Configuration().configure(new File("hibernate.cfg.xml"));

	  try {
      // Création de la SessionFactory à partir de hibernate.cfg.xml
	    sessionFactory = new Configuration().configure(new File("hibernate.cfg.xml")).buildSessionFactory();
	  } catch (Throwable ex) {
      // make sure we log the exception, as it might be swallowed
	    System.err.println("Initial SessionFactory creation failed." + ex);
      throw new ExceptionInInitializerError(ex);
	  }
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public void end() {
    sessionFactory.close();
  }
}
