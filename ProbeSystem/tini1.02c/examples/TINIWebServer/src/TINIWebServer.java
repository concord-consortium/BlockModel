/*
 * TINIWebServer.java
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
 $Workfile: TINIWebServer.java $
 $Revision: 15 $
 $Date: 11/21/00 7:06a $
 $Author: Chenot $
 $Modtime: 11/21/00 7:06a $
 $Log: /TINI/firmware/Lorne/Examples/TINIWebServer/src/TINIWebServer.java $
* 
* 15    11/21/00 7:06a Chenot
* 
* 14    1/05/00 6:51a Tomc
* 
* 13    9/17/99 7:06a Tomc
*
* 10    9/13/99 2:51p Tomc
*
* 9     9/13/99 1:35p Tomc
*
* 8     9/13/99 10:29a Tomc
*
* 7     9/10/99 4:18p Tomc
*
* 6     9/10/99 4:06p Tomc
*
* 5     8/19/99 1:29a Tomc
*
* 2     8/18/99 3:20p Tomc
*
* 1     8/18/99 2:35p Tomc
*
* 17    8/17/99 7:38p Tomc
*
* 16    8/17/99 6:36p Tomc
*
* 15    8/17/99 6:13p Tomc
*
* 12    8/16/99 8:32p Tomc
*
* 10    8/03/99 10:28a Tomc
*
* 9     8/03/99 8:11a Tomc
*
* 7     7/28/99 10:11a Tomc
*
* 6     7/19/99 3:31p Tomc
*
* 4     7/19/99 12:03p Tomc
*
* 3     7/19/99 10:29a Tomc
*/

import java.net.*;
import java.io.*;
import java.util.*;
import com.dalsemi.system.*;

/** This class implements a web server using the HTTPServer class. Logging is implemented using
  * the HTTPServer default logging option. Applications may leave default logging disabled and
  * implement their own logging methods.
  */
public class TINIWebServer
{
  WebWorker         webWorker;
  TemperatureWorker temperatureWorker;

  int     prevTemp;
  int     prev100s,
          prev10s,
          prev1s;
  byte[]  copyBuffer;
  String  webRoot,
          webIndex;
  boolean lastButtonFound = true;
  boolean localPages      = false;
  static  Object  lock    = new Object();// lock for file access
  Clock   clock           = new Clock();

