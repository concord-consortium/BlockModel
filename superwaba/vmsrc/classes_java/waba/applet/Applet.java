package waba.applet;

/*
Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

/*
 * Note: Everything that calls waba code in these classes must be
 * synchronized with respect to the Applet uiLock object to allow waba
 * programs to be single threaded. This is because of the multi-threaded
 * nature of Java and because timers use multiple threads.
 *
 * Because all calls into waba are synchronized and users can't call this code,
 * they can't deadlock the program in any way. If we moved the synchronization
 * into waba code, we would have the possibility of deadlock.
 */

import waba.ui.*;
import waba.sys.Settings;

public class Applet extends java.applet.Applet
{
String className;
Frame frame = null;
public MainWindow mainWindow = null;
public boolean isApplication = false;
public boolean isColor = false;
public int width;
public int height;
public String appPath = ""; // guich@120

public static Object uiLock = new Object();
public static Applet currentApplet;
boolean started; // guich@120

public void destroy()
   {
   if (mainWindow == null)
      return;
   mainWindow._stopTimer();
   synchronized(Applet.uiLock)
      {
      mainWindow.onExit();
      }
   }
public void init()
   {
   try
   {
      // print instructions
      System.out.println("Key emulations:");
      System.out.println("F1-F4 : HARD1 to HARD4");
		System.out.println("F5 : COMMAND");
		System.out.println("F6 : MENU");
		System.out.println("F7 : CALC");
		System.out.println("F8 : FIND");
		System.out.println("F11: KEYBOARD_ABC");
		System.out.println("F12: KEYBOARD_123");
      
      // dummy in values I haven't found out how to get yet
      Settings.dateFormat = Settings.DATE_DMY;
      Settings.dateSeparator = '/';
      Settings.is24Hour = true;
      Settings.timeSeparator = ':';
      Settings.screenWidth = 160;
      Settings.screenHeight = 160;
         
      java.util.Calendar cal = java.util.Calendar.getInstance();
      Settings.weekStart = (byte) (cal.getFirstDayOfWeek() - 1);

      java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
      Settings.thousandsSeparator = dfs.getGroupingSeparator();
      Settings.decimalSeparator = dfs.getDecimalSeparator();

      setLayout(new java.awt.BorderLayout());
      // NOTE: getParameter() and size() don't function in a
      // java applet constructor, so we need to call them here
      if (!isApplication)
         {
         className = getParameter("appClass");
         if (className == null) throw new Exception("please use a parameter \"appClass\" specifying the class name"); // guich@120
         appPath = getParameter("appPath"); // guich@120
         if (appPath != null && !appPath.endsWith("/")) appPath += "/"; // guich@120
         if (appPath == null) appPath = ""; // guich@120
         
         width = size().width;
         height = size().height;
         Settings.screenWidth = width; // guich@120
         Settings.screenHeight = height; // guich@120
         }
      currentApplet  = this;
      try
         {
         Class c = Class.forName(className);
         mainWindow = (MainWindow)c.newInstance();
         }
      catch (Exception e) { e.printStackTrace(); }
      // NOTE: java will call a partially constructed object
      // if show() is called before all the objects are constructed
      if (frame != null) frame.show(); 
      repaint();
   } catch (Exception ee) {ee.printStackTrace();} // guich@120
   }

void startApp()
{
   if (!started) // guich@120 - make sure that the canvas is available for drawing when starting the application. called by WinCanvas.paint.
   {
      try
      {
         synchronized(Applet.uiLock)
         {
            mainWindow.onStart();
         }
      } catch (Throwable e) {e.printStackTrace();}
      started = true;
   }
}
public static void main(String args[])
   {
   boolean isColor = false;
   int width = 160;
   int height = 160;
   int count = args.length;
   if (count == 0)
      {
      System.out.println("ERROR: you must supply a class name");
      return;
      }
   for (int i = 0; i < count - 1; i++)
      {
      if (args[i].equals("/w"))
         {
         if (++i < count - 1)
            try { width = Integer.parseInt(args[i]); }
            catch (Exception e)
               {
               System.out.println("ERROR: bad width");
               }
         }
      else if (args[i].equals("/h"))
         {
         if (++i < count - 1)
            try { height = Integer.parseInt(args[i]); }
            catch (Exception e)
               {
               System.out.println("ERROR: bad height");
               }
         }
      else if (args[i].equals("/color"))
         isColor = true;
      }
   Applet applet = new Applet();
   applet.className = args[count - 1];
   applet.isApplication = true;
   applet.isColor = isColor;
   applet.width = width;
   applet.height = height;
   Frame frame = new Frame();
   frame.setLayout(new java.awt.BorderLayout());
   frame.add("Center", applet);
   frame.resize(width, height);
   frame.show();
   applet.frame = frame;
   // NOTE: java requires us to do this to make sure things paint
   frame.hide();
   java.awt.Insets insets;
   try
      {
      insets = frame.getInsets();
      }
   catch (NoSuchMethodError e)
      {
      insets = frame.insets(); // this is the JDK 1.02 call to get insets
      }
   if (insets == null)
      insets = new java.awt.Insets(0, 0, 0, 0);
   frame.resize(width + insets.left + insets.right,
      height + insets.top + insets.bottom);
   applet.init();
   }
public void start()
   {
   currentApplet = this;
   mainWindow = MainWindow.getMainWindow();
   }
}