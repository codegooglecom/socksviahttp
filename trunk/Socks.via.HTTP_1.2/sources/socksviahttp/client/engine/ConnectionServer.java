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

// Title :        ConnectionServer.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Connection Server (Client part of socks via HTTP)

package socksviahttp.client.engine;

import java.net.*;
import java.io.*;

public abstract class ConnectionServer extends Thread
{
  public static final int LISTEN_TIMEOUT = 2000;
  public static final String LOCALHOST_IP = "127.0.0.1";

  protected ServerSocket serverSocket = null;
  public boolean listening = true;
  protected Configuration configuration = null;

  public ConnectionServer(Configuration configuration)
  {
    super();
    this.configuration = configuration;
  }

  /*public boolean checkServerVersion()
  {
    // Create a connection on the servlet server
    DataPacket dataPacket = new DataPacket();
    dataPacket.type = Const.CONNECTION_VERSION_REQUEST;
    dataPacket.id = Const.APPLICATION_VERSION;
    dataPacket.tab = "Version check".getBytes();

    // Send the connection
    int type = Const.CONNECTION_UNSPECIFIED_TYPE;
    String id = null;
    String serverInfoMessage = null;
    try
    {
      configuration.printlnInfo("<CLIENT> Version check : " + Const.APPLICATION_VERSION + " - URL : " + configuration.getUrl());
      DataPacket response = ThreadCommunication.sendHttpMessage(configuration, dataPacket);
      type = response.type;
      id = response.id;
    }
    catch(Exception e)
    {
      configuration.printlnFatal("<CLIENT> Version check : Cannot check the server version. Exception : " + e);
      return(false);
    }

    // Check the version
    if (type == Const.CONNECTION_VERSION_RESPONSE_KO)
    {
      configuration.printlnFatal("<SERVER> Version not supported. Version needed : " + id);
      return(false);
    }
    if (type == Const.CONNECTION_VERSION_RESPONSE_OK)
    {
      if (!Const.APPLICATION_VERSION.equals(id)) configuration.printlnWarn("<SERVER> Version supported but you should use version " + id);
      else configuration.printlnInfo("<SERVER> Version check : OK");
    }
    return(true);
  }*/

  public abstract void doListen();
  public abstract boolean init();

  public void run()
  {
    // Init
    if (!init()) return;

    // Main loop
    while(listening)
    {
      doListen();
    }

    // Shutdown
    try
    {
      // Close the ServerSocket
      serverSocket.close();
    }
    catch (IOException e){}
  }
}
