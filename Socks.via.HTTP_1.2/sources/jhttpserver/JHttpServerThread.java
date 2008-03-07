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

// Title :        HttpServerThread.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Http Server Thread (One instance per request)

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;
import java.util.*;

import jhttpserver.util.*;

public class JHttpServerThread extends Thread
{
  private JHttpServer server;
  private Socket socket;

  // Constructor
  public JHttpServerThread(JHttpServer server, Socket socket)
  {
    super();

    this.server = server;
    this.socket = socket;
  }

  public String readLine(InputStream is) throws IOException
  {
    StringBuffer bLine = new StringBuffer();
    boolean cont = true;
    boolean caution = false;
    while(cont)
    {
      int c = is.read();
      if (c == -1) cont = false;
      else
      {
        bLine.append((char)c);
        if (caution & (c == '\n')) cont = false;
        if (c == '\r') caution = true;
        else caution = false;
      }
    }
    String line = bLine.substring(0, bLine.length() - 2);
    return(line);
  }

  // Thread's run method
  public void run()
  {
    //System.out.println("Got connection from " + socket.getInetAddress().getHostAddress());

    InputStream is;

    try
    {
      is = socket.getInputStream();
    }
    catch (IOException e)
    {
      server.printlnError("Error while initializing streams : " + e);
      closeSocket();
      return;
    }

    boolean cont = true;
    int pos = 0;
    HttpServletRequestImpl request = new HttpServletRequestImpl();
    request.setServer(server);
    request.setSocket(socket);

    while(cont)
    {
      // TO CHANGE (optimize)
      try
      {
        String line = readLine(is);
        if ((line != null) && (line.length() > 0))
        {
          if (pos == 0)
          {
            // Command
            //System.out.println("COMMAND " + line);
            String[] tab = StringUtils.stringSplit(line, " ", false);
            String method = "GET";
            if (tab.length > 0) method = tab[0];
            String url = "/";
            if (tab.length > 1) url = tab[1];
            String protocol = "HTTP/1.0";
            if (tab.length > 2) protocol = tab[2];

            request.setMethod(method);

            int qpos = url.indexOf("?");
            if (qpos >= 0)
            {
              request.setRequestURI(url.substring(0, qpos));

              // TO CHANGE
              request.setServletPath(url.substring(0, qpos));

              request.setQueryString(url.substring(1 + qpos));
              request.computeQueryParameters();
            }
            else
            {
              request.setRequestURI(url);

              // TO CHANGE
              request.setServletPath(url);

              request.setQueryString(null);
            }
            request.setProtocol(protocol);
          }
          else
          {
            // Header
            //System.out.println("HEADER " + line);
            int tokenPos = line.indexOf(": ");
            String headerName = line.substring(0, tokenPos);
            String headerValue = line.substring(2 + tokenPos);
            request.setHeader(headerName, headerValue);
          }
          pos++;
        }
        else cont = false;
      }
      catch (IOException e)
      {
        //System.out.println("IOException in ThreadComm read : " + e);
        // Client closed the connection
        try
        {
          is.close();
        }
        catch(IOException ioe){}
        closeSocket();
        return;
      }
    }

    request.setInputStream(is);

    //
    HttpServletResponseImpl response = new HttpServletResponseImpl();
    response.setServer(server);
    response.setSocket(socket);
    response.setProtocol(request.getProtocol());
    try
    {
      response.setOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      //response.setOutputStream(socket.getOutputStream());
    }
    catch (IOException e)
    {
      server.printlnError("Exception : " + e);
    }

    GenericServlet servlet = null;
    String realUrl = server.getRealUrl(request.getRequestURI());
    try
    {
      int spos = realUrl.indexOf("/servlet/");
      String servletName = null;
      if (spos == -1)
      {
        if (server.getServerRoot() == null) throw new Exception("I'm not serving static documents");
        servletName = "jhttpserver.ServletSendFile";
      }
      else
      {
        servletName = realUrl.substring(9 + spos);
      }
      servlet = server.getLoadedServlet(servletName);
      if (servlet == null)
      {
        servlet = server.loadServlet(servletName);
      }
    }
    catch (Exception e)
    {
      //System.out.println("Exception while creating servlet : " + e);
      try
      {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
      }
      catch(IOException ioe){}
    }

    if (servlet != null)
    {
      try
      {
        servlet.service(request, response);
      }
      catch (Exception e)
      {
        server.printlnError("Exception while calling service : " + e);
      }
    }

    try
    {
      response.writeHead();
      //response.flushBuffer();
      response.writeErrorMessage();
      response.close();
    }
    catch (Exception e)
    {
      //System.out.println("Exception while closing response : " + e);
    }

    // Close input stream
    try
    {
      is.close();
    }
    catch (IOException e)
    {
      server.printlnWarn("Error while closing input stream : " + e);
    }

    // On ferme la socket
    closeSocket();

    //System.out.println("Disconnected of " + socket.getInetAddress().getHostAddress());
  }

  public void closeSocket()
  {
    try
    {
      socket.close();
    }
    catch (IOException e)
    {
      server.printlnWarn("Error while closing socket : " + e);
    }
  }
}
