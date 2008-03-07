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

// Title :        ServletTest.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  A test servlet to check that the servlet engine is running OK

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class ServletTest extends HttpServlet
{
  private static final String CONTENT_TYPE = "text/html";
  /**Initialize global variables*/
  public void init() throws ServletException
  {
    getServletContext().log("From ServletTest : init");
  }

  /**Process the HTTP Get request*/
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setContentType(CONTENT_TYPE);
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head><title>ServletTest</title></head>");
    out.println("<body>");
    out.println("<p>The servlet has received a GET. This is the reply.</p>");
    out.println("<form action=" + request.getRequestURI() + " method=post>");
    out.println("<input type=submit value=post>");
    out.println("</form>");
    out.println("</body></html>");
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setContentType(CONTENT_TYPE);
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head><title>ServletTest</title></head>");
    out.println("<body>");
    out.println("<p>The servlet has received a POST. This is the reply.</p>");
    out.println("<form action=" + request.getRequestURI() + " method=get>");
    out.println("<input type=submit value=get>");
    out.println("</form>");
    out.println("</body></html>");
  }
  /**Clean up resources*/
  public void destroy()
  {
  }
}
