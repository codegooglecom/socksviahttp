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

// Title :        ServletAdmin.java
// Version :      1.2
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Administration Servlet

package socksviahttp.server.admin;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import socksviahttp.core.consts.*;
import socksviahttp.core.util.*;
import socksviahttp.server.ServletSocks;
import socksviahttp.server.engine.*;

public class ServletAdmin extends HttpServlet
{
  private static final String PROPERTIES_FILE = "socksviahttp.server.initsrv";

  private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
  private String accessLogin = null;
  private String accessPassword = null;
  private String accessHeader = null;
  private String adminUrl = null;

  // init method
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);

    // Init the logging
    ServletSocks.logInit();

    //
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);

    // Get the connection login & password
    accessLogin = socksviahttp.core.util.PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "socks.admin.login");
    accessPassword = socksviahttp.core.util.PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "socks.admin.password");
    accessHeader = "Basic " + Base64Encoder.encode(accessLogin + ":" + accessPassword);

    // Get the url of the servlet
    adminUrl = socksviahttp.core.util.PropertiesFileReader.getPropertyStringValue(PROPERTIES_FILE, "socks.admin.servlet.url");
  }

  // get method
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doPost(request, response);
  }

  // post method
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    // Get the remote IP
    String ip = request.getRemoteAddr();
    String login = request.getParameter("login");
    String password = request.getParameter("password");

    boolean nonSecureAuthentication = false;
    if ((login != null) && (password != null))
    {
      if (login.equals(accessLogin) && password.equals(accessPassword)) nonSecureAuthentication = true;
      else ServletSocks.logConfiguration.printlnWarn("Admin refused for IP " + ip + " (bad old-school authentication)");
    }

    if (!nonSecureAuthentication)
    {
      String auth = request.getHeader("Authorization");
      if (auth == null)
      {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + Const.APPLICATION_NAME + " authentication\"");
        response.sendError(response.SC_UNAUTHORIZED, "Authorization Required");
        return;
      }

      if (!accessHeader.equals(auth))
      {
        // Log denied access
        ServletSocks.logConfiguration.printlnWarn("Admin refused for IP " + ip + " (bad HTTP authentication)");

        response.setHeader("WWW-Authenticate", "Basic realm=\"" + Const.APPLICATION_NAME + " authentication\"");
        response.sendError(response.SC_UNAUTHORIZED, "Authorization Required");
        return;
      }
    }

    // Do the job
    String sKey = request.getParameter("id_conn");
    if (sKey != null)
    {
      // Kick the user
      socksviahttp.server.engine.ExtendedConnection extConn = ServletSocks.table.get(sKey);
      if (extConn != null)
      {
        ServletSocks.logConfiguration.printlnInfo("Connection " + sKey + " : kicked...");
        extConn.conn.disconnect();
        // Shutdown extended connection
        extConn.shutdown();
        socksviahttp.server.ServletSocks.table.remove(sKey);
      }
    }

    // Set the headers
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);

    // Build the response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    out.println("<html>");
    out.println("<body>");
    out.println("<H1 align=CENTER>" + Const.APPLICATION_NAME + " v" + Const.APPLICATION_VERSION + " Administration</H1>");
    out.println("<BR><BR>");
    out.println("<FORM name=FormAdmin action=\"" + adminUrl + "\" METHOD=POST>");
    out.println("<TABLE width=95% border=1 align=center>");
    out.println("<TR>");
    out.println("<TH>&nbsp;</TH>");
    out.println("<TH>User</TH>");
    out.println("<TH>IP</TH>");
    out.println("<TH>Remote IP</TH>");
    out.println("<TH>Remote Port</TH>");
    out.println("<TH>Date of connection</TH>");
    out.println("<TH>Last Access(ms)</TH>");
    out.println("<TH>Bytes uploaded</TH>");
    out.println("<TH>Bytes downloaded</TH>");
    out.println("<TH>Upload speed (KB/s)</TH>");
    out.println("<TH>Download speed (KB/s)</TH>");
    out.println("</TR>");

    if (ServletSocks.table != null)
    {
      for (Enumeration e = ServletSocks.table.keys(); e.hasMoreElements(); )
      {
        String key = (String)e.nextElement();
        ExtendedConnection extConn = ServletSocks.table.get(key);
        if (extConn != null)
        {
          out.println("<TR>");
          out.println("<TD align=center><INPUT type=radio name=id_conn id=id_conn value=" + key + "></TD>");
          out.println("<TD align=center>" + extConn.user.login + "</TD>");
          out.println("<TD align=center>" + extConn.iprev + " (" + extConn.ip + ")</TD>");
          out.println("<TD align=center>" + extConn.destIPrev + " (" + extConn.destIP + ")</TD>");
          out.println("<TD align=center>" + extConn.destPort + "</TD>");
          out.println("<TD align=center>" + sdf.format(new Date(extConn.creationDate)) + "</TD>");
          out.println("<TD align=center>" + (new java.util.Date().getTime() - extConn.lastAccessDate) + "</TD>");
          out.println("<TD align=center>" + extConn.uploadedBytes + "</TD>");
          out.println("<TD align=center>" + extConn.downloadedBytes + "</TD>");
          out.println("<TD align=center>" + nf.format(extConn.currentUploadSpeed) + "</TD>");
          out.println("<TD align=center>" + nf.format(extConn.currentDownloadSpeed) + "</TD>");
          out.println("</TR>");
        }
      }
    }

    out.println("</TABLE>");
    out.println("<BR><BR>");
    if (nonSecureAuthentication)
    {
      out.println("<INPUT type=hidden name=login value=\"" + accessLogin + "\">");
      out.println("<INPUT type=hidden name=password value=\"" + accessPassword + "\">");
    }
    out.println("<INPUT type=submit value=\"Kick\">");
    out.println("</FORM>");
    out.println("</body>");
    out.println("</html>");
  }

  // destroy method
  public void destroy()
  {
  }
}
