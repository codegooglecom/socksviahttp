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

// Title :        Log.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Perform logging

package common.log;

import java.util.Date;

public class Log
{
  public static final int LOG_ALL = 0;
  public static final int LOG_DEBUG = 1;
  public static final int LOG_INFO = 2;
  public static final int LOG_WARN = 3;
  public static final int LOG_ERROR = 4;
  public static final int LOG_FATAL = 5;
  public static final int LOG_NONE = 6;

  /*public static void printlnLog(LogConfiguration config, int messageLevel, String message)
  {
    Date now = new Date();
    if (config.getConsoleLoggingLevel() <= messageLevel) System.out.println(config.getConsoleTimestampFormatter().format(now) + " - " + message + config.getConsoleCarriageReturn());
    if (config.getFileLoggingLevel() <= messageLevel) printLogFile(config.getLogFileName(), config.getFileTimestampFormatter().format(now) + " - " + message + config.getFileCarriageReturn());
  }

  // Write in file
  public static void printLogFile(String fileName, String message)
  {
    if ((fileName != null) && (fileName.length() > 0)) new DiskAccess(fileName, message.getBytes()).start();
  }*/
  public static void printlnLog(LogConfiguration config, int messageLevel, String message)
  {
    Date now = new Date();
    if (config.getConsoleLoggingLevel() <= messageLevel) System.out.println(config.getConsoleTimestampFormatter().format(now) + " - " + message + config.getConsoleCarriageReturn());
    if (config.getFileLoggingLevel() <= messageLevel)
    {
      String formattedMessage = config.getFileTimestampFormatter().format(now) + " - " + message + config.getFileCarriageReturn();
      logToFile(config, new LogMessage(formattedMessage.getBytes()));
    }
  }

  public static void logToFile(LogConfiguration config, LogMessage message)
  {
    if (config.getLogQueue() != null) config.getLogQueue().addLogMessage(message);
  }
}
