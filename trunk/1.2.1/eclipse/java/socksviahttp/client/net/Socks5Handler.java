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

// Title :        Socks5Handler.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks v5 Handler

package socksviahttp.client.net;

import socksviahttp.core.util.*;
import socksviahttp.core.net.*;
import socksviahttp.client.engine.*;

public class Socks5Handler extends GenericSocksHandler
{
  // Authentication methods
  public final static int SOCKS5_NO_AUTHENTICATION_REQUIRED = 0;
  public final static int SOCKS5_GSSAPI = 1;  // not supported yet
  public final static int SOCKS5_USERNAME_PASSWORD = 2; // not supported yet
  public final static int SOCKS5_NO_ACCEPTABLE_METHODS = 255;

  // Socks commands
  public final static int SOCKS5_CONNECT_COMMAND = 1; // CONNECT command code
  public final static int SOCKS5_BIND_COMMAND = 2;  // BIND command code (not supported yet)
  public final static int SOCKS5_UDP_ASSOCIATE_COMMAND = 3; // UDP ASSOCIATE command code (not supported yet)

  // Address types
  public final static int SOCKS5_IPV4 = 1;
  public final static int SOCKS5_DOMAIN = 3;
  public final static int SOCKS5_IPV6 = 4; // Not supported yet

  // Responses
  public final static int SOCKS5_OK = 0;
  public final static int SOCKS5_KO = 1;

  protected Connection conn;

  public Socks5Handler(Configuration configuration, byte[] tab, Connection conn)
  {
    super(configuration, tab, conn);
    this.conn = conn;

    label = "Socks5 Handler";
    version = ByteUtils.b2i(tab[0]);
  }

  public boolean isHandled()
  {
    if (version != 5) return(false);

    int numMethods = ByteUtils.b2i(tab[1]);
    boolean methodAccepted = false;

    if ((numMethods <= 0) || (numMethods != tab.length - 2))
    {
      configuration.printlnWarn("<CLIENT> Application sent a wrong version identifier/method selection message while in Socks5 transaction");
      return(false);
    }

    byte[] handshake = new byte[2];
    int i = 0;
    while ((i < numMethods) && (!methodAccepted))
    {
      methodAccepted = (ByteUtils.b2i(tab[i + 2]) == SOCKS5_NO_AUTHENTICATION_REQUIRED);
      i++;
    }
    handshake[0] = 5; // version

    if (!methodAccepted)
    {
      // Write handshake
      configuration.printlnWarn("<CLIENT> Application requires authentication and this is not supported yet");
      handshake[1] = ByteUtils.i2b(SOCKS5_NO_ACCEPTABLE_METHODS);
      conn.write(handshake);
      return(false);
    }

    // Write handshake
    handshake[1] = ByteUtils.i2b(SOCKS5_NO_AUTHENTICATION_REQUIRED);
    conn.write(handshake);

    // Read response from client application
    byte[] request = conn.read();

    version = ByteUtils.b2i(request[0]);
    command = ByteUtils.b2i(request[1]);
    int addressType = ByteUtils.b2i(request[3]);

    if (version != 5)
    {
      configuration.printlnWarn("<CLIENT> Unexpected Socks version returned by application while in Socks5 transaction : " + version);
      return(false);
    }

    if (command == SOCKS5_CONNECT_COMMAND)
    {
      if (addressType == SOCKS5_IPV4)
      {
        destPort = 256 * ByteUtils.b2i(request[8]) + ByteUtils.b2i(request[9]);
        destIP = "" + ByteUtils.b2i(request[4]) + "." + ByteUtils.b2i(request[5]) + "." + ByteUtils.b2i(request[6]) + "." + ByteUtils.b2i(request[7]);
        return(true);
      }
      if (addressType == SOCKS5_DOMAIN)
      {
        int len = ByteUtils.b2i(request[4]);
        destIP = "0.0.0.0";
        dnsName = new String(request, 5, len);
        destPort = 256 * ByteUtils.b2i(request[len+5]) + ByteUtils.b2i(request[len+6]);
        return(true);
      }
      if (addressType == SOCKS5_IPV6)
      {
        configuration.printlnWarn("<CLIENT> Socks5 IPV6 addresses not supported yet");
      }
    }

    if (command == SOCKS5_BIND_COMMAND)
    {
      configuration.printlnWarn("<CLIENT> Socks5 BIND command not supported yet");
    }

    if (command == SOCKS5_UDP_ASSOCIATE_COMMAND)
    {
      configuration.printlnWarn("<CLIENT> Socks5 UDP ASSOCIATE command not supported yet");
    }

    // Return false
    return(false);
  }

  public byte[] buildResponse(int responseType)
  {
    byte[] ret = new byte[10];

    String[] bytes = StringUtils.stringSplit(destIP, ".", false);
    ret[0] = 5;
    ret[1] = (byte)(responseType == RESPONSE_SUCCESS ? SOCKS5_OK : SOCKS5_KO);
    ret[2] = 0;
    ret[3] = SOCKS5_IPV4;
    ret[4] = ByteUtils.i2b(Integer.parseInt(bytes[0]));
    ret[5] = ByteUtils.i2b(Integer.parseInt(bytes[1]));
    ret[6] = ByteUtils.i2b(Integer.parseInt(bytes[2]));
    ret[7] = ByteUtils.i2b(Integer.parseInt(bytes[3]));
    ret[8] = ByteUtils.i2b(destPort / 256);
    ret[9] = ByteUtils.i2b(destPort % 256);
    return (ret);
  }
}
