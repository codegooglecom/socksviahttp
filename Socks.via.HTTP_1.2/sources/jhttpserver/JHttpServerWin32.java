/*
This file is part of JHttpServer.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

JHttpServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JHttpServer; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        JHttpServerWin32.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Http Server launcher

package jhttpserver;

public class JHttpServerWin32 extends Thread
{
  String[] args;

  public JHttpServerWin32()
  {
    super();
  }

  public void run()
  {
    JHttpServer.main(args);
  }

  public static void main(String[] args)
  {
    JHttpServerWin32 launcher = new JHttpServerWin32();
    launcher.args = args;
    launcher.start();
  }
}
