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

// Title :        ThreadPing.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Check if a server connection is still used

package socksviahttp.server.engine;

import java.util.*;

import socksviahttp.server.ServletSocks;

public class ThreadPing extends Thread
{
  public static final int DELAY = 5000; // 5sec

  public ThreadPing()
  {
    super();
  }

  public void run()
  {
    boolean state = true;

    while (state == true)
    {
      // Scan the open connections
      Vector closeForTimeout = new Vector();
      Vector closeForAuthorizedTime = new Vector();
      if (ServletSocks.table != null)
      {
        for (Enumeration e = ServletSocks.table.keys(); e.hasMoreElements(); )
        {
          String key = (String)e.nextElement();
          ExtendedConnection extConn = ServletSocks.table.get(key);
          if ((extConn != null) && (extConn.timeout > 0))
          {
            if ((new java.util.Date().getTime() - extConn.lastAccessDate) > 1000 * extConn.timeout)
            {
              closeForTimeout.add(key);
            }
            else
            {
              if (extConn.authorizedTime > 0)
              {
                if ((new java.util.Date().getTime() - extConn.creationDate) > 1000 * extConn.authorizedTime)
                {
                  closeForAuthorizedTime.add(key);
                }
              }
            }
          }
        }
      }

      // Close the unused connections
      for (int i = 0; i < closeForTimeout.size(); i++)
      {
        String key = (String)closeForTimeout.elementAt(i);
        ExtendedConnection extConn = ServletSocks.table.get(key);
        if (extConn != null)
        {
          ServletSocks.logConfiguration.printlnInfo("Closed connection " + key + " : Timeout reached");
          extConn.conn.disconnect();
          // Shutdown extended connection
          extConn.shutdown();
          ServletSocks.table.remove(key);
        }
      }

      // Close the connections whose authorized time is over
      for (int i = 0; i < closeForAuthorizedTime.size(); i++)
      {
        String key = (String)closeForAuthorizedTime.elementAt(i);
        ExtendedConnection extConn = ServletSocks.table.get(key);
        if (extConn != null)
        {
          ServletSocks.logConfiguration.printlnInfo("Closed connection " + key + " : Maximum time reached");
          extConn.conn.disconnect();
          // Shutdown extended connection
          extConn.shutdown();
          ServletSocks.table.remove(key);
        }
      }

      try
      {
        // Sleep
        Thread.sleep(DELAY);
      }
      catch (Exception e){}
    }
  }
}
