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

// Title :        ThreadCommunication.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Communication between socks via HTTP client & the servlet (HTTP Tunneling)

package socksviahttp.client.engine;

import java.net.*;
import java.io.*;
import java.util.zip.*;

import socksviahttp.core.consts.*;
import common.log.*;
import socksviahttp.core.net.*;
import socksviahttp.core.util.*;
import socksviahttp.client.net.*;

public abstract class ThreadCommunication extends Thread
{
  protected ServerInfo server = null;
  protected Connection source = null;
  protected String id_conn = null;
  protected String destinationUri = null;
  protected Configuration configuration = null;
  protected boolean requestOnlyIfClientActivity = false;

  public ThreadCommunication()
  {
    super();
  }

  public abstract boolean init();

  // Main task
  public void run()
  {
    if (!init())
    {
      configuration.printlnError("<CLIENT> Disconnecting application");
      source.disconnect();
      return;
    }

    boolean dialogInProgress = true;
    byte[] line;
    long initialTime = new java.util.Date().getTime();
    long lastUpdateTime = initialTime;
    long lastDataReceivedTime = initialTime;
    long lastDataSentTime = initialTime;

    // Init spy mode logfiles
    ThreadFileWriter tfdClient = null;
    ThreadFileWriter tfdServer = null;
    if ((configuration.getSpyMode() == Configuration.SPY_MODE_CLIENT) || (configuration.getSpyMode() == Configuration.SPY_MODE_BOTH))
    {
      tfdClient = new ThreadFileWriter(id_conn + "_svhc_fromapp.log");
      if (tfdClient.init()) tfdClient.start();
      else tfdClient = null;
    }
    if ((configuration.getSpyMode() == Configuration.SPY_MODE_SERVER) || (configuration.getSpyMode() == Configuration.SPY_MODE_BOTH))
    {
      tfdServer = new ThreadFileWriter(id_conn + "_svhc_toapp.log");
      if (tfdServer.init()) tfdServer.start();
      else tfdServer = null;
    }

    while (dialogInProgress == true)
    {
      try
      {
        line = source.read();

        long now = new java.util.Date().getTime();

        // Check if we have to start minimizing HTTP traffic
        if (now - initialTime > configuration.getDontTryToMinimizeTrafficBefore()) requestOnlyIfClientActivity = configuration.isRequestOnlyIfClientActivity();

        boolean forceRequest = (now > configuration.getForceRequestAfter() + lastUpdateTime);
        if (configuration.getForceRequestAfter() == 0) forceRequest = false;
        boolean requestBecauseDataReceived = (now < lastDataReceivedTime + configuration.getContinueRequestingAfterDataReceivedDuring());
        boolean requestBecauseDataSent = (now < lastDataSentTime + configuration.getContinueRequestingAfterDataSentDuring());
        if ((!requestOnlyIfClientActivity) || (forceRequest) || (requestBecauseDataReceived) || (requestBecauseDataSent) || (line == null) || (line.length > 0))
        {
          lastUpdateTime = new java.util.Date().getTime();
          DataPacket dataPacket = new DataPacket();
          dataPacket.id = id_conn;

          if (line == null)
          {
            // Connection closed
            configuration.printlnInfo("<CLIENT> Application closed the connection");
            configuration.printlnInfo("<CLIENT> " + server.getServerName() + ", close the connection " + id_conn);
            requestOnlyIfClientActivity = false; // Speeds up the shutdown
            dataPacket.type = Const.CONNECTION_DESTROY;
            dataPacket.tab = Const.TAB_EMPTY;
          }
          else
          {
            //configuration.printlnDebug("<CLIENT> " + server.getServerName() + ", update the connection " + id_conn);
            dataPacket.type = Const.CONNECTION_REQUEST;
            dataPacket.tab = line;

            if ((configuration.getSpyMode() == Configuration.SPY_MODE_CLIENT) || (configuration.getSpyMode() == Configuration.SPY_MODE_BOTH))
            {
              // Log server data to file
              if (tfdClient != null)
              {
                if (line.length > 0) tfdClient.addLogMessage(new LogMessage(line));
                //if (line.length > 0) new DiskAccess(id_conn + "_client.log", line).start();
              }
            }

            if (line.length > 0)
            {
              lastDataSentTime = new java.util.Date().getTime();
            }
          }

          // Configure the DataPacket
          dataPacket.zipData = server.isZipData();
          dataPacket.encryptData = server.isEncryptData();
          dataPacket.encryptionKey = server.getEncryptionKey().getBytes();

          // Send the message
          boolean packetTransmitted = false;
          int retry = 0;
          DataPacket response = null;
          while((!packetTransmitted) && (retry < 1 + configuration.getMaxRetries()))
          {
            try
            {
              response = sendHttpMessage(configuration, server, dataPacket);
              packetTransmitted = true;
            }
            catch(Exception e)
            {
              retry++;
              configuration.printlnWarn("<CLIENT> Cannot reach " + server.getServerName() + " (try #" + retry + "). Exception : " + e);
              Thread.sleep(configuration.getDelayBetweenTries());
            }
          }
          if (retry == 1 + configuration.getMaxRetries())
          {
            configuration.printlnError("<CLIENT> The maximum number of retries has been done");
            configuration.printlnError("<CLIENT> Disconnecting application");
            source.disconnect();
            dialogInProgress = false;
            // Close spy logfiles
            if (tfdClient != null) tfdClient.shutdown();
            if (tfdServer != null) tfdServer.shutdown();
            return;
          }

          if (response.errorCode != 0)
          {
            configuration.printlnError("<CLIENT> CRC Error. Check your secret encryption key");
            configuration.printlnError("<CLIENT> Disconnecting application");
            source.disconnect();
            dialogInProgress = false;
            // Close spy logfiles
            if (tfdClient != null) tfdClient.shutdown();
            if (tfdServer != null) tfdServer.shutdown();
            return;
          }

          // Write the received bytes
          switch (response.type)
          {
            case Const.CONNECTION_WRONG_ENCRYPTION_KEY:
              String serverMessage = new String(response.tab);
              configuration.printlnError("<" + server.getServerName() + "> " + serverMessage);
              // Close the source connection
              configuration.printlnInfo("<CLIENT> Disconnecting application");
              source.disconnect();

              // Stop the thread
              dialogInProgress = false;
              break;
            case Const.CONNECTION_RESPONSE:
              /*if ((configuration.getSpyMode() == Configuration.SPY_MODE_SERVER) || (configuration.getSpyMode() == Configuration.SPY_MODE_BOTH))
              {
                // Log server data to file
                if (response.tab.length > 0) new DiskAccess(id_conn + "_client.log", response.tab).start();
              }*/
              if (tfdServer != null)
              {
                if (response.tab.length > 0) tfdServer.addLogMessage(new LogMessage(response.tab));
              }
              if (response.tab.length > 0)
              {
                lastDataReceivedTime = new java.util.Date().getTime();
                source.write(response.tab);
              }
              break;
            case Const.CONNECTION_NOT_FOUND:
              configuration.printlnError("<" + server.getServerName() + "> Connection not found : " + id_conn);
              break;
            case Const.CONNECTION_DESTROY_OK:
              configuration.printlnInfo("<" + server.getServerName() + "> As CLIENT asked, connection closed : " + id_conn);
              break;
            default:
              configuration.printlnWarn("<CLIENT> " + server.getServerName() + " sent an unexpected response type : " + response.type);
              break;
          }

          // If the connection has been closed
          if (response.isConnClosed)
          {
            // Log
            configuration.printlnInfo("<" + server.getServerName() + "> Remote server closed the connection : " + response.id);

            // Close the source connection
            configuration.printlnInfo("<CLIENT> Disconnecting application");
            source.disconnect();

            // Stop the thread
            dialogInProgress = false;
          }

          if (response.type == Const.CONNECTION_DESTROY_OK)
          {
            // Close the source connection
            configuration.printlnInfo("<CLIENT> Disconnecting application");
            source.disconnect();

            // Stop the thread
            dialogInProgress = false;
          }

          if (response.type == Const.CONNECTION_NOT_FOUND)
          {
            // Close the source connection
            configuration.printlnError("<CLIENT> Disconnecting application");
            source.disconnect();

            // Stop the thread
            dialogInProgress = false;
          }
        }

        // Sleep
        //configuration.printlnDebug("<CLIENT> Sleeping " + configuration.getDelay() + " ms");
        if (dialogInProgress) Thread.sleep(configuration.getDelay());
      }
      catch (Exception e)
      {
        configuration.printlnError("<CLIENT> Unexpected Exception : " + e);
      }
    }
    // Close Spy logfiles
    if (tfdClient != null) tfdClient.shutdown();
    if (tfdServer != null) tfdServer.shutdown();
  }

