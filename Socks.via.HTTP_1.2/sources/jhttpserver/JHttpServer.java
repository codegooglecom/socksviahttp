/*
This file is part of JHttpServer.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

JHttpServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JHttpServer; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        HttpServer.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Http Server

package jhttpserver;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import jhttpserver.consts.*;
import jhttpserver.util.*;
import common.log.*;

public class JHttpServer extends LogConfiguration
{
  private static final String PROPERTIES_FILE = "jhttpserver.inithttpsrv";

  private ServerSocket serverSocket;
  private String serverName;
  private int serverPort;
  private Hashtable loadedServlets;
  private ServletContextImpl servletContextImpl;
  private String serverRoot = null;
  private String[] defaultDocuments = { "index.html", "index.htm", "default.html", "default.htm" };
  private boolean gzipEnabled = false;
  private InetAddress bindAddr = null;
  private Hashtable aliases;
  private String[] servletsToPreload;


  public JHttpServer(int logLevel)
  {
    super();
    this.consoleLoggingLevel = logLevel;
  }

  public boolean init()
  {
    // Logging configuration
    fileLoggingLevel = (int)PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "jhttpserver.logfile.level");
    logFileName = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.logfile.name");

    // Init logging system
    initLog();

    // BANNER
    printlnInfo(Const.SERVER_INFO + " by " + Const.AUTHOR_NAME + " <" + Const.AUTHOR_EMAIL + ">");

    // Server configuration
    serverName = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.server.name");
    printlnInfo("serverName = " + serverName);
    serverPort = (int)PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "jhttpserver.server.port");
    printlnInfo("serverPort = " + serverPort);
    serverRoot = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.server.root");
    if (serverRoot == null) printlnInfo("I'm not serving static documents");
    else printlnInfo("serverRoot = " + serverRoot);
    String bindip = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.server.bindip");
    if ((bindip != null) && (bindip.length() > 0))
    {
      try
      {
        bindAddr = InetAddress.getByName(bindip);
        printlnInfo("Listening only on IP : " + bindip);
      }
      catch(Exception e)
      {
        printlnError("Invalid IP Address specified in properties file (IP:" + bindip + ") - Listening on all IPs");
        bindAddr = null;
      }
    }
    else
    {
      printlnInfo("Listening on all IP");
    }

    if ("true".equals(PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.server.enablegzip")))
    {
      gzipEnabled = true;
      printlnInfo("GZIP compression enabled");
    }
    else
    {
      printlnInfo("GZIP compression disabled");
    }

    loadedServlets = new Hashtable();
    aliases = new Hashtable();

    // Get the aliases
    String sAllAliases = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.aliases").trim();
    String[] sAliases = StringUtils.stringSplit(sAllAliases, ",", true);
    for (int i = 0; i < sAliases.length; i++)
    {
      String aliasName = sAliases[i];
      String realUrl = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver." + aliasName + ".realurl").trim();
      String fakeUrl = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver." + aliasName + ".fakeurl").trim();
      aliases.put(fakeUrl, realUrl);
    }

    // Get the servlets to preload
    String sAllServletsToPreload = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "jhttpserver.preload.servlets").trim();
    servletsToPreload = StringUtils.stringSplit(sAllServletsToPreload, ",", true);

    // Create the ServletContextImpl
    servletContextImpl = new ServletContextImpl(this);
    servletContextImpl.setAttribute("HTTP_SERVER", this);
    return(true);
  }

  public String getRealUrl(String url)
  {
    if (url == null) return(null);
    Object realUrl = aliases.get(url);
    return(realUrl == null ? url : (String)realUrl);
  }

  public GenericServlet getLoadedServlet(String servletName)
  {
    Object obj = loadedServlets.get(servletName);
    if (obj == null) return(null);
    return((GenericServlet)obj);
  }

  public void addLoadedServlet(String servletName, GenericServlet loadedServlet)
  {
    loadedServlets.put(servletName, loadedServlet);
  }

  private void preloadServlets()
  {
    for (int i = 0; i < servletsToPreload.length; i++)
    {
      try
      {
        loadServlet(servletsToPreload[i]);
      }
      catch(Exception e)
      {
        printlnError("Failed to preload servlet " + servletsToPreload[i] + ". Exception : " + e);
      }
    }
  }

  public void start()
  {
    try
    {
      serverSocket = new ServerSocket(serverPort, 50, bindAddr);
    }
    catch(BindException e)
    {
      printlnFatal("Port " + serverPort + " already in use - Exiting.");
      return;
    }
    catch(IOException e)
    {
      printlnFatal("Error while creating ServerSocket : " + e);
      return;
    }

    while(true)
    {
      Socket socket;
      try
      {
        socket = serverSocket.accept();
        JHttpServerThread thread = new JHttpServerThread(this, socket);
        thread.start();
      }
      catch (IOException e)
      {
        //System.out.println("Error while accepting connection : " + e);
      }
    }
  }

  public static void main(String[] args)
  {
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
    JHttpServer server = new JHttpServer(logLevel);
    if (server.init())
    {
      // Preload the servlets
      server.preloadServlets();

      // Let's start
      server.start();
    }
  }

  public void setServerName(String serverName)
  {
    this.serverName = serverName;
  }
  public String getServerName()
  {
    return serverName;
  }
  public void setServerPort(int serverPort)
  {
    this.serverPort = serverPort;
  }
  public int getServerPort()
  {
    return serverPort;
  }
  public ServletContext getServletContext()
  {
    return servletContextImpl;
  }
  public String getServerRoot()
  {
    return serverRoot;
  }
  void setServerRoot(String serverRoot)
  {
    this.serverRoot = serverRoot;
  }

  public static String getServerInfo()
  {
    return(Const.SERVER_INFO);
  }

  public String[] getDefaultDocuments()
  {
    return defaultDocuments;
  }
  void setDefaultDocuments(String[] defaultDocuments)
  {
    this.defaultDocuments = defaultDocuments;
  }
  void setGzipEnabled(boolean gzipEnabled)
  {
    this.gzipEnabled = gzipEnabled;
  }
  public boolean isGzipEnabled()
  {
    return gzipEnabled;
  }

  public GenericServlet loadServlet(String servletName) throws Exception
  {
    GenericServlet servlet = (HttpServlet)Class.forName(servletName).newInstance();
    ServletConfigImpl servletConfigImpl = new ServletConfigImpl();
    servletConfigImpl.setServletContext(getServletContext());
    servletConfigImpl.setServletName(servletName);
    try
    {
      servlet.init(servletConfigImpl);
    }
    catch (ServletException se)
    {
      printlnError("ServletException while init : " + se);
    }
    addLoadedServlet(servletName, servlet);
    return(servlet);
  }
}
