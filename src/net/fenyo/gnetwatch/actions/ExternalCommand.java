
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

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Instances of this class launch and manage processes outside of the JVM.
 * @author Alexandre Fenyo
 * @version $Id: ExternalCommand.java,v 1.17 2008/05/22 19:46:21 fenyo Exp $
 */

public class ExternalCommand {
  private static Log log = LogFactory.getLog(ExternalCommand.class);

  private boolean merge = false;

  final private String [] cmdLine;
  final private String directory;

  final private StringBuffer sb = new StringBuffer();

  private Process process = null;
  private BufferedReader reader = null;
  private BufferedReader errReader = null;

  /**
   * Creates an ExternalCommand instance and saves the command line.
   * any thread.
   * @param cmdLine command line.
   * @param directory path to the current directory for the script to be launched.
   */
  public ExternalCommand(final String [] cmdLine, final String directory) {
    this.cmdLine = cmdLine;
    this.directory = directory;
  }

  /**
   * Creates an ExternalCommand instance and saves the command line.
   * any thread.
   * @param cmdLine command line.
   */
  // any thread
  public ExternalCommand(final String [] cmdLine) {
    this.cmdLine = cmdLine;
    this.directory = System.getProperty("java.io.tmpdir");
  }

  /**
   * Creates an ExternalCommand instance and saves the command line.
   * any thread.
   * @param cmdLine command line.
   * @param merge merge standard output and standard error.
   */
  // any thread
  public ExternalCommand(final String [] cmdLine, final boolean merge) {
    this.merge = merge;
    this.cmdLine = cmdLine;
    this.directory = System.getProperty("java.io.tmpdir");
  }

  public ExternalCommand(final String [] cmdLine, final boolean merge, final String curdir) {
    this.merge = merge;
    this.cmdLine = cmdLine;
    this.directory = curdir;
  }

  /**
   * Reads a line from the process output.
   * @param r reader.
   * @return line read.
   * @throws IOException IO exception.
   * @throws InterruptedException exception.
   */
  // data read is lost when interrupted
  // returns null if EOF
  // major feature: it never blocks the current thread while reading a stream
  // On peut améliorer les perfs en gardant dans sb ce qui est lu et donc en lisant plusieurs caractères à la fois
  // et en ne retournant que jusqu'au retour chariot.
  // this private method must be called from synchronized methods
  // any thread
  private String readLine(Reader r) throws IOException, InterruptedException {
    sb.setLength(0);
    while (!Thread.currentThread().isInterrupted()) {
      if (r.ready()) {
        final int ret = r.read();
        if (ret == -1) return sb.length() != 0 ? sb.toString() : null;
        if (ret == '\n') return sb.toString();
        sb.append((char) ret);
      } else {
        try {
          process.exitValue();
          return sb.length() != 0 ? sb.toString() : null;
        } catch (final IllegalThreadStateException ex) {}
        Thread.sleep(100);
      }
    }
    log.info("readLine(): was interrupted");
    throw new InterruptedException("readLine()");
  }

  /**
   * Reads the whole output.
   * @return whole output.
   * @throws InterruptedException exception.
   */
  // return null if IOException (EOF for instance)
  // any thread
  public synchronized String runStdoutStderr() throws InterruptedException {
    String retval = null;
    try {
      fork();
      retval = readStdoutStderr();
    } catch (final IOException ex) {}
    try {
      end();
    } catch (final IOException ex) {}
    return retval;
  }

  /**
   * Reads the whole standard output.
   * @return whole standard output.
   * @throws InterruptedException interrupted.
   */
  // return null if IOException (EOF for instance)
  // any thread
  public synchronized String runStdout() throws InterruptedException {
    String retval = null;
    try {
      fork();
      retval = readStdout();
    } catch (final IOException ex) {}
    try {
      end();
    } catch (final IOException ex) {}
    return retval;
  }

