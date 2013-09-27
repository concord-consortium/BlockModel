package wababin;

/**
 * Generates warp files for PalmOS and WinCE which contain .class and
 * .bmp files that make up an application.
 * Is equivalent to the waba warp.exe program.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 */
public class Warp
{
  /** the create warp file command */
  public static final int CMD_CREATE=1;

  /** the list warp file command */
  public static final int CMD_LIST=2;

  /** should we suppress non error messages? */
  public static boolean quiet;

  /**
   * The main application
   */
  public static void main(String[] args)
  {
    if (args.length < 2)
      usage();

    int cmd = 0;
    switch (args[0].charAt(0))
    {
      case 'c':
      case 'C':
        cmd = CMD_CREATE;
        break;
      case 'l':
      case 'L':
        cmd = CMD_LIST;
        break;
      default:
        System.out.println("ERROR: no command specified");
        System.exit(-1);
    }

    // parse command line options
    boolean recurseDirs=false;
    String creator=null;
    quiet=false;
    int i;
    for (i=1;i<args.length;i++)
    {
      String arg = args[i];
      if (arg.charAt(0) != '/' || arg.length() < 2)
        break;
      switch(arg.charAt(1))
      {
        case '?':
          usage();
          break;
        case 'r':
        case 'R':
          recurseDirs=true;
          break;
        case 'c':
        case 'C':
          if (++i == args.length)
          {
            System.out.println("ERROR: no creator specified");
            System.exit(-1);
          }
          arg = args[i];
          if (arg.length() != 4)
          {
            System.out.println("ERROR: creator must be 4 charaters");
            System.exit(-1);
          }
          creator = arg;
          break;
        case 'q':
        case 'Q':
          quiet = true;
          break;
        default:
          System.out.println("ERROR: unknown option "+arg);
          System.exit(-1);
        }
      }
    if (i == args.length)
    {
      System.out.println("ERROR: no warp file specified");
      System.exit(-1);
    }
    String warpFile = args[i];
    String warpExt;
    if (cmd == CMD_CREATE && (warpExt=Utils.checkForExtension(warpFile))!=null)
    {
      System.out.println("ERROR: when creating, don't specify an extension such as "+warpExt);
      System.exit(-1);
    }

    i++;

    if (cmd == CMD_CREATE)
    {
      if (!quiet)
        copyright();
      // generate input file list - expand wildcards and get full paths
      InputFile[] inputFiles;
      if (i==args.length)
        inputFiles=InputFile.expandFiles(new String[]{warpFile+".class"},0,1,false);
      else
        inputFiles=InputFile.expandFiles(args,i,args.length-i,recurseDirs);

      if (inputFiles.length == 0)
      {
        System.out.println("ERROR: no input files specified");
        System.exit(-1);
      }
      
      PdbFile pdb=new PdbFile(warpFile,creator);
      WarpFile wrp=new WarpFile(warpFile);
      if (!quiet)
      {
        System.out.println("warp files: "+pdb.getFile()+" "+wrp.getFile());
        System.out.println("PalmOS PDB name: "+pdb.getName());
        System.out.println("PalmOS PDB creator: "+pdb.getCreator());
        System.out.println("PalmOS PDB version: "+pdb.getVersion());
      }
      // sort path names
      sortInputFiles(inputFiles);

      pdb.create(inputFiles);
      wrp.create(inputFiles);
    }
    else if (cmd == CMD_LIST)
    {
      WarpFile wf=null;
      if (PdbFile.isValid(warpFile))
        wf=new PdbFile(warpFile);
      else
      if (WarpFile.isValid(warpFile))
        wf=new WarpFile(warpFile);
      else
      {
        System.out.println("ERROR: file does not have a warp file extension");
        System.exit(-1);
      }
      wf.list();
    }
  }

  /**
   * Sorts the input files into alphabetic order.  I've just used a bubble sort
   * here as the number of files is likely to be small and I can't be bothered
   * remembering how to do a quicksort :)
   * @param inputFiles the unsorted array of files which are to be sorted.
   */
  public static void sortInputFiles(InputFile[] inputFiles)
  {
    boolean changed=true;
    int j=inputFiles.length-1;
    while(changed)
    {
      changed=false;
      for(int i=0;i<j;i++)
      {
        if (inputFiles[i].compareTo(inputFiles[i+1])>0)
        {
          InputFile temp=inputFiles[i];
          inputFiles[i]=inputFiles[i+1];
          inputFiles[i+1]=temp;
          changed=true;;
        }
      }
      j--;
    }
  }

  /**
   * Print the copyright notice
   */
  public static void copyright()
  {
    System.out.println("Waba Application Resource Packager for Java  Version 1.50.0");
    System.out.println("Copyright (C) Rob Nielsen 1999. All rights reserved");
    System.out.println();
  }

  /**
   * Print usage information and quit
   */
  private static void usage()
  {
    copyright();
    System.out.println("Usage: java wababin.Warp command [options] warpfile [files]");
    System.out.println();
    System.out.println("Commands:");
    System.out.println("   c   Create new warp file");
    System.out.println("   l   List contents of a warp file");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  /?   Displays usage text");
    System.out.println("  /c   Override and assign PDB database creator (e.g. /c CrTr)");
    System.out.println("  /r   If a directory is specified in the files, recurse any subdirs");
    System.out.println("  /q   Quiet mode (no output except for errors)");
    System.out.println();
    System.out.println("This program creates both a WindowsCE .wrp warp file and a PalmOS .pdb");
    System.out.println("warp file. For PalmOS, a PDB database name and PDB creator will be");
    System.out.println("generated automatically from the name of the warp file.");
    System.out.println();
    System.out.println("Warp will automatically check any class files for dependencies and add");
    System.out.println("these files so you will only need to specify the main class file and");
    System.out.println("everything else will be added automatically, even directly referenced");
    System.out.println(".bmp files. (ie. Image im=new Image(\"rob.bmp\"); )");
    System.out.println("If no input files are specified, it will look for a .class file with");
    System.out.println("the same name of the warp file you are creating.  ");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("   java wababin.Warp c helloApp");
    System.out.println("   java wababin.Warp c helloApp *.class util\\*.class");
    System.out.println("   java wababin.Warp c helloApp *.class extra\\");
    System.out.println("   java wababin.Warp l helloApp.wrp");
    System.out.println("   java wababin.Warp l helloApp.pdb");
    System.exit(0);
  }
}