  /*public static DataPacket sendHttpMessage(Configuration config, DataPacket source) throws IOException, ClassNotFoundException
  {
    // Send an HTTP message
    DataPacket ret = null;
    InputStream is = null;
    ObjectInputStream ois = null;

    HttpMessage mess = new HttpMessage(config.getUrl());
    if (config.isProxyNeedsAuthentication())
    {
      // Set the proxy authorization
      mess.setProxyAuthorization(config.getProxyUser(), config.getProxyPassword());
    }

    // Create the InputStream
    is = mess.sendGZippedPostMessage(source);

    // Create the GZIPInputStream
    GZIPInputStream zis = new GZIPInputStream(is);

    // Create the ObjectInputStream
    ois = new ObjectInputStream(zis);

    // Read the response
    ret = (DataPacket)ois.readObject();

    // Close the stream
    ois.close();

    // Return the value
    return(ret);
  }*/

  public static DataPacket sendHttpMessage(Configuration config, ServerInfo server, DataPacket source) throws IOException //, ClassNotFoundException
  {
    // Send an HTTP message
    HttpMessage mess = new HttpMessage(server.getUrl());
    if (config.isProxyNeedsAuthentication())
    {
      // Set the proxy authorization
      mess.setProxyAuthorization(config.getProxyUser(), config.getProxyPassword());
    }

    // Create the InputStream
    byte[] serialized = source.saveToByteArray();
    InputStream is = mess.sendByteArrayInPostMessage(source.saveToByteArray());

    // Read the response
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] tmpBuffer = new byte[65536];
    int n;
    while ((n = is.read(tmpBuffer)) >= 0) baos.write(tmpBuffer, 0, n);
    is.close();

    // Build the DataPacket
    DataPacket ret = new DataPacket();
    ret.encryptionKey = server.getEncryptionKey().getBytes();
    ret.loadFromByteArray(baos.toByteArray());

    // Return the value
    return(ret);
  }
}
