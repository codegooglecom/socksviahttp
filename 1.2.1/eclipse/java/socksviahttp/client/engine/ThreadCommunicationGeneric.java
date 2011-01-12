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

// Title :        ThreadSocks.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Communication between socks via HTTP client & the servlet when the client uses a tunneled connection

package socksviahttp.client.engine;

import socksviahttp.core.consts.*;
import socksviahttp.core.net.*;

public class ThreadCommunicationGeneric extends ThreadCommunication
{
  public ThreadCommunicationGeneric(Connection source, String destinationUri, Configuration configuration)
  {
    super();
    this.source = source;
    this.destinationUri = destinationUri;
    this.configuration = configuration;
  }

  public boolean init()
  {
    configuration.printlnInfo("<CLIENT> An application asked a connection to " + destinationUri);

    // Get the server
    int type = Const.CONNECTION_UNSPECIFIED_TYPE;
    String serverInfoMessage = null;
    for (int i = 0; i < configuration.getServers().length; i++)
    {
      server = configuration.getServers()[i];

      // Create a connection on the servlet server
      DataPacket dataPacket = new DataPacket();
      dataPacket.type = Const.CONNECTION_CREATE;
      dataPacket.id = server.getUser() + ":" + server.getPassword() + ":" + server.getTimeout();
      dataPacket.tab = destinationUri.getBytes();

      // Send the connection
      try
      {
        configuration.printlnInfo("<CLIENT> " + server.getServerName() + ", create a connection to " + destinationUri);
        DataPacket response = sendHttpMessage(configuration, server, dataPacket); // May block here
        type = response.type;
        id_conn = response.id;
        serverInfoMessage = new String(response.tab);
        break;
      }
      catch(Exception e)
      {
        configuration.printlnWarn("<CLIENT> Cannot initiate a dialog with " + server.getServerName() + ". Exception : " + e);
        server = null;
        continue;
      }
    }

    if (server == null)
    {
      configuration.printlnError("<CLIENT> Cannot initiate a dialog with any server");
      return(false);
    }

    if (type == Const.CONNECTION_CREATE_OK)
    {
      configuration.printlnInfo("<" + server.getServerName() + "> Connection created : " + id_conn);
      //initOk = true;
      return(true);
    }
    else
    {
      configuration.printlnError("<" + server.getServerName() + "> " + serverInfoMessage);
    }
    return(false);
  }
}
