/*
 * WebWorker.java
 */

/* Copyright (C) 1999 Dallas Semiconductor Corporation.
 * All rights Reserved. Printed in U.S.A.
 * This software is protected by copyright laws of
 * the United States and of foreign countries.
 * This material may also be protected by patent laws of the United States
 * and of foreign countries.
 * This software is furnished under a license agreement and/or a
 * nondisclosure agreement and may only be used or copied in accordance
 * with the terms of those agreements.
 * The mere transfer of this software does not imply any licenses
 * of trade secrets, proprietary technology, copyrights, patents,
 * trademarks, maskwork rights, or any other form of intellectual
 * property whatsoever. Dallas Semiconductor retains all ownership rights.
 */
/*
 $Workfile: WebWorker.java $
 $Revision: 6 $
 $Date: 5/22/00 9:25a $
 $Author: Lsmith $
 $Modtime: 5/22/00 9:25a $
 $Log: /JavaBIGNetWidget/Applications/TINIWebServer/src/WebWorker.java $
* 
* 6     5/22/00 9:25a Lsmith
* 
* 5     9/16/99 5:43p Tomc
*
* 4     9/13/99 1:35p Tomc
*
* 3     9/13/99 10:29a Tomc
*
* 2     9/10/99 4:18p Tomc
*
* 1     9/10/99 4:07p Tomc
*/

import java.io.*;
import com.dalsemi.tininet.http.*;
//import com.dalsemi.nethack.*;

/** This class runs a web server.
  */
public class WebWorker
  implements Runnable
{
  Object  lock; // lock for file access
  String  threadName;
  byte[]  name;

  HTTPServer    httpServer;

  public int    httpPort    = HTTPServer.DEFAULT_HTTP_PORT;
  public String webRoot     = "/";
  public String webIndex    = "index.html";
  public String webLog      = "/web.log";
  boolean       debugOn     = false;
  /** Constructor
    */
  public WebWorker(Object lock)
  {
    try
    {
      this.lock = lock;
      // create an instance of HTTPServer on port httpPort
      httpServer  = new HTTPServer(httpPort);
      // override the default index page
      httpServer.setIndexPage(webIndex);
      // override the default HTTP root
      httpServer.setHTTPRoot(webRoot);
      // override the default log file name
      httpServer.setLogFilename(webLog);
    }
    catch(HTTPServerException h)
    {
      if(debugOn)
      {
        System.out.println(h.toString());
      }
    }

    boolean  loggingFailed = false;

    try
    {
      // enable logging
      // NOTE: the log file is always appended and
      //       will eventually consume all free memory
      //       if it is not managed.
      httpServer.setLogging(true);
    }
    catch(HTTPServerException h)
    {
      // problem with log file
      loggingFailed = true;

      if(debugOn)
      {
        System.out.println(h.toString());
      }
    }

    try
    {
      if(loggingFailed)
        httpServer.setLogging(false);
    }
    catch(HTTPServerException h)
    {
      if(debugOn)
      {
        // no need to do anything here? if we can't close it, we can't close it
        System.out.println(h.toString());
      }
    }
  }
  /** Get the web server's root directory.
    */
  String getWebRoot()
  {
    return httpServer.getHTTPRoot();
  }
  /** Get the web server's default page
    */
  String getWebPage()
  {
    return httpServer.getIndexPage();
  }
  /** Run the server thread
    */
  public void run()
  {
    threadName  = Thread.currentThread().getName();
    name        = (threadName+"\n").getBytes();

    if(debugOn)
    {
      System.out.println(threadName);
    }

    int result = 0;

    while(true)
    {
      try
      {
          // Threaded web server blocks on accept
        result = httpServer.serviceRequests(lock);
        
        if(debugOn)
        {
          System.out.println(new String("<"+result+">"));
        }
      }
      catch(HTTPServerException h)
      {
        if(debugOn)
        {
          System.out.println(h.toString());
        }
      }
      catch(Throwable t)
      {
        if(debugOn)
        {
          // why kill the server if the exception is not fatal?
          System.out.println(t.toString());
        }
      }
    }
  }
}
