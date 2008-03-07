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

// Title :        LogConfiguration.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Logging Configuration

package common.log;

import java.text.SimpleDateFormat;

public class LogConfiguration
{
  protected SimpleDateFormat consoleTimestampFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  protected SimpleDateFormat fileTimestampFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  protected String consoleCarriageReturn = "";
  protected String fileCarriageReturn = "\r\n";

  protected int consoleLoggingLevel = Log.LOG_WARN;
  protected int fileLoggingLevel = Log.LOG_INFO;
  protected String logFileName = null;
  protected ThreadFileWriter tfd = null;

  public LogConfiguration()
  {
    super();
  }

  public boolean initLog()
  {
    if (logFileName == null) return(true);
    tfd = new ThreadFileWriter(logFileName);
    if (!tfd.init())
    {
      tfd = null;
      return(false);
    }
    tfd.start();
    return(true);
  }

  public LogQueue getLogQueue()
  {
    return(tfd);
  }

  public void printlnDebug(String message)
  {
    Log.printlnLog(this, Log.LOG_DEBUG, "DEBUG : " + message);
  }

  public void printlnInfo(String message)
  {
    Log.printlnLog(this, Log.LOG_INFO, "INFO  : " + message);
  }

  public void printlnWarn(String message)
  {
    Log.printlnLog(this, Log.LOG_WARN, "WARN  : " + message);
  }

  public void printlnError(String message)
  {
    Log.printlnLog(this, Log.LOG_ERROR, "ERROR : " + message);
  }

  public void printlnFatal(String message)
  {
    Log.printlnLog(this, Log.LOG_FATAL, "FATAL : " + message);
  }

  public int getConsoleLoggingLevel()
  {
    return consoleLoggingLevel;
  }
  public int getFileLoggingLevel()
  {
    return fileLoggingLevel;
  }
  public String getLogFileName()
  {
    return logFileName;
  }
  public SimpleDateFormat getFileTimestampFormatter()
  {
    return fileTimestampFormatter;
  }
  public SimpleDateFormat getConsoleTimestampFormatter()
  {
    return consoleTimestampFormatter;
  }
  public String getFileCarriageReturn()
  {
    return fileCarriageReturn;
  }
  public String getConsoleCarriageReturn()
  {
    return consoleCarriageReturn;
  }
  public void setConsoleCarriageReturn(String consoleCarriageReturn)
  {
    this.consoleCarriageReturn = consoleCarriageReturn;
  }
  public void setConsoleLoggingLevel(int consoleLoggingLevel)
  {
    this.consoleLoggingLevel = consoleLoggingLevel;
  }
  public void setConsoleTimestampFormatter(SimpleDateFormat consoleTimestampFormatter)
  {
    this.consoleTimestampFormatter = consoleTimestampFormatter;
  }
  public void setFileCarriageReturn(String fileCarriageReturn)
  {
    this.fileCarriageReturn = fileCarriageReturn;
  }
  public void setFileLoggingLevel(int fileLoggingLevel)
  {
    this.fileLoggingLevel = fileLoggingLevel;
  }
  public void setLogFileName(String LogFileName)
  {
    this.logFileName = LogFileName;
  }
  public void setFileTimestampFormatter(SimpleDateFormat fileTimestampFormatter)
  {
    this.fileTimestampFormatter = fileTimestampFormatter;
  }
}
