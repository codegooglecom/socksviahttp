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

// Title :        CachedFile.java
// Version :      0.97
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO <jhttpserver@cqs.dyndns.org>
// Description :  A file in memory (cached)

package jhttpserver;

import java.io.*;

public class CachedFile
{
  private byte[] fileContent;
  private String fileName;
  private long modificationDate;

  public CachedFile()
  {
    super();
    fileName = null;
    fileContent = null;
    modificationDate = 0;
  }

  public void preloadFile(String fileName) throws IOException
  {
    if (fileContent != null) return;
    this.fileName = fileName;
    File f = new File(fileName);
    this.modificationDate = f.lastModified();
    FileInputStream fis = new FileInputStream(f);
    BufferedInputStream bis = new BufferedInputStream(fis);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[65536];
    boolean cont = true;
    while(cont)
    {
      int read = bis.read(buffer, 0, 65536);
      if (read == -1) cont = false;
      else
      {
        baos.write(buffer, 0, read);
      }
    }
    baos.close();
    bis.close();
    fileContent = baos.toByteArray();
  }

  public void flushFileCache() throws IOException
  {
    fileContent = null;
    preloadFile(fileName);
  }

  public byte[] getFileContent()
  {
    return(fileContent);
  }
  public long getModificationDate()
  {
    return modificationDate;
  }
}
