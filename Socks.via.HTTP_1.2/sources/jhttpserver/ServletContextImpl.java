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

// Title :        ServletContextImpl.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Basic implementation of ServletContext

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ServletContextImpl implements ServletContext
{
  private Hashtable attributes;
  private JHttpServer server;

  public ServletContextImpl(JHttpServer server)
  {
    super();
    attributes = new Hashtable();
    this.server = server;
  }

  public void setAttribute(String name, Object object)
  {
    attributes.put(name, object);
  }

  public void removeAttribute(String name)
  {
    attributes.remove(name);
  }

  public void log(String message, Throwable throwable)
  {
    server.printlnError(message);
  }

  public void log(String message)
  {
    server.printlnInfo(message);
  }

  public void log(Exception e, String msg)
  {
    log(msg, e);
  }

  public Enumeration getServlets()
  {
    return(new Vector().elements());
  }

  public Enumeration getServletNames()
  {
    return(new Vector().elements());
  }

  public Servlet getServlet(String name)
  {
    return(null);
  }

  public String getServerInfo()
  {
    return(server.getServerInfo());
  }

  public InputStream getResourceAsStream(String path)
  {
    return(null);
  }

  public URL getResource(String path)
  {
    return(null);
  }

  public RequestDispatcher getRequestDispatcher(String name)
  {
    return(null);
  }

  public RequestDispatcher getNamedDispatcher(String name)
  {
    return(null);
  }

  public String getRealPath(String path)
  {
    return(path);
  }

  public int getMinorVersion()
  {
    return(2);
  }

  public int getMajorVersion()
  {
    return(2);
  }

  public String getMimeType(String file)
  {
    if (file == null) return("text/plain");
    int dpos = file.lastIndexOf(".");
    String extension = file.substring(dpos);
    if (extension.equalsIgnoreCase(".TXT")) return("text/plain");
    if (extension.equalsIgnoreCase(".HTM")) return("text/html");
    if (extension.equalsIgnoreCase(".HTML")) return("text/html");
    if (extension.equalsIgnoreCase(".XML")) return("text/xml");
    if (extension.equalsIgnoreCase(".CSS")) return("text/css");
    if (extension.equalsIgnoreCase(".PDF")) return("application/pdf");
    if (extension.equalsIgnoreCase(".RTF")) return("application/msword");
    if (extension.equalsIgnoreCase(".DOC")) return("application/msword");
    if (extension.equalsIgnoreCase(".EXE")) return("application/x-msdownload");
    if (extension.equalsIgnoreCase(".ZIP")) return("application/x-zip-compressed");
    if (extension.equalsIgnoreCase(".RAR")) return("application/x-rar-compressed");
    if (extension.equalsIgnoreCase(".TAR")) return("application/x-tar");
    if (extension.equalsIgnoreCase(".JPE")) return("image/jpeg");
    if (extension.equalsIgnoreCase(".JPG")) return("image/jpeg");
    if (extension.equalsIgnoreCase(".JPEG")) return("image/jpeg");
    if (extension.equalsIgnoreCase(".GIF")) return("image/gif");
    if (extension.equalsIgnoreCase(".PNG")) return("image/png");

    return("text/plain");
  }

  public Enumeration getInitParameterNames()
  {
    return(new Vector().elements());
  }

  public String getInitParameter(String name)
  {
    return(null);
  }

  public ServletContext getContext(String uripath)
  {
    return(this);
  }

  public Enumeration getAttributeNames()
  {
    return(attributes.keys());
  }

  public Object getAttribute(String name)
  {
    return(attributes.get(name));
  }
}