  /**
   * Displays command line arguments.
   * any thread.
   * @param none.
   * @return void.
   */
  public synchronized void logArgs() {
    for (int x = 0; x < cmdLine.length; x++)
      log.debug("arg " + x + ": " + cmdLine[x]);
  }

  /**
   * Launches a process but do not wait for its completion.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>before</bold> reading the process output stream.
   */
  public synchronized void fork() throws IOException {
    final ProcessBuilder pb = new ProcessBuilder(cmdLine);
    pb.directory(new File(directory));
    pb.redirectErrorStream(merge);
    process = pb.start();

    if (process == null) throw new IOException("null process");

    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
  }

  /**
   * Merges stdout and stderr.
   * Waits for the end of the process output stream.
   * Note that this method can return before the process completion.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>while</bold> reading the process output stream.
   */
  public synchronized String readStdoutStderr() throws IOException, InterruptedException {
    StringBuffer output = new StringBuffer();
    String str;
    while ((str = readLine(reader)) != null) {
      output.append(str);
      output.append("\n");
    }

    while ((str = readLine(errReader)) != null) {
      output.append(str);
      output.append("\n");
    }

    // We can not return a StringBuffer since it could be modified by readOutput() running
    // in another thread, even if we synchronize each method of ExternalCommand.
    return output.toString();
  }

  /**
   * Reads stdout.
   * Waits for the end of the process output stream.
   * Note that this method can return before the process completion.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>while</bold> reading the process output stream.
   */
  public synchronized String readStdout() throws IOException, InterruptedException{
    StringBuffer output = new StringBuffer();
    String str;

    while ((str = readLine(reader)) != null) {
      output.append(str);
      output.append("\n");
    }

    // We can not return a StringBuffer since it could be modified by readOutput() running
    // in another thread, even if we synchronize each method of ExternalCommand.
    return output.toString();
  }

  /**
   * Reads stderr.
   * Waits for the closure of the process output stream.
   * Note that this method can return before the process completion.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>while</bold> reading the process output stream.
   */
  public synchronized String readStderr() throws IOException, InterruptedException {
    StringBuffer output = new StringBuffer();
    String str;

    while ((str = readLine(errReader)) != null) {
      output.append(str);
      output.append("\n");
    }

    // We can not return a StringBuffer since it could be modified by readOutput() running
    // in another thread, even if we synchronize each method of ExternalCommand.
    return output.toString();
  }

  /**
   * Reads one line of the stdout.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>while</bold> reading the process output stream.
   */
  public synchronized String readLineStdout() throws IOException, InterruptedException {
    return readLine(reader);
  }

  /**
   * Reads one line of the stderr.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException i/o exception <bold>while</bold> reading the process output stream.
   */
  public synchronized String readLineStderr() throws IOException, InterruptedException {
    return readLine(errReader);
  }

  /**
   * Make sure the underlying file descriptors are closed, to avoid maintening
   * unused resources in an application server JVM for instance.
   * any thread.
   * @param none.
   * @return void.
   * @throws IOException error while closing streams.
   */
  public void end() throws IOException {
    // may be closing the readers is sufficient to get these streams closed
    // we want the underlying file descriptors being close to avoid maintening
    // unused resources in an application server JVM, so we explicitely close
    // those streams.
    if (process != null) {
      kill();
      synchronized (this) {
        if (process.getInputStream() != null) process.getInputStream().close();
        if (process.getOutputStream() != null) process.getOutputStream().close();
        if (process.getErrorStream() != null) process.getErrorStream().close();
      }
    }
  }

  /**
   * Kills the process.
   * We do not reset process to null since it can be used to get exit code. 
   * any thread.
   * @param none.
   * @return void.
   */
  private void kill() {
    if (process != null) {
      boolean terminated = false;
      process.destroy();
      while (terminated == false)
        try {
          process.waitFor();
          terminated = true;
        } catch (final InterruptedException ex) {
          log.warn("Exception", ex);
        }
    }
  }
}
