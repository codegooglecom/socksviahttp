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

// Title :        ServerInfo.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Informations on a server

package socksviahttp.client.engine;

import java.net.*;

public class ServerInfo
{
  private String serverName;
  private URL url = null;
  private String user = null;
  private String password = null;
  private long timeout = 0;
  private boolean zipData = true;
  private boolean encryptData = false;
  private String encryptionKey = "";

  public ServerInfo()
  {
    super();
  }
  public String getPassword()
  {
    return password;
  }
  public long getTimeout()
  {
    return timeout;
  }
  public URL getUrl()
  {
    return url;
  }
  public String getUser()
  {
    return user;
  }
  public void setPassword(String password)
  {
    this.password = password;
  }
  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }
  public void setUser(String user)
  {
    this.user = user;
  }
  public String getServerName()
  {
    return serverName;
  }
  public void setServerName(String serverName)
  {
    this.serverName = serverName;
  }
  public void setUrl(URL url)
  {
    this.url = url;
  }
  public boolean isEncryptData()
  {
    return encryptData;
  }
  public String getEncryptionKey()
  {
    return encryptionKey;
  }
  public void setEncryptData(boolean encryptData)
  {
    this.encryptData = encryptData;
  }
  public void setEncryptionKey(String encryptionKey)
  {
    this.encryptionKey = encryptionKey;
  }
  public boolean isZipData()
  {
    return zipData;
  }
  public void setZipData(boolean zipData)
  {
    this.zipData = zipData;
  }
}
