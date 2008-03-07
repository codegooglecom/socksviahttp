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

// Title :        GenericConnectionServer.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Tunneling Connection Server (Client part of socks via HTTP)

package socksviahttp.client.engine;

import java.net.*;
import java.io.*;

import socksviahttp.core.net.*;

public class GenericConnectionServer extends ConnectionServer
{
  protected Tunnel tunnel = null;

  public GenericConnectionServer(Configuration configuration, Tunnel tunnel)
  {
    super(configuration);
    this.tunnel = tunnel;
  }

  public boolean init()
  {
    // Let's start
    try
    {
      serverSocket = new ServerSocket(tunnel.getLocalPort());
      serverSocket.setSoTimeout(LISTEN_TIMEOUT);
    }
    catch (IOException e)
    {
      configuration.printlnError("<CLIENT> Unexpected Exception while creating ServerSocket in GenericConnectionServer : " + e);
      return(false);
    }
    return(true);
  }

  public void doListen()
  {
    try
    {
      Socket s = serverSocket.accept();
      if ((!s.getInetAddress().getHostAddress().equals(LOCALHOST_IP)) && configuration.isListenOnlyLocalhost())
      {
        // Log
        configuration.printlnWarn("<CLIENT> Incoming generic connection on localport " + tunnel.getLocalPort() + " refused from IP " + s.getInetAddress().getHostAddress());

        // Close the socket
        s.close();
      }
      else
      {
        configuration.printlnInfo("<CLIENT> Incoming generic connection on localport " + tunnel.getLocalPort() + " accepted from IP " + s.getInetAddress().getHostAddress());
        Connection conn = new Connection(s);
        ThreadCommunicationGeneric tcg = new ThreadCommunicationGeneric(conn, tunnel.getDestinationUri() , configuration);
        tcg.start();
      }
    }
    catch (InterruptedIOException iioe){}
    catch (Exception e)
    {
      configuration.printlnError("<CLIENT> Unexpected Exception while listening in GenericConnectionServer : " + e);
    }
  }
}
