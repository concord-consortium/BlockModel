package wababin;

import java.io.*;

/**
 * Generates launcher apps for PalmOS and WinCE for launching Waba programs.
 * Is equivalent to the waba exegen.exe program.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 */
public class Exegen
{
  /** the path to the waba application on WinCE devices */
  private static final String ceWabaPath = "\\Program Files\\waba\\waba.exe";

  // defaults for memory allocation
  private static final int DEFAULT_CLASS_HEAP_SIZE=14000;
  private static final int DEFAULT_OBJECT_HEAP_SIZE=8000;
  private static final int DEFAULT_STACK_SIZE=1500;
  private static final int DEFAULT_NATIVE_STACK_SIZE=300;

  /** should we suppress non error messages? */
  public static boolean quiet=false;

  /**
   * The main application
   */
  public static void main(String[] args)
  {
    if (args.length < 2)
      usage();

    String prcIcon=null;
    String prcCreator=null;
    String ceWarpDir=null;
    int defWidth = 0;
    int defHeight = 0;
    int classHeapSize = DEFAULT_CLASS_HEAP_SIZE;
    int objectHeapSize = DEFAULT_OBJECT_HEAP_SIZE;
    int stackSize = DEFAULT_STACK_SIZE;
    int nativeStackSize = DEFAULT_NATIVE_STACK_SIZE;

    // parse options
    int i;
    for (i = 0; i < args.length; i++)
    {
      String arg = args[i];
      if (arg.charAt(0) != '/' || arg.length() < 2)
        break;
      switch(arg.charAt(1))
      {
        case '?':
          usage();
          break;
        case 'q':
        case 'Q':
          quiet = true;
          break;
        case 'c':
        case 'C':
          if (++i == args.length)
          {
            System.out.println("ERROR: no creator id specified");
            System.exit(-1);
          }
          arg = args[i];
          prcCreator=arg;
          break;
        case 'i':
        case 'I':
          if (++i == args.length)
          {
            System.out.println("ERROR: no icon file specified");
            System.exit(-1);
          }
          arg = args[i];
          prcIcon = arg;
          break;
        case 'p':
        case 'P':
          if (++i == args.length)
          {
            System.out.println("ERROR: no directory specified for /p option");
            System.exit(-1);
          }
          ceWarpDir = args[i];
          break;
        case 'w':
        case 'W':
        case 'h':
        case 'H':
        case 'l':
        case 'L':
        case 'm':
        case 'M':
        case 's':
        case 'S':
        case 't':
        case 'T':
          if (++i == args.length)
          {
            System.out.println("ERROR: bad #");
            System.exit(-1);
          }
          char c = arg.charAt(1);
          arg = args[i];
          int n=0;
          try
          {
            n= Integer.parseInt(arg);
          }
          catch(NumberFormatException e)
          {
            System.out.println("ERROR: bad #");
            System.exit(-1);
          }
          if (c == 'l' || c == 'L') classHeapSize = n;
          else if (c == 'm' || c == 'M') objectHeapSize = n;
          else if (c == 's' || c == 'S') stackSize = n;
          else if (c == 't' || c == 'T') nativeStackSize = n;
          else if (c == 'w' || c == 'W') defWidth = n;
          else if (c == 'h' || c == 'H') defHeight = n;
          break;
        default:
          System.out.println("ERROR: unknown option "+args[i]);
          System.exit(-1);
      }
    }
    if (i == args.length)
    {
      System.out.println("ERROR: no output file specified");
      System.exit(-1);
    }
    String exefile = args[i++];
    String prcName=Utils.strip(exefile);
    if (ceWarpDir == null)
      ceWarpDir="\\Program Files\\waba";

    String prcPath=exefile+".prc";
    String lnkPath=exefile+".lnk";

    if (i == args.length)
      {
      System.out.println("ERROR: no main window class specified");
      System.exit(-1);
      }

    // convert . to / in className to make it a class path
    String className = args[i++];

    if (className.endsWith(".class"))
      className=className.substring(0,className.length()-6);

    className=className.replace('.','/');

    if (i == args.length)
    {
      System.out.println("ERROR: no warp file specified");
      System.exit(-1);
    }
    String warpFile = args[i++];

    String warpExt=Utils.checkForExtension(warpFile);
    if (warpExt!=null)
    {
      System.out.println("ERROR: warp files should be specified without extensions such as "+warpExt);
      System.exit(-1);
    }
    String warpName=Utils.strip(warpFile);

    if (prcCreator==null)
      prcCreator=(new PdbFile(warpFile)).getCreator();

    if (i != args.length)
    {
      System.out.println("ERROR: extra arguments found at end of command");
      System.out.println(args[i]);
      System.exit(-1);
    }

    if (!quiet)
    {
      copyright();

      System.out.println("output files: "+prcPath+" "+lnkPath);
      System.out.println("class name: "+className);
      System.out.println("PalmOS PRC name: "+prcName);
      System.out.println("PalmOS PRC creator: "+prcCreator);
      System.out.print("PalmOS PRC icon: ");
      if (prcIcon!=null)
        System.out.println(prcIcon);
      else
        System.out.println("<default>");
      System.out.println("WindowsCE warp directory: "+ceWarpDir);
      System.out.println("class heap size: "+classHeapSize);
      System.out.println("object heap size: "+objectHeapSize);
      System.out.println("native stack size: "+nativeStackSize);
      System.out.println("stack size: "+stackSize);
    }

    // generate prc file
    PrcFile prc=new PrcFile(prcPath);

    prc.create(prcName,prcCreator,classHeapSize,objectHeapSize,stackSize,nativeStackSize,className,prcIcon);

    // write out lnk file
    try
    {
      if (!quiet)
        System.out.println("...writing "+lnkPath);

      DataOutputStream dos=new DataOutputStream(new FileOutputStream(lnkPath));
      String widthHeight = "";
      if(defWidth != 0){
	  widthHeight += " /w " + defWidth;
      } 
      if(defHeight != 0){
	  widthHeight += " /h " + defHeight;
      }
      String lnk="\""+ceWabaPath+"\"" + widthHeight + " /l "+classHeapSize+
        " /m "+objectHeapSize+" /s "+stackSize+ " /t "+nativeStackSize+" "+className+" \""+
        ceWarpDir+"\\"+warpName+".wrp\"";
      // NOTE: the format of a CE .lnk shortcut file is:
      //
      // <path len>#<path>
      //
      // on one line with no spaces
      dos.writeBytes(lnk.length()+"#"+lnk);
    }
    catch(FileNotFoundException fnfe)
    {
      System.out.println("ERROR: can't open output file");
      System.exit(-5);
    }
    catch(IOException ioe)
    {
      System.out.println("ERROR: can't write to output file");
      System.exit(-5);
    }
    if (!quiet)
      System.out.println("...done");
  }

