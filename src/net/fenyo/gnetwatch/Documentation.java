
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
import javax.net.ssl.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.*;
import java.security.cert.*;

import net.fenyo.gnetwatch.GUI.*;
import net.fenyo.gnetwatch.actions.ExternalCommand;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.data.EventReachable;

import org.dom4j.*;
import org.dom4j.io.*;

import org.apache.commons.logging.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.apache.fop.apps.*;

/**
 * Generates documentation.
 * Tested successfully with XALAN-J 2.7.0
 * @author Alexandre Fenyo
 * @version $Id: Documentation.java,v 1.2 2008/04/15 23:58:18 fenyo Exp $
 */

public class Documentation {
  private static Log log = LogFactory.getLog(Documentation.class);

  /**
   * General entry point.
   * @param args command line arguments.
   * @return void.
   * @throws IOException io exception.
   */
  public static void main(String[] args) throws IOException, TransformerConfigurationException, TransformerException, FileNotFoundException, FOPException {
    final String docbook_stylesheets_path = args[0];

    // Get configuration properties.
    final Config config = new Config();

    // Read general logging rules.
    GenericTools.initLogEngine(config);
    log.info(config.getString("log_engine_initialized"));
    log.info(config.getString("begin"));

    Transformer docbookTransformerHTML = TransformerFactory.newInstance().
      newTransformer(new StreamSource(new File(docbook_stylesheets_path + "/html/docbook.xsl")));
//    docbookTransformerHTML.setParameter("draft.mode", "no");

    docbookTransformerHTML.transform(new StreamSource(new FileReader(new File("gnetwatch-documentation.xml"))),
        new StreamResult(new FileWriter(new File("c:/temp/gnetwatch-documentation.html"))));

    Transformer docbookTransformerFO = TransformerFactory.newInstance().
    newTransformer(new StreamSource(new File(docbook_stylesheets_path + "/fo/docbook.xsl")));
//    docbookTransformerFO.setParameter("draft.mode", "no");

    docbookTransformerFO.transform(new StreamSource(new FileReader(new File("gnetwatch-documentation.xml"))),
        new StreamResult(new FileWriter(new File("c:/temp/gnetwatch-documentation.fo"))));

    // for very old FOP version (0.20):
//    Driver driver = new Driver();
//    driver.setRenderer(Driver.RENDER_PDF);
//    driver.setInputSource(new InputSource(new FileReader(new File("c:/temp/gnetwatch-documentation.fo"))));
//    driver.setOutputStream(new FileOutputStream(new File("c:/temp/gnetwatch-documentation.pdf")));
//    driver.run();
    // with new FOP version:
    OutputStream outStream = new BufferedOutputStream(new FileOutputStream("c:/temp/gnetwatch-documentation.pdf"));
    final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Source source = new StreamSource(new FileReader(new File("c:/temp/gnetwatch-documentation.fo")));
    Result result = new SAXResult(fop.getDefaultHandler());
    transformer.transform(source, result);
    outStream.close();
  }
}
