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

// Title :        ServletConfigImpl.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  Basic implementation of ServletConfig

package jhttpserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class ServletConfigImpl implements ServletConfig
{
  private String servletName;
  private ServletContext servletContext;

  public ServletConfigImpl()
  {
    super();
  }

  public String getServletName()
  {
    return(servletName);
  }

  public ServletContext getServletContext()
  {
    return(servletContext);
  }

  public Enumeration getInitParameterNames()
  {
    return(new Vector().elements());
  }

  public String getInitParameter(String name)
  {
    return(null);
  }
  void setServletName(String servletName)
  {
    this.servletName = servletName;
  }

  void setServletContext(ServletContext servletContext)
  {
    this.servletContext = servletContext;
  }
}
