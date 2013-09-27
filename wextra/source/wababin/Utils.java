package wababin;

import java.io.*;

/**
 * Some general utility methods used by the Warp and Exegen programs.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 */
public class Utils
{
  /**
   * A convenience method for reading a fixed length string from an
   * input stream.
   * @param is the input stream to read from
   * @param len the length of the string
   * @returns the read string
   */
  public static String readString(InputStream is,int len) throws IOException
  {
    byte[] b=new byte[len];
    int rd=0;
    while (rd<len)
      rd+=is.read(b,rd,len-rd);
    return new String(b);
  }

  /**
   * A method for reading an integer from an input stream when it has
   * been saved as low byte first as opposed to high byte first as used
   * by DataInputStream.
   * @param in the stream to read from.
   * @returns the int that has been read from the stream
   */
  public static int readReverseInt(InputStream in) throws IOException
  {
    int ch1 = in.read();
    int ch2 = in.read();
    int ch3 = in.read();
    int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
         throw new EOFException();
    return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
  }

  /**
   * A method for reading an short from an input stream when it has
   * been saved as low byte first as opposed to high byte first as used
   * by DataInputStream.
   * @param in the stream to read from.
   * @returns the short that has been read from the stream
   */
  public static short readReverseShort(InputStream in) throws IOException
  {
    int ch1 = in.read();
    int ch2 = in.read();
    if ((ch1 | ch2) < 0)
         throw new EOFException();
    return (short)((ch2 << 8) + (ch1 << 0));
  }

  /**
   * Strips any path components or extensions from a filename to get the
   * base name.  eg <code>strip("one\two\Three.ext")</code> would return
   * <code>"Three"</code>
   * @param s the string to strip
   */
  public static String strip(String s)
  {
    int st=s.lastIndexOf('\\');
    if (st==-1)
      st=s.lastIndexOf('/');
    if (st==-1)
      st=0;
    if (s.charAt(st)=='\"')
      st++;
    int en=s.lastIndexOf(".");
    if (en==-1||en<=st)
      en=s.length();
    if (s.charAt(en-1)=='\"')
      en--;
    return s.substring(st,en);
  }

  /**
   * Checks if the given path has a file extension.
   * @param s the path to check
   * @returns the extension, including the '.' or null if none.
   */
  public static String checkForExtension(String s)
  {
    int dotInd=s.lastIndexOf('.');
    if (dotInd==-1)
      return null;
    int slashInd=s.lastIndexOf('\\');
    if (slashInd==-1)
      slashInd=s.lastIndexOf('/');
    if (slashInd==-1||slashInd<dotInd)
      return s.substring(dotInd);
    return null;
  }
}
