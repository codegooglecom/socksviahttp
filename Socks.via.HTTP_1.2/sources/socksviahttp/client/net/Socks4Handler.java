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

// Title :        Socks4Handler.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks v4 Handler

package socksviahttp.client.net;

import socksviahttp.core.util.*;
import socksviahttp.core.net.*;
import socksviahttp.client.engine.*;

public class Socks4Handler extends GenericSocksHandler
{
  public static final int SOCKS4_REPLY_VERSION = 0; // Must be 0
  public static final int SOCKS4_OK = 90; // request granted
  public static final int SOCKS4_KO = 91; // request rejected or failed
  public static final int SOCKS4_IDENTD_KO = 92; // request rejected because SOCKS server cannot connect to identd on the client
  public static final int SOCKS4_USERID_KO = 93; // request rejected because the client program and identd report different user-ids

  public static final int SOCKS4_CONNECT_COMMAND = 1;  // CONNECT command code
  public static final int SOCKS4_BIND_COMMAND = 2;  // BIND command code (not supported yet)

  public Socks4Handler(Configuration configuration, byte[] tab, Connection conn)
  {
    super(configuration, tab, conn);

    label = "Socks4 Handler";
    version = ByteUtils.b2i(tab[0]);
    command = ByteUtils.b2i(tab[1]);
  }

  public boolean isHandled()
  {
    if (version != 4) return(false);

    // Test if basic Socks4 (and not Socks4a)
    if ((tab[4] != 0) || (tab[5] != 0) || (tab[6] != 0))
    {
      if (command != SOCKS4_BIND_COMMAND)
      {
        destPort = 256 * ByteUtils.b2i(tab[2]) + ByteUtils.b2i(tab[3]);
        destIP = "" + ByteUtils.b2i(tab[4]) + "." + ByteUtils.b2i(tab[5]) + "." + ByteUtils.b2i(tab[6]) + "." + ByteUtils.b2i(tab[7]);
        userId = new String(tab, 8, tab.length - 8 - 1);
        return(true);
      }
      else
      {
        // Socks4 BIND Command not supported yet
        configuration.printlnWarn("<CLIENT> Socks4 BIND command not supported yet");
      }
    }
    return(false);
  }

  public byte[] buildResponse(int responseType)
  {
    byte[] ret = new byte[8];

    String[] bytes = StringUtils.stringSplit(destIP, ".", false);
    ret[0] = (byte)SOCKS4_REPLY_VERSION;
    ret[1] = (byte)(responseType == RESPONSE_SUCCESS ? SOCKS4_OK : SOCKS4_KO);
    ret[2] = ByteUtils.i2b(destPort / 256);
    ret[3] = ByteUtils.i2b(destPort % 256);
    ret[4] = ByteUtils.i2b(Integer.parseInt(bytes[0]));
    ret[5] = ByteUtils.i2b(Integer.parseInt(bytes[1]));
    ret[6] = ByteUtils.i2b(Integer.parseInt(bytes[2]));
    ret[7] = ByteUtils.i2b(Integer.parseInt(bytes[3]));

    return (ret);
  }
}
