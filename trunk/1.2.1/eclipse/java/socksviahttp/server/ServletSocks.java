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

// Title :        ServletSocks.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Main Servlet (Server part of Socks via HTTP)

package socksviahttp.server;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import socksviahttp.core.util.*;
import common.log.*;
import socksviahttp.core.net.*;
import socksviahttp.core.consts.*;
import socksviahttp.server.acl.*;
import socksviahttp.server.engine.*;

public class ServletSocks extends HttpServlet
{
  private static final String PROPERTIES_FILE = "socksviahttp.server.initsrv";
  private static final String[] SUPPORTED_CLIENT_VERSIONS = { "1.2" };

  public static boolean logInitDone = false;
  public static LogConfiguration logConfiguration = null;
  public static ConnectionTable table = null;
  public static UserList userlist = null;
  public static long serverTimeout = 0; // Infinite timeout
  public static String serverEncryptionKey = null;

  // init method
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);

    // Logging configuration
    logInit();

    // Get the timeout
    serverTimeout = PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "socks.timeout");
    if (serverTimeout < 0) serverTimeout = 0;

    // Get the encryption key
    serverEncryptionKey = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "socks.encryptionkey").trim();

    // Create the UserList
    logConfiguration.printlnInfo("Loading UserList");
    userlist = new UserList();

    // Fill it
    String sUsers = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "socks.server.users").trim();
    String[] users = StringUtils.stringSplit(sUsers, ",", true);
    for (int i = 0; i < users.length; i++)
    {
      String userLogin = users[i];
      String userPassword = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "users.password." + userLogin).trim();
      int spyMode = (int)PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "users.spymode." + userLogin);
      UserInfo userInfo = new UserInfo(userLogin, userPassword, spyMode);
      userInfo.authorizedTime = PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "users.authorizedtime." + userLogin);
      String sIp = PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "users.ip." + userLogin).trim();
      String[] ips = StringUtils.stringSplit(sIp, ",", true);
      for (int j = 0; j < ips.length; j++)
      {
        userInfo.addIp(ips[j]);
      }

      // Add the UserInfo to the list
      logConfiguration.printlnDebug("Adding user " + userLogin);
      userlist.addUser(userInfo);
    }

    // Create the ConnectionTable
    logConfiguration.printlnInfo("Creating ConnectionTable");
    table = new ConnectionTable();

    // Start the ThreadPing
    logConfiguration.printlnInfo("Creating the timeout checking thread");
    new ThreadPing().start();
  }

  public static void logInit()
  {
    if (logInitDone) return;

    // Init the logging
    logConfiguration = new LogConfiguration();
    logConfiguration.setConsoleLoggingLevel((int)PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "server.logconsole.level"));
    logConfiguration.setFileLoggingLevel((int)PropertiesFileReader.getPropertyLongValue(PROPERTIES_FILE, "server.logfile.level"));
    logConfiguration.setLogFileName(PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "server.logfile.name"));
    logConfiguration.initLog();
    logInitDone = true;

    // BANNER
    logConfiguration.printlnInfo(Const.APPLICATION_NAME + " v" + Const.APPLICATION_VERSION + " by " + Const.AUTHOR_NAME + " <" + Const.AUTHOR_EMAIL + ">");
  }

  // get method
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    // Get the remote IP
    String ip = request.getRemoteAddr();
    logConfiguration.printlnWarn("HTTP GET connection from " + ip);

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<body>");
    out.println("<p>This url cannot be invoked directly.</p>");
    out.println("</body>");
    out.println("</html>");
  }

  // post method
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    // Get the remote IP
    String ip = request.getRemoteAddr();
    //logConfiguration.printlnDebug("HTTP POST connection from " + ip);

    // Set the headers
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);

    response.setContentType("application/octet-stream");

    /*UserInfo userInfo = null;
    String auth = request.getHeader("Authorization");
    if ((auth != null) && (auth.startsWith("Basic ")))
    {
      // Check authorization
      String decodedAuth = Base64Decoder.decode(auth.substring(6));
      String[] userpass = StringUtils.stringSplit(decodedAuth, ":", false);
      String login = userpass[0];
      String pass = userpass[1];
      String err = null;

      // Check the user
      userInfo = userlist.getUser(login);
      if (userInfo == null)
      {
        // Unknown user
        logConfiguration.printlnWarn("Refused request to unknown user : " + login + "...");
        err = "Refused request to unknown user : " + login + "...";
      }

      // Check the password
      else if (!userInfo.password.equals(pass))
      {
        // Wrong password
        logConfiguration.printlnWarn("Refused request to user : " + login + " (bad password)...");
        err = "Refused request to user : " + login + " (bad password)...";
      }

      // Check the IP
      else if (!userInfo.isAuthorized(ip))
      {
        // Bad IP
        logConfiguration.printlnWarn("Refused request to user : " + login + " (unauthorized ip : " + ip + ")...");
        err = "Refused request to user : " + login + " (unauthorized ip : " + ip + ")...";
      }
    }
    else
    {
      // Not authorized
      // TO DO
    }*/


    try
    {
      // Read the request
      /*GZIPInputStream zis = new GZIPInputStream(request.getInputStream());
      ObjectInputStream ois = new ObjectInputStream(zis);
      DataPacket input = (DataPacket)ois.readObject();
      ois.close();*/

      //int totalLen = request.getIntHeader("Content-Length");
      int totalLen = request.getContentLength();
      InputStream is = request.getInputStream();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] tmpBuffer = new byte[65536];
      int n;
      int totalRead = 0;
      while ((totalRead < totalLen) && ((n = is.read(tmpBuffer)) >= 0))
      {
        baos.write(tmpBuffer, 0, n);
        totalRead += n;
      }
      is.close();
      DataPacket input = new DataPacket();
      input.encryptionKey = serverEncryptionKey.getBytes();
      input.loadFromByteArray(baos.toByteArray());

      ///////

      int type = input.type;
      String id_conn = input.id;

      if (input.errorCode != 0)
      {
        // TO DO
        // Warn the client that the encryption key may be wrong
        logConfiguration.printlnWarn("Connection " + id_conn + " uses a wrong encryption key.");
        type = Const.CONNECTION_WRONG_ENCRYPTION_KEY;
      }

      ExtendedConnection extConn = null;
      Connection conn = null;

      // Build the response
      DataPacket output = new DataPacket();
      output.zipData = input.zipData;
      output.encryptData = input.encryptData;
      output.encryptionKey = serverEncryptionKey.getBytes();
      output.id = id_conn;

      switch(type)
      {
        case Const.CONNECTION_CREATE: // Create a connection

          String iprev = nsLookup(ip);

          String err = null;
          String[] userpass = StringUtils.stringSplit(input.id, ":", false);
          String login = userpass[0];
          String pass = userpass[1];
          String sUserTimeout = userpass[2];
          long userTimeout = Integer.parseInt(sUserTimeout);
          if (userTimeout < 0) userTimeout = 0;

          // Check the user
          UserInfo userInfo = userlist.getUser(login);
          if (userInfo == null)
          {
            // Unknown user
            logConfiguration.printlnWarn("Refused connection to unknown user : " + login);
            err = "Refused connection to unknown user : " + login;
          }

          // Check the password
          else if (!userInfo.password.equals(pass))
          {
            // Wrong password
            logConfiguration.printlnWarn("Refused connection to user : " + login + " (wrong password)");
            err = "Refused connection to user : " + login + " (wrong password)";
          }

          // Check the IP
          else if (!userInfo.isAuthorized(ip))
          {
            // Bad IP
            logConfiguration.printlnWarn("Refused connection to user : " + login + " (unauthorized ip : " + ip + ")");
            err = "Refused connection to user : " + login + " (unauthorized ip : " + ip + ")";
          }

          if (err != null)
          {
            output.type = Const.CONNECTION_CREATE_KO;
            output.tab = err.getBytes();
          }
          else
          {
            // Create a connection ID
            id_conn = IdGenerator.generateId(login);

            // Log
            logConfiguration.printlnInfo("Connection create : " + id_conn);

            // Get the host and the port we have to connect to
            String url = new String(input.tab);
            String host = url.substring(0, url.indexOf(':'));
            int port = Integer.parseInt(url.substring(1 + url.indexOf(':'), url.length()));

            // Create the connection
            conn = new Connection(Connection.CONNECTION_CLIENT_TYPE);

            // Connect
            if (conn.connect(host, port) != 0)
            {
              // Log
              logConfiguration.printlnWarn("Connection " + id_conn + " failed from " + iprev + "(" + ip + ") to " + host + ":" + port);

              output.type = Const.CONNECTION_CREATE_KO;
              err = "Unable to connect to " + host + ":" + port;
              output.tab = err.getBytes();
            }
            else
            {
              // Log
              logConfiguration.printlnInfo("Connection " + id_conn + " created from " + iprev + "(" + ip + ") to " + host + ":" + port);

              // Create the ExtendedConnection
              extConn = new ExtendedConnection();
              extConn.conn = conn;
              extConn.ip = ip;
              extConn.iprev = iprev;
              extConn.destIP = resolveName(host);
              extConn.destIPrev = nsLookup(host);
              extConn.destPort = port;
              extConn.user = userInfo;
              if (serverTimeout == 0) extConn.timeout = userTimeout;
              else
              {
                if (userTimeout == 0) extConn.timeout = serverTimeout;
                else extConn.timeout = Math.min(userTimeout, serverTimeout);
              }
              extConn.authorizedTime = userInfo.authorizedTime;

              // Init Spy mode logfiles
              if ((extConn.user.spyMode == UserInfo.SPY_MODE_CLIENT) || (extConn.user.spyMode == UserInfo.SPY_MODE_BOTH))
              {
                extConn.tfdClient = new ThreadFileWriter(id_conn + "_svhs_fromclient.log");
                if (extConn.tfdClient.init()) extConn.tfdClient.start();
                else extConn.tfdClient = null;
              }
              if ((extConn.user.spyMode == UserInfo.SPY_MODE_SERVER) || (extConn.user.spyMode == UserInfo.SPY_MODE_BOTH))
              {
                extConn.tfdServer = new ThreadFileWriter(id_conn + "_svhs_toclient.log");
                if (extConn.tfdServer.init()) extConn.tfdServer.start();
                else extConn.tfdServer = null;
              }

              // Add this to the ConnectionTable
              table.put(id_conn, extConn);

              // Build the response
              output.type = Const.CONNECTION_CREATE_OK;
              output.id = id_conn;
              //output.tab = conn.read();
              //output.tab = Const.TAB_EMPTY;
              String resp = "" + conn.getSocket().getInetAddress().getHostAddress() + ":" + conn.getSocket().getPort();
              output.tab = resp.getBytes();
            }
          }
          break;

        case Const.CONNECTION_VERSION_REQUEST:
          // Test the version
          if (isCompatible(input.id))
          {
            logConfiguration.printlnInfo("Version check - Version supported : " + input.id);
            output.type = Const.CONNECTION_VERSION_RESPONSE_OK;
            output.id = Const.APPLICATION_VERSION;
          }
          else
          {
            logConfiguration.printlnInfo("Version check - Version not supported : " + input.id);
            output.type = Const.CONNECTION_VERSION_RESPONSE_KO;
            output.id = Const.APPLICATION_VERSION;
          }
          output.tab = Const.TAB_EMPTY;
          break;

        case Const.CONNECTION_PING:
          // Send a PONG
          output.type = Const.CONNECTION_PONG;
          output.tab = input.tab;
          break;

        case Const.CONNECTION_PONG:
          // Reply to PONG
          // TO DO

          // Send a pong_received
          output.type = Const.CONNECTION_PONG_RECEIVED;
          output.tab = input.tab;
          break;

        case Const.CONNECTION_REQUEST:  // Request

          // Get the connection
          extConn = table.get(id_conn);

          if (extConn == null)
          {
            logConfiguration.printlnWarn("Connection not found : " + id_conn);

            // Connection not found
            output.type = Const.CONNECTION_NOT_FOUND;
            output.tab = Const.TAB_EMPTY;
          }
          else
          {
            long lastAccessDate = extConn.lastAccessDate;
            extConn.lastAccessDate = new java.util.Date().getTime();
            conn = extConn.conn;

            /*if ((extConn.user.spyMode == UserInfo.SPY_MODE_CLIENT) || (extConn.user.spyMode == UserInfo.SPY_MODE_BOTH))
            {
              // Log client data to file
              new DiskAccess(id_conn + "_client.log", input.tab).start();
            }*/
            if (extConn.tfdClient != null)
            {
              if (input.tab.length > 0) extConn.tfdClient.addLogMessage(new LogMessage(input.tab));
            }

            // Add the uploaded bytes
            extConn.uploadedBytes += input.tab.length;

            // write the bytes
            conn.write(input.tab);

            // Update the upload speed
            long div = 1 + extConn.lastAccessDate - lastAccessDate;
            extConn.currentUploadSpeed = (double)input.tab.length / div;

            // Build the response
            output.type = Const.CONNECTION_RESPONSE;
            byte[] buf = conn.read();
            if (buf == null)
            {
              output.tab = Const.TAB_EMPTY;
              output.isConnClosed = true;

              // Shutdown extended connection
              extConn.shutdown();

              // Remove the connection from the ConnectionTable
              table.remove(id_conn);
            }
            else
            {
              /*if ((extConn.user.spyMode == UserInfo.SPY_MODE_SERVER) || (extConn.user.spyMode == UserInfo.SPY_MODE_BOTH))
              {
                // Log server data to file
                new DiskAccess(id_conn + "_server.log", buf).start();
              }*/
              if (extConn.tfdServer != null)
              {
                if (buf.length > 0) extConn.tfdServer.addLogMessage(new LogMessage(buf));
              }

              // Add the received bytes
              extConn.downloadedBytes += buf.length;

              // Update the download speed
              div = 1 + extConn.lastAccessDate - lastAccessDate;
              extConn.currentDownloadSpeed = (double)buf.length / div;

              // Prepare the output
              output.tab = buf;
            }
          }
          break;

        case Const.CONNECTION_WRONG_ENCRYPTION_KEY:
          output.type = Const.CONNECTION_WRONG_ENCRYPTION_KEY;
          output.encryptData = false;
          err = "CRC Error. Check your secret encryption key";
          output.tab = err.getBytes();
          break;

        case Const.CONNECTION_DESTROY:  // Close the connection
          // Log
          logConfiguration.printlnInfo("Connection destroy : " + id_conn);

          // Get the connection
          extConn = table.get(id_conn);

          if (extConn == null)
          {
            logConfiguration.printlnInfo("Connection already destroyed by timeout : " + id_conn);

            // Connection not found
            output.type = Const.CONNECTION_DESTROY_OK;
          }
          else
          {
            extConn.lastAccessDate = new java.util.Date().getTime();
            conn = extConn.conn;

            // Close it
            conn.disconnect();

            // Shutdown extended connection
            extConn.shutdown();

            // Remove it from the ConnectionTable
            table.remove(id_conn);

            // Build the response
            output.type = Const.CONNECTION_DESTROY_OK;
          }
          break;
      }

      // Send the response
      /*GZIPOutputStream zos = new GZIPOutputStream(response.getOutputStream());
      ObjectOutputStream oos = new ObjectOutputStream(zos);
      oos.writeObject(output);
      oos.close();*/
      byte[] bArray = output.saveToByteArray();
      response.setContentLength(bArray.length);
      OutputStream os = response.getOutputStream();
      os.write(bArray);
      os.close();
    }
    catch(Throwable e)
    {
      logConfiguration.printlnError("Exception : " + e);
    }
  }

  // destroy method
  public void destroy()
  {
  }

  // nsLookup (IP -> DNS Name)
  public String nsLookup(String ip)
  {
    String hostname = "?";
    java.net.InetAddress address = null;
    try
    {
      address = java.net.InetAddress.getByName(ip);
      hostname = address.getHostName();
    }
    catch (Exception e){}
    return(hostname);
  }

  // resolveName (DNS Name -> IP)
  public String resolveName(String dnsName)
  {
    String ip = "?";
    java.net.InetAddress address = null;
    try
    {
      address = java.net.InetAddress.getByName(dnsName);
      ip = address.getHostAddress();
    }
    catch (Exception e){}
    return(ip);
  }

  // Check if a client version is supported by this server version
  public boolean isCompatible(String clientVersion)
  {
    for (int i = 0; i < SUPPORTED_CLIENT_VERSIONS.length; i++)
    {
      if (SUPPORTED_CLIENT_VERSIONS[i].equals(clientVersion)) return(true);
    }
    return(false);
  }
}
