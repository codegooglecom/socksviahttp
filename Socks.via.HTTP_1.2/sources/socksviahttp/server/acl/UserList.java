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

// Title :        UserList.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Contains all the users

package socksviahttp.server.acl;

import java.util.*;

public class UserList
{
  private Vector m_userlist = new Vector();

  public UserList()
  {
  }

  public void addUser(UserInfo user)
  {
    m_userlist.add(user);
  }

  public UserInfo getUser(String login)
  {
    for (int i = 0; i < m_userlist.size(); i++)
    {
      UserInfo user = (UserInfo)m_userlist.elementAt(i);
      if (user.login.equals(login)) return(user);
    }
    return(null);
  }
}