  static String indexTop   = "<HTML>"+
                               "<HEAD>"+
                                 "<TITLE>TINIWebServer</TITLE>"+
                               "</HEAD>"+
                               "<BODY COLOR=\"#0000FF\" BGCOLOR=\"#FFFFFF\" ALINK=\"#C0C0C0\">"+
                                 "<META HTTP-EQUIV=\"Expires\" CONTENT=\"0\">"+
                                 "<META HTTP-EQUIV=\"Last modified\" CONTENT=\"now\">"+
                                 "<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">"+
                                 "<META HTTP-EQUIV=\"Cache-Control\" CONTENT=\"no-cache, must-validate\">"+
                                 "<FONT ALIGN=\"CENTER\" COLOR=\"#0000FF\">"+
                                   "<H1 ALIGN=\"CENTER\">TINIWebServer</H1>"+
                                   "<H3 ALIGN=\"CENTER\">If you can read this, TINIWebServer is running<BR>On TINI</H3><BR>"+
                                   "<P ALIGN=\"CENTER\">"+
                                     "The TINIWebServer application uses the HTTPServer class to implement a simple web server.<BR>"+
                                   "</P>"+
                                   "<H1 ALIGN=\"CENTER\">Current temperature ";
  static String indexBottom  =     "</H1><BR>"+
                                 "</FONT>"+
                               "</BODY>"+
                             "</HTML>";
  /**  Constructor
    */
  public TINIWebServer()
    throws IOException
  {
     copyBuffer = new byte[1024];
  }
  /** Update the temperature on the web page.
    */
  void  updateTemperature(int temperature, boolean buttonFound)
  {
/*
    if((temperature == prevTemp) && (lastButtonFound == buttonFound))
      return;
*/
    lastButtonFound = buttonFound;
    prevTemp = temperature;

    if(!localPages)
    {
      createPage(temperature, buttonFound);
    }
    else
    {
      createPageLocal(temperature, buttonFound);
    }
  }
  /** Concatenate top and bottom of index page around temperature
    * using local index array info.
    */
  public void createPageLocal(int temp, boolean buttonFound)
  {

    try
    {
      clock.getRTC();
      int hour    = clock.getHour();
      int minute  = clock.getMinute();
      int second  = clock.getSecond();

      String timeString = new String("<BR>Current time " + hour + ":");

      if(minute < 10)
        timeString += "0";

      timeString += Integer.toString(minute);
      timeString += ":";

      if(second < 10)
        timeString += "0";

      timeString += Integer.toString(second);

      synchronized(lock)
      {
        FileOutputStream index = new FileOutputStream(new File(webRoot, 
                                                               webIndex));
        index.write(indexTop.getBytes(), 0, indexTop.length());

        if(buttonFound)
        {
          index.write((Integer.toString(temp)+" F").getBytes());
        }
        else
        {
          index.write("- No DS1920's found".getBytes());
        }

        index.write(timeString.getBytes());
        
        index.write(indexBottom.getBytes(), 0, indexBottom.length());
        index.close();
      }
    }
    catch(Exception e)
    {
      System.out.println("createPageLocal -"+e.toString());
    }
  }
  /** Concatenate top and bottom of index page around temperature.
    */
  public void createPage(int temp, boolean buttonFound)
  {
    try
    {
      File indexPage = new File(webRoot+"indextop.html");

      if(!indexPage.exists())
      {
        localPages = true;
        createPageLocal(temp, buttonFound);
        return;
      }

      clock.getRTC();
      int hour    = clock.getHour();
      int minute  = clock.getMinute();
      int second  = clock.getSecond();

      String timeString = new String("<BR>Current time " + hour + ":");

      if(minute < 10)
        timeString += "0";

      timeString += Integer.toString(minute);
      timeString += ":";

      if(second < 10)
        timeString += "0";

      timeString += Integer.toString(second);

      synchronized(lock)
      {
        FileOutputStream index = new FileOutputStream(new File(webRoot, 
                                                               webIndex));
        FileInputStream   tempFile  = null;
        int bytesRead = (tempFile = new FileInputStream(new File(webRoot,
                                            "indextop.html"))).read(copyBuffer);
        index.write(copyBuffer, 0, bytesRead);
        tempFile.close();

        if(buttonFound)
        {
          index.write((Integer.toString(temp)+" F").getBytes());
        }
        else
        {
          index.write("- No DS1920's found".getBytes());
        }

        index.write(timeString.getBytes());
        
        bytesRead = (tempFile = new FileInputStream(new File(webRoot,
                                         "indexbottom.html"))).read(copyBuffer);
        index.write(copyBuffer, 0, bytesRead);
        tempFile.close();

        index.close();
      }
    }
    catch(Exception e)
    {
      System.out.println("createPage -"+e.toString());
    }
  }
  /** Start the various servers. Then check the temperature and
    * update the web page.
    */
  public void drive()
  {
    try
    {
      // create the web server
      Thread webServer = new Thread(webWorker);
      webServer.setName("Web Server");

      // create the temperature server
      Thread temperatureServer = new Thread(temperatureWorker);
      temperatureServer.setName("Temperature Server");

      webRoot   = webWorker.getWebRoot();
      webIndex  = webWorker.getWebPage();

      createPage(0, false);
      // start the web server
      webServer.start();
      // start the temperature server
      temperatureServer.start();

      // periodically check the temperature and update the web page
      while(true)
      {
        Thread.sleep(5000);
        updateTemperature(temperatureWorker.getCurrentTemperature(),
                          temperatureWorker.buttonFound());
      }
    }
    catch(Throwable t)
    {
      System.out.println("createPageLocal -"+t.toString());
      
      // why kill the server if the exception is not fatal?
      //System.out.println(t);
    }
  }
  /** Create a TINIWebServer.
    */
  public static void main(String[] args)
  {
    System.out.println("Starting TINI WebServer version 2.0 ...");

    try
    {
      int currentArg    = 0;

      TINIWebServer tiniWebServer     = new TINIWebServer();
      tiniWebServer.webWorker         = new WebWorker(tiniWebServer.lock);
      tiniWebServer.temperatureWorker = new TemperatureWorker();

      tiniWebServer.drive();
    }
    catch(Throwable t)
    {
      System.out.println(t);
      System.out.println(t.getMessage());      
    }
    finally
    {
      System.out.println("\nTINIWebserver exiting...");
    }
  }
}