  /**
   * Print the copyright notice
   */
  private static void copyright()
  {
    System.out.println("Waba Launch Executable Generator for Java  Version 1.40.0");
    System.out.println("Copyright (C) Rob Nielsen 1999. All rights reserved");
    System.out.println("");
  }

  /**
   * Print usage information and quit
   */
  private static void usage()
  {
    copyright();
    System.out.println("Usage: java Exegen [options] exefile main-window-class warpfile");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  /?   Displays usage text");
    System.out.println("  /c   Override and assign PDC creator (e.g. /c CrTr)");
    System.out.println("  /h   Assign height of application's main window");
    System.out.println("  /i   Assign PalmOS PRC icon (e.g. /i sample.bmp)");
    System.out.println("  /l   Assign size of class heap (e.g. /l 10000)");
    System.out.println("  /m   Assign size of object heap (e.g. /m 20000)");
    System.out.println("  /p   Full path to directory containing warp file under WindowsCE");
    System.out.println("  /s   Assign size of stack (e.g. /s 2000)");
    System.out.println("  /t   Assign size of native stack (e.g. /t 50)");
    System.out.println("  /w   Assign width of application's main window");
    System.out.println();
    System.out.println("This program generates a WindowsCE application shortcut .lnk file and");
    System.out.println("a PalmOS .prc application. These files are used to launch (start up) a");
    System.out.println("Waba program.");
    System.out.println();
    System.out.println("File extensions are generated automatically. For example, if you specify");
    System.out.println("myapp as the exefile, a myapp.lnk and myapp.prc will be created.");
    System.out.println();
    System.out.println("The /w and /h parameters define the default width and height of the");
    System.out.println("application's window. The value of 0 for either will cause the main window");
    System.out.println("to appear at a default size which is different on each platform.");
    System.out.println();
    System.out.println("The /p parameter defines the full path to the directory which will contain");
    System.out.println("the warp file under WindowsCE. This path is placed in the shortcut (.lnk)");
    System.out.println("file so the application will know where to find it's warp file.");
    System.out.println();
    System.out.println("For PalmOS, if no icon is defined, a black box is used. Any icon given must");
    System.out.println("be in .bmp format. A PalmOS PRC creator and PRC name will be assigned based");
    System.out.println("on the warpfile and exefile respectively. The exefile must be 30 characters");
    System.out.println("or less.");
    System.out.println();
    System.out.println("The sizes specified are used by the WabaVM to determine how much memory");
    System.out.println("to allocate for the app. The size of the class heap defaults to "+(DEFAULT_CLASS_HEAP_SIZE / 1000)+"K");
    System.out.println("The size of the object heap defaults to "+(DEFAULT_OBJECT_HEAP_SIZE / 1000)+"K. The size of the stack");
    System.out.println("defaults to "+DEFAULT_STACK_SIZE+" bytes. The size of the native stack defaults to "+DEFAULT_NATIVE_STACK_SIZE+" bytes.");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("java Exegen /i s.bmp /p \"\\Program Files\\Scribble\" Scribble ScribbleApp scribble");
    System.out.println("java Exegen /w 160 /h 160 /m 20000 Calc CalcWindow calc");
    System.exit(0);
  }
}
