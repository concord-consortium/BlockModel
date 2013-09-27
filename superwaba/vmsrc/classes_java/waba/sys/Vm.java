package waba.sys;

/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

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

import waba.applet.Applet;

/**
 * Vm contains various system level methods.
 * <p>
 * This class contains methods to copy arrays, obtain a timestamp,
 * sleep and get platform and version information.
 */

// NOTE:
// In the future, these methods may include getting unique object id's,
// getting object classes, sleep (for single threaded apps),
// getting amount of memory used/free, etc.
// The reason these methods should appear in this class and not somewhere
// like the Object class is because each method added to the Object class
// adds one more method to every object in the system.
import java.awt.datatransfer.*;
import java.awt.Toolkit;
public class Vm
{
	private static ClipboardOwner defaultClipboardOwner = new ClipboardObserver();

	static class ClipboardObserver implements ClipboardOwner {

		public void lostOwnership(Clipboard clipboard, Transferable contents) {
		}
	}
private Vm()
	{
	}
/** copies the specific string to the clipboard */
public static void clipboardCopy(String s) {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	StringSelection contents = new StringSelection(s);
	clipboard.setContents(contents, defaultClipboardOwner);
}
/** gets the last string from the clipboard. if none, returns "". */
public static String clipboardPaste() {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable content = clipboard.getContents(defaultClipboardOwner);
	if (content != null) {
		try {
			String dstData = (String) (content.getTransferData(DataFlavor.stringFlavor));
			return dstData;
		} catch (Exception e) {
			return "";
		}
	}
	return "";
}
/**
 * Copies the elements of one array to another array. This method returns
 * true if the copy is successful. It will return false if the array types
 * are not compatible or if either array is null. If the length parameter
 * would cause the copy to read or write past the end of one of the arrays,
 * an index out of range error will occur. If false is returned then no
 * copying has been performed.
 * @param srcArray the array to copy elements from
 * @param srcStart the starting position in the source array
 * @param dstArray the array to copy elements to
 * @param dstStart the starting position in the destination array
 * @param length the number of elements to copy
 */

public static boolean copyArray(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length)
	{
	if (length < 0)
		return false;
	try
		{
		System.arraycopy(srcArray, srcStart, dstArray, dstStart, length);
		}
	catch (Exception e)
		{
		return false;
		}
	return true;
	}
/** draws a text in the window, erasing all the line; increments y automatically and starts over when reach the bottom of screen. use only to debug. 
ps: you can delimit the area where this function draws using the method setTrace(false,0,minY,lines). See setTrace for details.
added by guich in 1.02 */
public static void debug(String s, int wait) {
	System.out.println(s);
	if (wait > 0) sleep(wait);
}
/**
 * Executes a command.
 * <p>
 * As an example, the following call could be used to run the command
 * "scandir /p mydir" under Java, Win32 or WinCE:
 * <pre>
 * int result = Vm.exec("scandir", "/p mydir", 0, true);
 * </pre>
 * This example executes the Scribble program under PalmOS:
 * <pre>
 * Vm.exec("Scribble", null, 0, false);
 * </pre>
 * This example executes the web clipper program under PalmOS, telling
 * it to display a web page by using launchCode 54 (CmdGoToURL).
 * <pre>
 * Vm.exec("Clipper", "http://www.yahoo.com", 54, true);
 * </pre>
 * The args parameter passed to this method is the arguments string
 * to pass to the program being executed.
 * <p>
 * The launchCode parameter is only used under PalmOS. Under PalmOS, it is
 * the launch code value to use when the Vm calls SysUIAppSwitch().
 * If 0 is passed, the default launch code (CmdNormalLaunch) is used to
 * execute the program.
 * <p>
 * The wait parameter passed to this method determines whether to execute
 * the command asynchronously. If false, then the method will return without
 * waiting for the command to complete execution. If true, the method will
 * wait for the program to finish executing and the return value of the
 * method will be the value returned from the application under Java, Win32
 * and WinCE.
 * <p>
 * Under PalmOS, the wait parameter is ignored since executing another
 * program terminates the running program.
 * 
 * Notes added by guich@120:
 * To make a portable exec between appletviewer and Palm OS, please do like the following: <pre>
 *    if (Vm.getPlatform().equalsIgnoreCase("Java"))
 *        Vm.exec("versapalm.finance.VPFinanceSetup",null,-1,false);
 *     else
 *        Vm.exec("Finance Setup",null,0,false);  
 * </pre>
 * Also note that, if you call <i>exit</i> after calling <i>exec</i>, the program called 
 * will be terminated, since both are running in the same VM.
 * @param command the command to execute
 * @param args command arguments
 * @param launchCode launch code for PalmOS applications
 * @param wait whether to wait for the command to complete execution before returning 
 */

public static int exec(String command, String args, int launchCode, boolean wait)
	{
      int status = -1;
	   if (launchCode == -1) // guich@120
	   {
         try
         {
            // guich@120: the ideal were that all classes should be re-instantiated, because any static methods that used the last MainWindow are now pointing to invalid data.
            Class c = Class.forName(command);
            Applet.currentApplet.mainWindow.destroy();
            Applet.currentApplet.mainWindow = (waba.ui.MainWindow)c.newInstance();
            Applet.currentApplet.mainWindow.onStart(); // ps: since we are being called from an app, we cannot use the synchronized method
            status = 0;
         }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
      {
	      java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
	      try	{
		      java.lang.Process p = runtime.exec(command + " " + args);
		      if (wait)
			      status = p.waitFor();
		      else
			      status = 0;
		      }
      	catch (Exception e) {}
      }
	return status;
	}
/** returns the free class heap. calls GC before. added by guich */
public static int getFreeClassHeap() {
	return 99999;
}
/** returns the free object heap. dont call GC. added by guich. */
public static int getFreeObjectHeap() {
	return 99999;
}
/** Returns the platform the Virtual Machine is running under as a string. */

public static String getPlatform()
	{
	return "Java";
	}
/** returns the ROM version, like 0x02000000 or 0x03010000. added by guich */
public static int getRomVersion() {
	return 0;
}
/**
 * Returns a time stamp in milliseconds. The time stamp is the time
 * in milliseconds since some arbitrary starting time fixed when
 * the VM starts executing. The maximum time stamp value is (1 << 30) and
 * when it is reached, the timer will reset to 0 and will continue counting
 * from there.
 */

public static int getTimeStamp()
	{
	return (int)(System.currentTimeMillis() % (1 << 30));
	}
/** 
 * Returns the username of the user running the Virutal Machine. Because of
 * Java's security model, this method will return null when called in a Java
 * applet. This method will also return null under most WinCE devices (that
 * will be fixed in a future release).
 */

public static String getUserName()
	{
	if (!Applet.currentApplet.isApplication)
		return null;
	return java.lang.System.getProperty("user.name");
	}
/**
 * Returns the version of the Waba Virtual Machine. The major version is
 * base 100. For example, version 1.0 has value 100. Version 2.0 has a
 * version value of 200. A beta 0.8 VM will have version 80.
 * ps: Waba 1.0G will return 1.01. SuperWaba = 110 (1.1)
 */
public static int getVersion()
	{
	return 120;
	}
/**
 * Returns true if the system supports a color display and false otherwise.
 */

public static boolean isColor()
	{
	return Applet.currentApplet.isColor;
	}
/** returns a random number. added by guich
   @param seed if > 0, sets the seed, if = 0, uses the last seed
   @returns a 32 bits random number generated. since the original function returns a 16 bits, i create an 32 bits asking for 2 consecutives 16 bits numbers. */
// guich@120: corrected so it uses the last rand if seed == 0.
private static java.util.Random lastRand;
public static int random(int seed) 
{
	lastRand = (lastRand != null && seed == 0)?lastRand:new java.util.Random(seed);
	return lastRand.nextInt();
}
/**
 * Sets the device's "auto-off" time. This is the time in seconds where, if no
 * user interaction occurs with the device, it turns off. To keep the device always
 * on, pass 0. This method only works under PalmOS. The integer returned is
 * the previous auto-off time in seconds.
 */

public static int setDeviceAutoOff(int seconds)
	{
	return 0;
	}
/** if true, intercepts the "hard" system keys: calc, find and the 4 hard keys. added by guich */
public static void setSystemKeysUse(boolean intercept) {
}
/** prints the current class and method that is being called by the vm. only useful for debug. 
the contents are printed in screen, erasing everything. 
Example: setTrace(true,500,100,2) - sets the trace on, with 1/2 second of delay, and will draw the steps from 100 to 100+10*2 screen coordinates. guich@102
@param on true turns the trace on
@param delay delay between each step. Carefull!
@param minY starting row for printing chars.
@param lines number of lines that will be used to overwrite the screen. */
public static void setTrace(boolean on, int delay, int minY, int lines) {
	Thread.dumpStack();
}
/**
 * Causes the VM to pause execution for the given number of milliseconds.
 * @param millis time to sleep in milliseconds
 */

public static void sleep(int millis)
	{
	try
		{
		Thread.currentThread().sleep(millis);
		}
	catch (Exception e) {}
	}

/** return true is the string is valid. */
// added by guich@120
public static boolean isOk(String s)
{
   return s != null && s.length() > 0;
}

/** used in some classes so they can correctly open files. */
// added by guich@120
public static java.io.InputStream openInputStream(String path)
{
	boolean isApp = Applet.currentApplet.isApplication;
	java.io.InputStream stream = null;
	try 
	{	   
	   if (isApp)
		   stream = new java.io.FileInputStream(isOk(Applet.currentApplet.appPath)?(Applet.currentApplet.appPath+path):path); 
	   else
	   {
		   java.net.URL url;			   
		   if (!isOk(Applet.currentApplet.appPath))
		   {
			   java.net.URL codeBase = Applet.currentApplet.getCodeBase();
			   String cb = codeBase.toString();
			   char lastc = cb.charAt(cb.length() - 1);
			   char firstc = path.charAt(0);
			   if (lastc != '/' && firstc != '/')
				   cb += "/";
			   url = new java.net.URL(cb + path);
		   } 
		   else url = new java.net.URL("file://localhost/"+Applet.currentApplet.appPath + path); // guich@120

   	   stream = url.openStream();
	   }
   }
   catch (Exception e) {System.out.println("error in Vm.openInputStream:"+e.getClass()+" "+e.getMessage());}; // guich@120
	return stream;
}
/** used in some classes so they can correctly open files. */
// added by guich@120
public static java.io.OutputStream openOutputStream(String path)
{
	boolean isApp = Applet.currentApplet.isApplication;
	java.io.OutputStream stream = null;
	try 
	{	   
	   if (isApp || isOk(Applet.currentApplet.appPath)) // output isnt supported by protocol file://
		   stream = new java.io.FileOutputStream(isOk(Applet.currentApplet.appPath)?(Applet.currentApplet.appPath+path):path); 
	   else
	   {
	      try
	      {
		      java.net.URL url;			   
			   java.net.URL codeBase = Applet.currentApplet.getCodeBase();
			   String cb = codeBase.toString();
			   char lastc = cb.charAt(cb.length() - 1);
			   char firstc = path.charAt(0);
			   if (lastc != '/' && firstc != '/')
				   cb += "/";
			   url = new java.net.URL(cb + path);

            java.net.URLConnection con = url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(false);
            stream = con.getOutputStream();
         } 
         catch (java.net.UnknownServiceException u) // try another way
         {
            stream = new java.io.FileOutputStream(isOk(Applet.currentApplet.appPath)?(Applet.currentApplet.appPath+path):path); 
         }
	   }
   }
   catch (Throwable e) {System.out.println("error in Vm.openOutputStream:"+e.getClass()+" "+e.getMessage());}; // guich@120
	return stream;
}
}