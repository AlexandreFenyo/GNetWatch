
GNetWatch
Copyright 2006, 2007, 2008, 2016 Alexandre Fenyo
gnetwatch@fenyo.net - http://fenyo.net

GNU PUBLIC LICENSE

Read the file "COPYING" for informations about the GPL distribution license.

----------------------------------------------------------------------

Latest documentation, sources & binaries available on:

  http://gnetwatch.sourceforge.net

----------------------------------------------------------------------

This file is only a subset of the documentation.
Please open the full documentation file named gnetwatch.pdf
in this archive or get the latest documentation files
from the GNetWatch official web server:

  - HTML format:
    http://gnetwatch.sourceforge.net/gnetwatch-html.htm

  - PDF format:
    http://gnetwatch.sourceforge.net/gnetwatch.pdf

----------------------------------------------------------------------

Usage:

  GNetWatch is a free open source Java application that offers
  real-time graphical monitoring and analysis of network performances
  through SNMP, ICMP and traffic generation modules.

Compliance and support for network protocols:

  - GNetWatch can generate packets marked with any DiffServ codepoint,
    especially those defined in:
    - RFC-2597 Assured Forwarding Per Hop Behaviour Group,
    - RFC-2598 Expedite Forwarding Per Hop Behaviour Group.

  - GNetWatch can browse MIBs using any of the following SNMP dialects:
    - SNMPv1,
    - SNMPv2c,
    - SNMPv3 (MD5 + DES).

  - GNetWatch can be used on IPv4 and IPv6 networks.

  - GNetWatch can generate Ethernet jumbo frames.

  - GNetWatch can make use of external programs:
    - Ethereal/Wireshark to track new hosts
    - NMap to display informations about remote hosts
    - external probes shipped with gnetwatch and probes
      that you can write yourself

Graphic performances:

  GNetWatch is using both SWT and AWT simultaneously:
  the main GUI is drawn with SWT in order to get direct access to the
  underlying windowing system and animated graphs are generated
  using Java2D over AWT.

Database management:

  Since version 3.0, GNetWatch holds its configuration
  and the collected data in a JDBC compliant database.
  It is shipped with an embedded database: HSQLDB, but
  you can configure an external database.

----------------------------------------------------------------------
--                          RUNNING GNETWATCH                       --
----------------------------------------------------------------------

Several ways to run GNetWatch

A- download a bundle (installation already packaged) for Linux or MS-Windows

B- download the GNetWatch JAR file and public domain dependant packages

C- download the GNetWatch java sources archive and compile it

----------------------------------------------------------------------

Using a bundle to run GNetWatch under MS-WINDOWS

Just follow these steps:

