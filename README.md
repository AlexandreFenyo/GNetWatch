# GNetWatch

real-time graphical monitoring and analysis of network performance through SNMP and ICMP

Full documentation and binary distributions for Windows and Linux here: http://gnetwatch.sourceforge.net/

## Description

GNetWatch is a free open source Java application that offers real-time graphical monitoring and analysis of network performance through SNMP and ICMP. To get an instant view of the network state, data are collected, stored and displayed every few seconds. Two traffic generation modules are available. The former can flood UDP packets of any size (jumbo frames for instance) and tagged with any DiffServ/ToS flag for QoS and class of services testing. The latter can generate a huge quantity of parallel requests to any HTTP(s) server, for web applications load testing. To automatically discover new hosts, GNetWatch can make use of Ethereal/WireShark and later invoke NMap to get informations about the remote systems. Note that IPv6 and SNMPv3 are fully supported by GNetWatch. 

## Main features

GNetWatch can flood packets with any DiffServ codepoint, especially those defined in:
- RFC-2597 Assured Forwarding Per Hop Behaviour Group,
- RFC-2598 Expedite Forwarding Per Hop Behaviour Group.

It can also talk using any of the following SNMP dialects: SNMPv1, SNMPv2c, SNMPv3 (MD5 + DES).

It can be used on IPv4 and IPv6 networks and can generate Ethernet jumbo frames.

Graphic performances:

> GNetWatch is using both SWT and AWT simultaneously: the main GUI is drawn with SWT in order to get direct access to the
> underlying windowing system and animated graphs are generated using Java2D over AWT. 
