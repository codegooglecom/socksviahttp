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

// Title :        ExtendedConnection.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Connection wrapper (with extended attributes)

package socksviahttp.server.engine;

import common.log.*;

public class ExtendedConnection
{
  public String ip = "?";
  public String iprev = "?";
  public socksviahttp.server.acl.UserInfo user = null;
  public socksviahttp.core.net.Connection conn = null;
  public long creationDate = new java.util.Date().getTime();
  public long lastAccessDate = new java.util.Date().getTime();
  public long uploadedBytes = 0;
  public long downloadedBytes = 0;
  public String destIP = "?";
  public String destIPrev = "?";
  public int destPort = 0;
  public double currentUploadSpeed = 0;
  public double currentDownloadSpeed = 0;
  public long timeout = 0;
  public long authorizedTime = 0;
  public ThreadBind threadBind = null;
  public ThreadFileWriter tfdClient = null;
  public ThreadFileWriter tfdServer = null;

  public boolean shutdown()
  {
    if (tfdClient != null)
    {
      tfdClient.shutdown();
      tfdClient = null;
    }
    if (tfdServer != null)
    {
      tfdServer.shutdown();
      tfdServer = null;
    }
    return(true);
  }
}