1- download and install a Java SE Runtime Environment (JRE)
   compliant with JRE 5 specifications at least
   (available for instance from http://java.sun.com)

2- download and extract the GNetWatch MS-Windows bundle

3- double-click on gnetwatch.bat
   Until GNetWatch 2.2, you could run the program by
   double-clicking on the GNetWatchBundle.jar file.
   Starting with GNetWatch 3.0, AVOID double-clicking
   on GNetWatchBundle.jar because it will launch GNetWatch
   without correctly setting the heap size, so the program
   may crash later if using an internal database.
   On the contrary, gnetwatch.bat correctly sets the
   appropriate memory options.

----------------------------------------------------------------------

Using a bundle to run GNetWatch under LINUX

Just follow these steps:

1- download and install a Java SE Runtime Environment (JRE)
   compliant with JRE 5 specifications at least
   (available for instance from http://java.sun.com)

2- download and extract the GNetWatch LINUX bundle

3- set and export the MOZILLA_FIVE_HOME environment variable
   (see your Mozilla or Firefox documentation)

4- include the GNetWatch installation directory and the MOZILLA_FIVE_HOME
   in the LD_LIBRARY_PATH environment variable

Example:
user@host% tar zxf GNetWatch-LinuxBundle-version.tar.gz
user@host% cd GNetWatch-LinuxBundle-version
user@host% MOZILLA_FIVE_HOME=/usr/lib/mozilla-1.7.12
user@host% export MOZILLA_FIVE_HOME
user@host% LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$MOZILLA_FIVE_HOME:."
user@host% export LD_LIBRARY_PATH
user@host% java -Xmx1G -jar GNetWatchBundle.jar

If you get the following error message "org.eclipse.swt.SWTError XPCOM error -2147467262",
try removing any xulrunner version higher than the latest 1.8.x.

----------------------------------------------------------------------
--                        CONFIGURING GNETWATCH                     --
----------------------------------------------------------------------

The main configuration file is named config.xml and is located
in the GNetWatch installation base directory.

This file contains configuration entries following this format:
  <entry key="CONFIGURATION_ENTRY">ENTRY_VALUE</entry>

Here are the available configuration entries:

- net.fenyo.log4j:
  This entry defines the name of the logging engine configuration file.

- net.fenyo.initialobjects:
  This entry defines the name of a file that contains definitions
  of user-defined GNetWatch objects that will be built
  just after GnetWatch start-up.

- net.fenyo.ipaddresseditor.insertonkeypressed:
  This boolean entry defines the type of Key events that are used
  by the IPv4 address editor of GNetWatch.
  When running GNetWatch under SWT+GTK+X11, set it to false.
  Otherwise, set it to true.

- net.fenyo.ping.countparameter:
  Since JRE is not efficient with raw sockets, GNetWatch starts external
  PING processes. This entry defines the option used by GNetWatch
  To make PING stop after having sent a specific number of ICMP packets.

- net.fenyo.ping.regex:
  This entry defines a regular expression used by GNetWatch to parse
  the output of PING processes.

- net.fenyo.language and net.fenyo.country:
  these entries are used to set the desired LOCALE.

Here is a configuration file example for use with an english MS-Windows system:

    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
    <properties>
      <comment>General configuration properties for MS-Windows</comment>
      <entry key="net.fenyo.log4j">log4j.xml</entry>
      <entry key="net.fenyo.initialobjects">initial-objects.xml</entry>
      <entry key="net.fenyo.ipaddresseditor.insertonkeypressed">true</entry>
      <entry key="net.fenyo.ping.countparameter">-n</entry>
      <entry key="net.fenyo.ping.regex">(.|\r|\n)*:.*?([0-9]+)[^0-9]*ms(.|\r|\n)*</entry>
      <entry key="net.fenyo.language"></entry>
      <entry key="net.fenyo.country"></entry>
    </properties>

Here is a configuration file example for use with a french Linux system:
    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
    <properties>
      <comment>General configuration properties for Linux</comment>
      <entry key="net.fenyo.log4j">log4j.xml</entry>
      <entry key="net.fenyo.initialobjects">initial-objects.xml</entry>
      <entry key="net.fenyo.ipaddresseditor.insertonkeypressed">false</entry>
      <entry key="net.fenyo.ping.countparameter">-c</entry>
      <entry key="net.fenyo.ping.regex">(.|\r|\n)*:.*?([0-9]+)\.[0-9]*[^0-9]*ms(.|\r|\n)*</entry>
      <entry key="net.fenyo.language">fr</entry>
      <entry key="net.fenyo.country">FR</entry>
    </properties>

----------------------------------------------------------------------
--                     CREATING OBJECTS AT START-UP                 --
----------------------------------------------------------------------

The file defined in the "net.fenyo.initialobjects" configuration entry
may be used to make GNetWatch automatically build objects at start-up.

Among the many objects GNetWatch lets you build through the GUI,
only a few target types with limited features can be built through
this file :

- groups:
  trees of groups can be defined, meaning parents of a group are groups.

- IPv4 targets:
  parents of IPv4 targets must be groups.
  IPv4 targets can define specific SNMP properties.

- IPv6 targets:
  parents of IPv6 targets must be groups.
  IPv6 targets can NOT define any specific SNMP properties
  (you need to use the GUI to define those properties).

- IPv4 subnets:
  parents of IPv4 subnets must be groups.

- IPv4 ranges:
  parents of IPv4 ranges must be groups.

Here is an example of such a configuration file:

    <?xml version="1.0" encoding="ISO-8859-1" ?>
    <gnetwatch>
      <objects>
        <target targetType="group">
          <name>fenyo.net</name>
        </target>
        <target targetType="group">
          <name>sourceforge.net</name>
        </target>
        <target targetType="group">
          <name>gnetwatch.sourceforge.net</name>
          <parent parentType="group">sourceforge.net</parent>
        </target>
        <target targetType="ipv4">
          <address>66.35.250.209</address>
          <parent parentType="group">gnetwatch.sourceforge.net</parent>
        </target>
        <target targetType="group">
          <name>gw.fenyo.net</name>
          <parent parentType="group">fenyo.net</parent>
        </target>
        <target targetType="ipv4">
          <address>192.168.0.5</address>
          <parent parentType="group">gw.fenyo.net</parent>
        </target>
        <target targetType="ipv4">
          <address>88.170.235.198</address>
          <parent parentType="group">gw.fenyo.net</parent>
          <snmp><version>v2c</version><community>private</community></snmp>
        </target>
        <target targetType="group">
          <name>sandbox</name>
          <parent parentType="group">gw.fenyo.net</parent>
          <parent parentType="group">www.sourceforge.net</parent>
        <target targetType="ipv4">
          <address>127.0.0.1</address>
          <parent parentType="group">sandbox</parent>
        </target>
        </target>
      </objects>
    </gnetwatch>

The tree structure corresponding to this file looks like:

    - fenyo.net
      - gw.fenyo.net
        - 192.168.0.5
        - 213.41.133.35
        - sandbox
          - 127.0.0.1
    - sourceforge.net
      - gnetwatch.sourceforge.net
        - 66.35.250.209
        - sandbox (same instance as the previous one)
          - 127.0.0.1

Here is an example of definition of the SNMPv1 properties:

  <snmp>
    <version>v1</version>
    <community>public</community>
    <pdu-max-size>1400</pdu-max-size>
    <port>161</port>
    <retries>3</retries>
  </snmp>

Here is an example of definition of the SNMPv2c properties:

  <snmp>
    <version>v2c</version>
    <community>public</community>
    <pdu-max-size>1400</pdu-max-size>
    <port>161</port>
    <retries>3</retries>
  </snmp>

Here is an example of definition of the SNMPv3 properties:

  <snmp>
    <version>v3</version>
    <security>AUTH_PRIV</security>
    <!-- use NOAUTH_NOPRIV to get no authentication nor privacy,
             AUTH_NOPRIV to get authentication but no privacy
             and AUTH_PRIV to get both authentication and privacy -->
    <password-auth>my_secret_for_authentication</password-auth>
    <password-priv>my_secret_for_privacy</password-priv>
    <pdu-max-size>1400</pdu-max-size>
    <port>161</port>
    <retries>3</retries>
  </snmp>

----------------------------------------------------------------------
--                     RUNNING EXTERNAL PROGRAMS                    --
----------------------------------------------------------------------

GNetWatch can call NMap and Ethereal/Wireshark:
  - Ethereal/Wireshark to track new hosts
  - NMap to display informations about remote hosts
Before running GNetWatch, you must include nmap (or named.exe on
MS-Windows) and tethereal (or tethereal.exe on MS-Windows)
in the PATH.
If you installed Wireshark instead of Ethereal, you must
rename tshark (or tshark.exe on MS-Windows) to tethereal
(or to tethereal.exe on MS-Windows).

----------------------------------------------------------------------
--                     USING AN EXTERNAL DATABASE                   --
----------------------------------------------------------------------

If you plan to collect a big amount of data or to track a lot of targets,
you may encounter memory and performance limitations using the internal
HSQLB database. In that case, migrate to an external database.
You can choose any type of JDBC database supported by Hibernate.
The section "Database connection settings" in the file hibernate.cfg.xml
defines the JDBC driver and connection path to the database.

For instance, to use an external HSQLDB process, apply the following steps:

  - run an external HSQLDB database process with the following command:

    java -XX:+AggressiveHeap -cp GNetWatchBundle.jar org.hsqldb.Server

  - change the Database connection settings in the file hibernate.cfg.xml
    like this:

    <!-- Database connection settings -->
    <property name="connection.driver_class">org.hsqldb.jdbcDriver</property>
    <property name="connection.url">jdbc:hsqldb:hsql://127.0.0.1</property>

  - Finally, run GNetWatch.


----------------------------------------------------------------------
--                     BUILD FROM SOURCES                           --
----------------------------------------------------------------------

mvn install assembly:assembly



