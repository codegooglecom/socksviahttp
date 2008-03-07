/*
This file is part of Socks via HTTP.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

Socks via HTTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Socks via HTTP; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        Client.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Main class (client part)

package socksviahttp.client;

import java.net.MalformedURLException;

import socksviahttp.core.consts.*;
import socksviahttp.core.util.*;
import common.log.*;
import socksviahttp.client.engine.*;

public class Client
{
  private static final String PROPERTIES_FILE = "socksviahttp.client.init";

  private int logLevel = Log.LOG_WARN;
  private SocksConnectionServer socksServer = null;
  private GenericConnectionServer[] tunnelServers = null;
  private Configuration configuration = null;

  public Client(int logLevel)
  {
    super();
    this.logLevel = logLevel;
  }

  public boolean init()
  {
    configuration = new Configuration();
    configuration.setConsoleLoggingLevel(logLevel);
    try
    {
      configuration.load(PROPERTIES_FILE);
    }
    catch(MalformedURLException e)
    {
      configuration.printlnFatal("<CLIENT> SERVER url is incorrect : MalformedURLException : " + e);
      return(false);
    }

    // Start logging system
    configuration.initLog();

    // BANNER
    configuration.printlnInfo(Const.APPLICATION_NAME + " v" + Const.APPLICATION_VERSION + " by " + Const.AUTHOR_NAME + " <" + Const.AUTHOR_EMAIL + ">");

    // Proxy on
    if (configuration.isUseProxy())
    {
      // Logging
      configuration.printlnInfo("Using proxy " + configuration.getProxyHost() + ":" + configuration.getProxyPort());

      // JDK 1.3 and below
      System.getProperties().put("proxySet", "true");
      System.getProperties().put("proxyHost", configuration.getProxyHost());
      System.getProperties().put("proxyPort", configuration.getProxyPort());

      // JDK 1.4
      System.getProperties().put("http.proxyHost", configuration.getProxyHost());
      System.getProperties().put("http.proxyPort", configuration.getProxyPort());
    }
    else
    {
      // JDK 1.3 and below
      System.getProperties().remove("proxySet");
      System.getProperties().remove("proxyHost");
      System.getProperties().remove("proxyPort");

      // JDK 1.4
      System.getProperties().remove("http.proxyHost");
      System.getProperties().remove("http.proxyPort");
    }
    return(true);
  }

  public void start()
  {
    socksServer = new SocksConnectionServer(configuration);
    /*if (!socksServer.checkServerVersion())
    {
      // Please upgrade
      return;
    }*/

    // Start the socks server
    socksServer.start();

    // Start the generic servers (direct tunneling)
    Tunnel[] tunnels = configuration.getTunnels();
    tunnelServers = new GenericConnectionServer[tunnels.length];
    for (int i = 0; i < tunnels.length; i++)
    {
      tunnelServers[i] = new GenericConnectionServer(configuration, tunnels[i]);
      if (tunnels[i].getDestinationUri().length() > 0) tunnelServers[i].start();
    }
  }

  /*public void stop()
  {
    if (socksServer == null) return;

    // Stop the server
    socksServer.listening = false;
    try
    {
      socksServer.join(60000);
    }
    catch(Exception ex){}
    socksServer = null;
  }*/

  // Main method
  public static void main(String[] args)
  {
    //System.out.println(Const.APPLICATION_NAME + " v" + Const.APPLICATION_VERSION + " by " + Const.AUTHOR_NAME + " <" + Const.AUTHOR_EMAIL + ">");
    int logLevel = Log.LOG_INFO;
    if (args.length == 1)
    {
      if (args[0].equals("-log0")) logLevel = Log.LOG_ALL;
      if (args[0].equals("-log1")) logLevel = Log.LOG_DEBUG;
      if (args[0].equals("-log2")) logLevel = Log.LOG_INFO;
      if (args[0].equals("-log3")) logLevel = Log.LOG_WARN;
      if (args[0].equals("-log4")) logLevel = Log.LOG_ERROR;
      if (args[0].equals("-log5")) logLevel = Log.LOG_FATAL;
      if (args[0].equals("-log6")) logLevel = Log.LOG_NONE;
    }
    Client client = new Client(logLevel);
    if (client.init())
    {
      // Let's start
      client.start();
    }
  }
  public Configuration getConfiguration()
  {
    return configuration;
  }
}
