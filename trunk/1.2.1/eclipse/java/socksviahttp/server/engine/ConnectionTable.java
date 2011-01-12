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

// Title :        ConnectionTable.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Table of connections

package socksviahttp.server.engine;

import java.util.*;

public class ConnectionTable
{
  private Hashtable table = null;

  public ConnectionTable()
  {
    table = new Hashtable();
  }

  public ExtendedConnection put(String key, ExtendedConnection value)
  {
    Object old = table.put(key, value);
    if (old == null) return(null);
    return((ExtendedConnection)old);
  }

  public ExtendedConnection get(String key)
  {
    Object obj = table.get(key);
    if (obj == null) return(null);
    ExtendedConnection ret = (ExtendedConnection)obj;
    return(ret);
  }

  public ExtendedConnection remove(String key)
  {
    Object old = table.remove(key);
    if (old == null) return(null);
    return((ExtendedConnection)old);
  }

  public void clear()
  {
    table.clear();
  }

  public boolean isEmpty()
  {
    return(table.isEmpty());
  }

  public int size()
  {
    return(table.size());
  }

  public Enumeration elements()
  {
    return(table.elements());
  }

  public Enumeration keys()
  {
    return(table.keys());
  }
}
