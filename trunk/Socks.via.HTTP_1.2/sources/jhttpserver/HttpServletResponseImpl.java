/*
This file is part of JHttpServer.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

JHttpServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTYge; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JHttpServer; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        HttpServletResponseImpl.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Basic implementation of HttpServletResponse

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class HttpServletResponseImpl implements HttpServletResponse
{
  private Hashtable headers;
  private String contentType = "text/plain";
  private Socket socket;
  //private OutputStream os;
  private BufferedOutputStream os;
  private ServletOutputStream sos = null;
  private PrintWriter pw = null;
  private String protocol;
  private Locale locale;
  private String charset;
  private boolean headersWrited = false;
  private int contentLength = -1;
  private JHttpServer server;
  public static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
  private int statusCode;
  private String statusMessage;
  private String errorMessage = null;

  public HttpServletResponseImpl()
  {
    super();
    headers = new Hashtable();
    locale = Locale.FRENCH;
    charset = "iso8859-1";
    statusCode = 200;
    statusMessage = "OK";
  }

  public void setStatus(int sc, String sm)
  {
    statusCode = sc;
    statusMessage = sm;
  }

  public void setStatus(int sc)
  {
    statusCode = sc;
    statusMessage = "";
  }

  public void setIntHeader(String name, int value)
  {
    headers.put(name, Integer.toString(value));
  }

  public void setHeader(String name, String value)
  {
    headers.put(name, value);
  }

  public void setDateHeader(String name, long date)
  {
    headers.put(name, HttpServletResponseImpl.sdf.format(new Date(date)));
  }

  public void sendRedirect(String location) throws IOException
  {
    statusCode = 301;
    statusMessage = "Moved Permanently";
    if (headersWrited) return;
    writeString(protocol + " " + statusCode + " " + statusMessage);
    writeString("\r\n");
    writeString("Date: " + sdf.format(new Date()));
    writeString("\r\n");
    writeString("Server: " + server.getServerInfo());
    writeString("\r\n");
    writeString("Location: " + location);
    writeString("\r\n");
    writeString("Connection: close"); // TO DO : check this header
    writeString("\r\n");
    writeString("Content-Type: " + contentType);
    writeString("\r\n");

    // Write the custom headers
    for (Enumeration e = headers.keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String value = (String)headers.get(key);
      writeString(key + ": " + value);
      writeString("\r\n");
    }

    writeString("\r\n");
    headersWrited = true;
  }

  public void sendError(int sc) throws IOException
  {
    sendError(sc, "Error " + sc);
  }

  public void sendError(int sc, String msg) throws IOException
  {
    if (headersWrited) return;
    writeString(protocol + " " + sc + " " + msg);
    writeString("\r\n");
    writeString("Date: " + sdf.format(new Date()));
    writeString("\r\n");
    writeString("Server: " + server.getServerInfo());
    writeString("\r\n");
    writeString("Connection: close"); // TO DO : check this header
    writeString("\r\n");
    writeString("Content-Type: " + contentType);
    writeString("\r\n");

    // Write the custom headers
    for (Enumeration e = headers.keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String value = (String)headers.get(key);
      writeString(key + ": " + value);
      writeString("\r\n");
    }

    writeString("\r\n");
    headersWrited = true;

    errorMessage = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">" +
                    "<HTML><HEAD>" +
                    "<TITLE>" + sc + " " + msg + "</TITLE>" +
                    "</HEAD><BODY>" +
                    "<H1>" + msg + "</H1>" +
                    "</BODY></HTML>";
  }

  public String encodeURL(String url)
  {
    return(URLEncoder.encode(url));
  }

  public String encodeUrl(String url)
  {
    return(encodeURL(url));
  }

  public String encodeRedirectURL(String url)
  {
    return(URLEncoder.encode(url));
  }

  public String encodeRedirectUrl(String url)
  {
    return(encodeRedirectURL(url));
  }

  public boolean containsHeader(String name)
  {
    return headers.contains(name);
  }

  public void addIntHeader(String name, int value)
  {
    headers.put(name, Integer.toString(value));
  }

  public void addHeader(String name, String value)
  {
    headers.put(name, value);
  }

  public void addDateHeader(String name, long date)
  {
    headers.put(name, HttpServletResponseImpl.sdf.format(new Date(date)));
  }

  public void addCookie(Cookie cookie)
  {
  }

  public void setLocale(Locale loc)
  {
    this.locale = loc;
  }

  public void setContentType(String type)
  {
    contentType = type;
  }

  public void setContentLength(int len)
  {
    contentLength = len;
  }

  public void setBufferSize(int size)
  {
  }

  public void reset()
  {
  }

  public boolean isCommitted()
  {
    return(true);
  }

  public PrintWriter getWriter() throws IOException
  {
    // On ecrit les headers
    writeHead();

    //
    pw = new PrintWriter(os);
    return(pw);
  }

  public ServletOutputStream getOutputStream() throws IOException
  {
    // On ecrit les headers
    writeHead();

    //
    sos = new ServletOutputStreamImpl(os);
    return(sos);
  }

  public Locale getLocale()
  {
    return(locale);
  }

  public String getCharacterEncoding()
  {
    return(charset);
  }

  public int getBufferSize()
  {
    return(0);
  }

  public void flushBuffer() throws IOException
  {
    if (sos != null)
    {
      sos.flush();
      return;
    }

    if (pw != null)
    {
      pw.flush();
      return;
    }

    os.flush();
  }

  void setOutputStream(BufferedOutputStream os)
  {
    this.os = os;
  }
  Socket getSocket()
  {
    return socket;
  }
  void setSocket(Socket socket)
  {
    this.socket = socket;
  }

  void writeHead()
  {
    if (headersWrited) return;
    writeString(protocol + " " + statusCode + " " + statusMessage);
    writeString("\r\n");
    writeString("Date: " + sdf.format(new Date()));
    writeString("\r\n");
    writeString("Server: " + server.getServerInfo());
    writeString("\r\n");

    writeString("Connection: close"); // TO DO : check this header
    writeString("\r\n");
    writeString("Content-Type: " + contentType);
    writeString("\r\n");

    // Write the custom headers
    for (Enumeration e = headers.keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String value = (String)headers.get(key);
      writeString(key + ": " + value);
      writeString("\r\n");
    }

    if (contentLength != -1)
    {
      writeString("Content-Length: " + contentLength);
      writeString("\r\n");
    }

    writeString("\r\n");
    headersWrited = true;
  }

  void writeErrorMessage()
  {
    if (errorMessage == null) return;
    writeString(errorMessage);
  }

  private void writeString(String toWrite)
  {
    try
    {
      os.write(toWrite.getBytes());
    }
    catch (IOException e)
    {
      server.printlnError("Exception while writing string : " + e);
    }
  }

  void close() throws IOException
  {
    if (sos != null)
    {
      sos.flush();
      sos.close();
      return;
    }
    if (pw != null)
    {
      pw.flush();
      pw.close();
      return;
    }
    os.flush();
    os.close();
  }

  void setProtocol(String protocol)
  {
    this.protocol = protocol;
  }
  String getProtocol()
  {
    return protocol;
  }
  void setServer(JHttpServer server)
  {
    this.server = server;
  }
  public JHttpServer getServer()
  {
    return server;
  }
}
