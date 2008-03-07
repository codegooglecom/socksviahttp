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

// Title :        HttpServletRequestImpl.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Basic implementation of HttpServletRequest

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;

import java.security.Principal;

import jhttpserver.util.*;

public class HttpServletRequestImpl implements HttpServletRequest
{
  private Hashtable headers;
  private Hashtable parameters;
  private String method;
  //private String serverName;
  //private int serverPort;
  private Socket socket;
  private String protocol;
  private String requestURI;
  private String queryString;
  private String servletPath;
  private boolean postParametersTreated = false;
  private InputStream is;
  private JHttpServer server;

  public HttpServletRequestImpl()
  {
    super();
    headers = new Hashtable();
    parameters = new Hashtable();
  }

  public boolean isUserInRole(String role)
  {
    return(true);
  }

  public boolean isRequestedSessionIdValid()
  {
    return(true);
  }

  public boolean isRequestedSessionIdFromUrl()
  {
    return(true);
  }

  public boolean isRequestedSessionIdFromURL()
  {
    return(true);
  }

  public boolean isRequestedSessionIdFromCookie()
  {
    return(true);
  }

  public Principal getUserPrincipal()
  {
    return(null);
  }

  public HttpSession getSession(boolean create)
  {
    return(null);
  }

  public HttpSession getSession()
  {
    return(null);
  }

  public String getServletPath()
  {
    return(servletPath);
  }

  public String getRequestedSessionId()
  {
    return(null);
  }

  public String getRequestURI()
  {
    return(requestURI);
  }

  public String getRemoteUser()
  {
    return(null);
  }

  public String getPathInfo()
  {
    return(null);
  }

  public String getQueryString()
  {
    return(queryString);
  }

  public String getPathTranslated()
  {
    return(null);
  }

  public String getMethod()
  {
    return(method);
  }

  public int getIntHeader(String name)
  {
    String val = getHeader(name);
    if (val == null) return(-1);
    return(Integer.parseInt(val));
  }

  public Enumeration getHeaders(String name)
  {
    return(null);
  }

  public Enumeration getHeaderNames()
  {
    return(headers.keys());
  }

  public String getHeader(String name)
  {
    Object obj = headers.get(name);
    if (obj == null) return(null);
    return((String)obj);
  }

  public long getDateHeader(String name)
  {
    return(0);
  }

  public Cookie[] getCookies()
  {
    return(null);
  }

  public String getContextPath()
  {
    return("");
  }

  public String getAuthType()
  {
    return(null);
  }

  public void setAttribute(String name, Object o)
  {
  }

  public void removeAttribute(String name)
  {
  }

  public boolean isSecure()
  {
    return(false);
  }

  public int getServerPort()
  {
    return(server.getServerPort());
  }

  public String getServerName()
  {
    return(server.getServerName());
  }

  public String getScheme()
  {
    return("http");
  }

  public RequestDispatcher getRequestDispatcher(String path)
  {
    return(null);
  }

  public String getRemoteHost()
  {
    return(socket.getInetAddress().getHostName());
  }

  public String getRemoteAddr()
  {
    return(socket.getInetAddress().getHostAddress());
  }

  public String getRealPath(String path)
  {
    return(null);
  }

  public BufferedReader getReader() throws IOException
  {
    return(new BufferedReader(new InputStreamReader(is)));
  }

  public String getProtocol()
  {
    return(protocol);
  }

  public String[] getParameterValues(String name)
  {
    try
    {
      getPostParameters();
    }
    catch(IOException e)
    {
      server.printlnWarn("Exception while getting post parameters : " + e);
    }
    String[] ret = (String[])parameters.values().toArray(new String[0]);
    return(ret);
  }

  public Enumeration getParameterNames()
  {
    try
    {
      getPostParameters();
    }
    catch(IOException e)
    {
      server.printlnWarn("Exception while getting post parameters : " + e);
    }
    return(parameters.keys());
  }

  public String getParameter(String name)
  {
    try
    {
      getPostParameters();
    }
    catch(IOException e)
    {
      server.printlnWarn("Exception while getting post parameters : " + e);
    }
    Object val = parameters.get(name);
    if (val == null) return(null);
    return((String)val);
  }

  public Enumeration getLocales()
  {
    return(null);
  }

  public Locale getLocale()
  {
    return(null);
  }

  public ServletInputStream getInputStream() throws IOException
  {
    return(new ServletInputStreamImpl(is));
  }

  public String getContentType()
  {
    // TO CHANGE (charset)
    return(getHeader("Content-Type"));
  }

  public int getContentLength()
  {
    return(getIntHeader("Content-Length"));
  }

  public String getCharacterEncoding()
  {
    return(null);
  }

  public Enumeration getAttributeNames()
  {
    return(null);
  }

  public Object getAttribute(String name)
  {
    return(null);
  }

  void setMethod(String method)
  {
    this.method = method;
  }

  Socket getSocket()
  {
    return socket;
  }
  void setSocket(Socket socket)
  {
    this.socket = socket;
  }
  void setHeader(String name, String value)
  {
    headers.put(name, value);
  }
  void setProtocol(String protocol)
  {
    this.protocol = protocol;
  }
  void setRequestURI(String requestURI)
  {
    this.requestURI = requestURI;
  }
  void setQueryString(String queryString)
  {
    this.queryString = queryString;
  }
  void computeQueryParameters()
  {
    String[] tab = StringUtils.stringSplit(queryString, "&", false);
    for (int i = 0; i < tab.length; i++)
    {
      int vpos = tab[i].indexOf("=");
      parameters.put(tab[i].substring(0, vpos), URLDecoder.decode(tab[i].substring(1 + vpos)));
    }
  }
  void setServletPath(String servletPath)
  {
    this.servletPath = servletPath;
  }

  private void getPostParameters() throws IOException
  {
    if (postParametersTreated) return;
    if (method.equalsIgnoreCase("GET"))
    {
      postParametersTreated = true;
      return;
    }
    int contentLength = getContentLength();
    if (contentLength <= 0)
    {
      postParametersTreated = true;
      return;
    }

    byte[] buf = new byte[contentLength];
    int readLen = 0;
    boolean cont = true;
    while (cont && (readLen < contentLength))
    {
      int len = is.read(buf, readLen, contentLength - readLen);
      if (len == -1) cont = false;
      else readLen += len;
    }

    String line = new String(buf, 0, readLen);

    // Compute post parameters
    String[] tab = StringUtils.stringSplit(line, "&", false);
    for (int i = 0; i < tab.length; i++)
    {
      int vpos = tab[i].indexOf("=");
      parameters.put(tab[i].substring(0, vpos), URLDecoder.decode(tab[i].substring(1 + vpos)));
    }
    //
    postParametersTreated = true;
  }
  void setInputStream(InputStream is)
  {
    this.is = is;
  }
  public JHttpServer getServer()
  {
    return server;
  }
  public void setServer(JHttpServer server)
  {
    this.server = server;
  }
}
