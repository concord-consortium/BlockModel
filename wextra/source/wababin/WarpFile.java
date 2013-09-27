package wababin;

import java.io.*;

/**
 * A warp file which packages up a number of input files (usually .class and
 * .bmp) into a single file, like a .zip or a .jar but with no compression.
 * This class describes the standard .wrp format but there is a derived class
 * PdbFile which formats the warp file in a form readable to PalmOS devices.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 */
public class WarpFile
{
  /** the extension of this file */
  private static final String WRP_EXT=".wrp";

  /** the file object representing this warp file */
  protected File warpFile;

  /**
   * Constructs a new warp file.
   */
  protected WarpFile()
  {
  }

  /**
   * Constructs a new WarpFile with the given path.
   * @param path the path to this file.
   */
  public WarpFile(String path)
  {
    if (!isValid(path))
      path+=WRP_EXT;
    warpFile=new File(path);
  }

  /**
   * Is this path a valid pdb file (ie does it end with .wrp?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path)
  {
    return path!=null&&path.length()>4&&
      path.substring(path.length()-4).equalsIgnoreCase(WRP_EXT);
  }

  /**
   * Gets the filename of this warp file.
   * @param returns the file.
   */
  public String getFile()
  {
    return warpFile.toString();
  }

  /**
   * Creates this warp file with the given list of input files.
   * @param inputFiles a sorted array of files to add
   */
  public void create(InputFile[] inputFiles)
  {
    try
    {
      if (!Warp.quiet)
        System.out.println("...writing "+warpFile);
      DataOutputStream dos=new DataOutputStream(new FileOutputStream(warpFile));
      writeHeader(dos,inputFiles.length);
      writeFileList(dos,inputFiles);

      for (int i = 0; i < inputFiles.length; i++)
        writeRecord(dos,inputFiles[i]);

      dos.close();
      if (!Warp.quiet)
        System.out.println("...done");
    }
    catch (FileNotFoundException fnfe)
    {
      System.out.println("ERROR: can't create file "+warpFile);
      System.exit(-1);
    }
    catch (IOException ioe)
    {
      System.out.println("ERROR: problem writing to file "+warpFile);
      System.exit(-1);
    }
  }

  /**
   * Writes the header of this file
   * @param dos the output stream to write to
   * @param numInputFiles the number of input files in this warp file
   */
  protected void writeHeader(DataOutputStream dos,int numInputFiles) throws IOException
  {
    dos.writeBytes("Wrp1");
    dos.writeInt(numInputFiles);
  }

  /**
   * Writes the list of files contained in this warp file.  This consists of a list
   * of offsets in the file where each file starts.
   * @param dos the output stream to write to
   * @param inputFile the sorted array of input files.
   */
  protected void writeFileList(DataOutputStream dos,InputFile[] inputFiles) throws IOException
  {
    int recOffset=8+(inputFiles.length + 1) * 4;
    for (int i=0;i<inputFiles.length;i++)
    {
      dos.writeInt(recOffset);
      int pathLen = inputFiles[i].getName().length();
      int size = inputFiles[i].getFileLength();
      recOffset += 2 + pathLen + size;
    }
    dos.writeInt(recOffset);
  }

  /**
   * Writes an individual input file to this warp file.
   * @param dos the output stream to write to
   * @param inputFile the inputFile to write.
   */
  protected void writeRecord(DataOutputStream dos,InputFile inputFile) throws IOException
  {
    String name=inputFile.getName();
    if (!inputFile.exists())
    {
      System.out.println("ERROR: can't load file "+name);
      System.exit(-1);
    }
    if (!Warp.quiet)
      System.out.println("...adding: "+name);
    dos.writeShort(name.length());
    dos.writeBytes(name);
    inputFile.writeFile(dos);
  }

  /**
   * List the contents of this warp file as a list of files and their sizes.
   */
  public void list()
  {
    try
    {
      DataInputStream dis=new DataInputStream(new FileInputStream(warpFile));
      int numFiles=readHeader(dis);
      if (numFiles==-1)
      {
        System.out.println("ERROR: bad magic - file not a warp file "+warpFile);
        System.exit(-1);
      }

      if (!Warp.quiet)
      {
        Warp.copyright();
        System.out.println("file: "+warpFile);
      }
      System.out.println("record count: "+numFiles);
      System.out.println("contents:");
      listFiles(dis,numFiles);
      dis.close();
    }
    catch (FileNotFoundException fnfe)
    {
      System.out.println("ERROR: can't open file "+warpFile);
      System.exit(-1);
    }
    catch (IOException e)
    {
      System.out.println("ERROR: problem reading from file "+warpFile);
      System.exit(-1);
    }
  }

  /**
   * Reads the header of the warp file.
   * @param dis the input stream to read from
   * @returns the number of files in this warp, or -1 if there is an error.
   */
  protected int readHeader(DataInputStream dis) throws IOException
  {
    if (Utils.readString(dis,4).equals("Wrp1"))
      return dis.readInt();
    else
      return -1;
  }

  /**
   * List the files in this warp file by reading the list of offsets and
   * then printing the name and size of each file pointed to.
   * @param dis the input stream to read from
   * @param numFiles the number of files to read
   */
  protected void listFiles(DataInputStream dis,int numFiles) throws IOException
  {
    int[] offsets=new int[numFiles+1];
    for(int i = 0; i < numFiles+1; i++)
      offsets[i]=dis.readInt();
    for(int i=0;i<numFiles;i++)
    {
      short pathLen=dis.readShort();
      String name=Utils.readString(dis,pathLen);
      int size=offsets[i+1]-offsets[i]-pathLen-2;
      System.out.println("  "+name+" ("+size+")");
      dis.skipBytes(size);
    }
    dis.close();
  }
}