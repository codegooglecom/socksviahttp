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
// Description :  Communication between socks via HTTP client & the servlet when the client uses the socks server

package socksviahttp.client.engine;

import socksviahttp.core.consts.*;
import socksviahttp.core.net.*;
import socksviahttp.core.util.*;
import socksviahttp.client.net.*;

public class ThreadCommunicationSocks extends ThreadCommunication
{
  public ThreadCommunicationSocks(Connection source, Configuration configuration)
  {
    super();
    this.source = source;
    this.configuration = configuration;
  }

  public boolean init()
  {
    //this.requestOnlyIfClientActivity = configuration.isRequestOnlyIfClientActivity();
    this.requestOnlyIfClientActivity = false;

    // Get the destination
    GenericSocksHandler socksHandler = null;
    try
    {
      socksHandler = GenericSocksHandler.getHandler(this.configuration, this.source);
    }
    catch(SocksException e)
    {
      configuration.printlnWarn("<CLIENT> " + e.getMessage());
      return(false);
    }

    destinationUri = (socksHandler.getDnsName() != null ? socksHandler.getDnsName() : socksHandler.getDestIP()) + ":" + socksHandler.getDestPort();
    configuration.printlnInfo("<CLIENT> An application asked a connection to " + destinationUri);
    configuration.printlnDebug("<CLIENT> Handler type : " + socksHandler.getLabel());

    // Get the server
    int type = Const.CONNECTION_UNSPECIFIED_TYPE;
    String serverInfoMessage = null;
    for (int i = 0; i < configuration.getServers().length; i++)
    {
      server = configuration.getServers()[i];

      // Create a connection request
      DataPacket dataPacket = new DataPacket();
      dataPacket.type = Const.CONNECTION_CREATE;
      dataPacket.id = server.getUser() + ":" + server.getPassword() + ":" + server.getTimeout();
      dataPacket.tab = destinationUri.getBytes();

      // Send the connection request to the HTTP server
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
      // Send the response packet to the socks client
      GenericSocksHandler replyPacket = socksHandler;
      this.source.write(replyPacket.buildResponse(GenericSocksHandler.RESPONSE_FAILURE));
      return(false);
    }

    if (type == Const.CONNECTION_CREATE_OK)
    {
      //initOk = true;
      configuration.printlnInfo("<" + server.getServerName() + "> Connection created : " + id_conn);
      configuration.printlnDebug("<" + server.getServerName() + "> Connected to " + serverInfoMessage);

      // Send the response packet to the socks client
      GenericSocksHandler replyPacket = socksHandler;
      String[] resp = StringUtils.stringSplit(serverInfoMessage, ":", false);
      replyPacket.setDestIP(resp[0]);
      replyPacket.setDestPort(Integer.parseInt(resp[1]));
      this.source.write(replyPacket.buildResponse(GenericSocksHandler.RESPONSE_SUCCESS));
      return(true);
    }
    else
    {
      configuration.printlnError("<" + server.getServerName() + "> " + serverInfoMessage);

      // Send the response packet to the socks client
      GenericSocksHandler replyPacket = socksHandler;
      this.source.write(replyPacket.buildResponse(GenericSocksHandler.RESPONSE_FAILURE));
    }
    return(false);
  }
}
