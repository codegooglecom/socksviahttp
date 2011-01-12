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

// Title :        UserInfo.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Represents a user

package socksviahttp.server.acl;

import java.util.*;

public class UserInfo
{
  public static final int SPY_MODE_NONE = 0;
  public static final int SPY_MODE_CLIENT = 1;
  public static final int SPY_MODE_SERVER = 2;
  public static final int SPY_MODE_BOTH = 3;

  public String login = null;
  public String password = null;
  public long authorizedTime = 0;
  public int spyMode = SPY_MODE_NONE;
  public Vector allowed_ip = new Vector();

  public UserInfo(String login, String password, int spyMode)
  {
    this.login = login;
    this.password = password;
    this.spyMode = spyMode;
  }

  public void addIp(String ip)
  {
    allowed_ip.add(ip);
  }

  public boolean isAuthorized(String ip)
  {
    for (int i = 0; i < allowed_ip.size(); i++)
    {
      String ipi = (String)allowed_ip.elementAt(i);
      if (wildmatch(ipi, ip) == true) return(true);
    }
    return(false);
  }

  // Returns true if input matches the mask (case insensitive)
  public static boolean wildmatch(String mask, String input)
  {
    if (mask == null || input == null)
    {
      return false;
    }

    String lowerMask = mask.trim().toLowerCase();
    String lowerInput = input.trim().toLowerCase();

    int maskLen = lowerMask.length();
    int inputLen = lowerInput.length();

    if ((inputLen <= 0) || (maskLen <= 0))
    {
      return false;
    }

    int maskIndex = 0;
    int inputIndex = 0;

    int nextMaskIndex = -1;
    int nextInputIndex = -1;

    char maskChar = '\0';
    boolean doingAsterisk = false;

    while(true)
    {
      /* look for end conditions */
      if (maskIndex >= maskLen)
      {
        if (doingAsterisk || (inputIndex >= inputLen)) return true;
        maskChar = '\0';
      }
      else
      {
        maskChar = lowerMask.charAt(maskIndex);
      }

      switch (maskChar)
      {
        case '*':
          doingAsterisk = true;
          nextMaskIndex = ++maskIndex;
          nextInputIndex = inputIndex;
          break;
        default:
          if (inputIndex >= inputLen) return false;

          doingAsterisk = false;
          if (maskChar != lowerInput.charAt(inputIndex))
          {
            if (nextMaskIndex != -1)
            {
              maskIndex = nextMaskIndex;
              inputIndex = nextInputIndex++;
            }
            else
            {
              return false;
            }

            if (inputIndex >= inputLen)
            {
              return false;
            }
          }
          else
          {
            maskIndex++;
            inputIndex++;
          }
          break;
      }
    }
  }
}
