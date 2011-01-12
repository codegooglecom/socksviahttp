/*
This file is part of Socks via HTTP & JHttpServer.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

Socks via HTTP & JHttpServer are distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Socks via HTTP & JHttpServer; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        LogQueue.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Log queue

package common.log;

public interface LogQueue
{
  public void addLogMessage(LogMessage message);
  public LogMessage getMessage();
  public void removeMessage();
  public int getQueueSize();
  public void clearQueue();
}
