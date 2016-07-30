
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
import java.util.*;
import java.text.*;
import org.hibernate.cfg.*;
import org.apache.commons.logging.*;

/**
 * Instances of this class maintain general parameters like configuration properties.
 * @author Alexandre Fenyo
 * @version $Id: Config.java,v 1.9 2008/04/15 23:58:17 fenyo Exp $
 */

public class Config {
  private static Log log = LogFactory.getLog(Config.class);

  // Configuration properties.
  private final Properties properties;

  // Localization state variables.
  private Locale locale = null;
  private ResourceBundle bundle = null;

  private boolean needEnd = false;

  private int debug_level = 0;

  /**
   * Constructor.
   * Reads the configuration properties from the initialization file.
   * main thread
   * @param none.
   * @throws IOException file not found.
   */
  public Config() throws IOException {
    properties = new Properties();
    properties.loadFromXML(new FileInputStream("config.xml"));

    locale = new Locale(getProperty("language"), getProperty("country"));
    bundle = ResourceBundle.getBundle("i18n", locale);
  }

  public int getDebugLevel() {
    return debug_level;
  }

  public void setDebugLevel(final int debug_level) {
    this.debug_level = debug_level;
  }

  /**
   * Declare that the application will exit soon.
   * main thread
   * @param none.
   * @return void.
   */
  // main thread
  public void setEnd() {
    needEnd = true;
  }

  /**
   * Checks the application state.
   * @param none.
   * @return boolean application state.
   */
  // Background, Capture, Queue, AwtGUI & main threads
  public boolean isEnd() {
    return needEnd;
  }

  /**
   * Gets a property value.
   * @param key.
   * @return String property value.
   */
  public String getProperty(final String key) {
    return properties.getProperty("net.fenyo." + key);
  }

  /**
   * Gets a property value.
   * @param key key.
   * @param dflt default value.
   * @return String property value.
   */
  public String getProperty(final String key, final String dflt) {
    return properties.getProperty("net.fenyo." + key, dflt);
  }

  /**
   * Returns the locale associated with this configuration.
   * @param none.
   * @return Locale locale.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Returns the i18n resource bundle associated with this configuration.
   * @param none.
   * @return ResourceBundle resource bundle.
   */
  public ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @return String locale dependant message.
   */
  public String getString(final String key) {
    return bundle.getString(key);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param params array of arguments to scatter in the i18n locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object [] params) {
    final MessageFormat formatter = new MessageFormat("");
    formatter.setLocale(locale);
    formatter.applyPattern(getString(key));
    return formatter.format(params);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param arg argument for locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object arg) {
    Object [] msgArgs = { arg };
    return getPattern(key, msgArgs);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param arg1 argument for locale dependant message.
   * @param arg2 argument for locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object arg1, final Object arg2) {
    Object [] msgArgs = { arg1, arg2 };
    return getPattern(key, msgArgs);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param arg1 argument for locale dependant message.
   * @param arg2 argument for locale dependant message.
   * @param arg3 argument for locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object arg1, final Object arg2, final Object arg3) {
    Object [] msgArgs = { arg1, arg2, arg3 };
    return getPattern(key, msgArgs);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param arg1 argument for locale dependant message.
   * @param arg2 argument for locale dependant message.
   * @param arg3 argument for locale dependant message.
   * @param arg4 argument for locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object arg1, final Object arg2, final Object arg3, final Object arg4) {
    Object [] msgArgs = { arg1, arg2, arg3, arg4 };
    return getPattern(key, msgArgs);
  }

  /**
   * Returns an i18n message.
   * @param key i18n key.
   * @param arg1 argument for locale dependant message.
   * @param arg2 argument for locale dependant message.
   * @param arg3 argument for locale dependant message.
   * @param arg4 argument for locale dependant message.
   * @param arg5 argument for locale dependant message.
   * @return String locale dependant message.
   */
  public String getPattern(final String key, final Object arg1, final Object arg2,
         final Object arg3, final Object arg4, final Object arg5) {
    Object [] msgArgs = { arg1, arg2, arg3, arg4, arg5 };
    return getPattern(key, msgArgs);
  }
}
