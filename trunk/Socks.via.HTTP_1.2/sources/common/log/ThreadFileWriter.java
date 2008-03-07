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

// Title :        ThreadFileWriter.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Thread writing to logfile

package common.log;

import java.util.Vector;
import java.io.*;

public class ThreadFileWriter extends Thread implements LogQueue
{
  public static final int DELAY = 200;
  protected boolean isRunning = false;
  protected Vector queue;
  protected String fileName;
  protected FileOutputStream fos = null;

  public ThreadFileWriter(String fileName)
  {
    super();
    this.fileName = fileName;
    queue = new Vector(50, 10);
  }

  public synchronized boolean init()
  {
    // Open the file
    try
    {
      fos = new FileOutputStream(fileName, true);
    }
    catch(FileNotFoundException fnfe)
    {
      return(false);
    }

    // Return OK
    isRunning = true;
    return(true);
  }

  public synchronized void shutdown()
  {
    isRunning = false;
  }

  public synchronized void _shutdown()
  {
    if (fos != null)
    {
      try
      {
        // Flush the file
        fos.flush();

        // Close the file
        fos.close();
      }
      catch(IOException ioe){}
      fos = null;
    }
  }

  public boolean performQueueJob()
  {
    while(getQueueSize() > 0)
    {
      // Get the message
      LogMessage message = getMessage();

      // Write the message
      try
      {
        fos.write(message.getMessage());
        removeMessage();
      }
      catch(IOException ioe)
      {
        return(false);
      }
    }
    return(true);
  }

  public void run()
  {
    while(isRunning)
    {
      performQueueJob();

      try
      {
        fos.flush();
      }
      catch(IOException e){}
      try
      {
        Thread.sleep(DELAY);
      }
      catch(InterruptedException ie){}
    }
    performQueueJob();
    _shutdown();
  }

  // LogQueue Interface Methods
  public synchronized void addLogMessage(LogMessage message)
  {
    queue.add(message);
  }

  public synchronized LogMessage getMessage()
  {
    if (queue.size() == 0) return(null);
    return((LogMessage)queue.elementAt(0));
  }

  public synchronized void removeMessage()
  {
    if (queue.size() == 0) return;
    queue.remove(0);
  }

  public synchronized void clearQueue()
  {
    queue.clear();
  }

  public synchronized int getQueueSize()
  {
    return(queue.size());
  }
}
