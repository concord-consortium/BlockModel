/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 23/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 ****************************************************************************/



package extra.io;



import waba.io.*;

import waba.util.*;

import waba.sys.Vm;

/**

 * BufferStream is actually several classes in one.  It can serve as a

 * buffer to another stream, packaging up several read or write operations

 * into a single one to make them more efficient.  It can act as a sink,

 * collecting any number of writes, returning the result as a byte array.

 * It can do offstream processing, taking a byte array that may have been

 * read from another stream and treating it as a stream itself, or filling

 * in an array so it can be sent off on another stream.  It is of most use

 * when used in conjunction with DataStream.  Here's some examples:<p>

 * Here we write a new record to a Catalog and read it back in.

 * <pre>

 *   BufferStream outBuffer=new BufferStream();

 *   DataStream ds=new DataStream(outBuffer);

 *   ds.writeInt(12);

 *   ds.writeString("Hello");

 *   ds.writeFloat(3.14);

 *   byte[] buf=bs.getBuffer();

 *

 *   Catalog c=new Catalog("crtr.TYPE",Catalog.WRITE_ONLY);

 *   c.addRecord(buf.length);

 *   c.writeBytes(buf,0,buf.length);

 *   c.close();

 *

 *   ...

 *

 *   Catalog c=new Catalog("crtr.TYPE",Catalog.READ_ONLY);

 *   c.setRecordPos(c.getRecordCount()-1);

 *   int size=c.getRecordSize();

 *   byte[] b=new byte[size];

 *   c.readBytes(buf,0,buf.length);

 *   c.close();

 *

 *   BufferStream inBuffer=new BufferStream(buf);

 *   DataStream ds=new DataStream(inBuffer);

 *   int i=ds.readInt();       // 12

 *   String s=ds.readString(); // Hello

 *   float f=ds.readFloat();   // 3.14

 * </pre>

 * <p>

 * In this example we are buffering the output to a SerialPort.  In this

 * case any data we write to the BufferStream is not sent to the

 * SerialPort until the buffer is full or flush() is called.  In this case

 * the input is still being handled through the SerialPort but the same

 * bufferPort object could be used to read too.  One point to note if you

 * do it this way is that when it's buffer is empty, the BufferStream will

 * attempt to read as many bytes as will fit in it's buffer, regardless of

 * how many are asked for.  This could cause problems if you want to access

 * the underlying stream without using the BufferStream.

 * <pre>

 *   SerialPort port = new SerialPort(0, 9600);

 *   if (!port.isOpen())

 *     return;

 *

 *   BufferStream bufferPort=new BufferStream(port);

 *   byte buf[] = new byte[10];

 *   buf[0] = 3;

 *   buf[1] = 7;

 *   bufferPort.writeBytes(buf, 0, 2);

 *   buf[0] = 4;

 *   buf[1] = 1;

 *   bufferPort.writeBytes(buf, 0, 2);

 *   bufferPort.flush();

 *   int count = port.readBytes(buf, 0, 10);

 *   if (count == 10)

 *   ...

 *   port.close();

 * </pre>

 */

public class BufferStream extends Stream

{

  /** the default buffer and increment size */

  private static final int BUFFER_SIZE=50;



  /** the underlying stream */

  protected Stream stream=null;



  /** the read buffer */

  protected byte[] rbuffer;



  /** the write buffer (often points the the same array as rbuffer) */

  protected byte[] wbuffer;



  /** the number of bytes available to read in the buffer */

  protected int readAvailable;



  /** the number of bytes of space available in the buffer to write */

  protected int writeAvailable;



  /** the index of the array indicating the start point (used by reset()) */

  protected int initStart=0;



  /** the number of bytes available to read or write to initially (used by reset()) */

  protected int initCount=-1;



  /** the array position where the next byte will be read from */

  protected int readPos=0;



  /** the array position where the next byte will be written to */

  protected int writePos=0;



  /** is the buffer growable? (empty constructor) */

  protected boolean isGrowable=false;



  /** have we read bytes from this BufferStream? */

  protected boolean haveRead=false;



  /** have we written bytes to this BufferStream? */

  protected boolean haveWritten=false;



  /** is this stream closed? */

  protected boolean closed=false;



  /**

   * Constructs a new BufferStream for writing to.  When all the data

   * has been written, use getBuffer() to get the completed byte array.

   */

  public BufferStream()

  {

    setBuffer(null,0,0);

  }



  /**

   * Constructs a new BufferStream for reading from or writing to a given

   * byte array.  The array will not be grown if more bytes are attempted

   * to be written than the size of the buffer.

   */

  public BufferStream(byte[] buffer)

  {

    this(buffer,0,buffer.length);

  }



  /**

   * Constructs a new BufferStream for reading from or writing to a segment

   * of a given byte array.  The array will not be grown if more bytes are

   * attempted to be written than the given count.

   */

  public BufferStream(byte[] buffer,int start,int count)

  {

    setBuffer(buffer,start,count);

  }



  /**

   * Constructs a new BufferStream for buffering input and output from

   * a given stream.  A default buffer size of 50 is used.

   * @param stream the stream to buffer.

   */

  public BufferStream(Stream stream)

  {

    this(stream,BUFFER_SIZE);

  }



  /**

   * Constructs a new BufferStream for buffering input and output from

   * a given stream.

   * @param stream the stream to buffer.

   * @param size the size of the buffer in bytes.  This is the number of

   * bytes that are read or written when it is empty or full

   */

  public BufferStream(Stream stream,int size)

  {

    this.stream=stream;

    rbuffer=new byte[size];

    wbuffer=rbuffer;

    readAvailable=0;

    writeAvailable=size;

  }



  /**

   * Gets the buffer.  Only useful for BufferStreams initialised with the

   * empty constuctor.

   * @return null, if this is a stream based BufferStream,

   * the original buffer passed in, if one was, or

   * a buffer the exact size of the number of bytes written to it,

   * if the empty constructor was used.

   */

  public byte[] getBuffer()

  {

    if (stream!=null)

      return null;

    if (isGrowable)

    {

      byte[] b=new byte[writePos];

      Vm.copyArray(wbuffer,0,b,0,writePos);

      return b;

    }

    return wbuffer;

  }



	/**

	 * Sets the buffer used by this BufferStream.  If the buffer is null,

	 * a growable array is used.

	 * @param buffer the buffer to use.

	 */

	public void setBuffer(byte[] buffer)

	{

		setBuffer(buffer,0,buffer!=null?buffer.length:0);

	}



	/**

	 * Sets the buffer slice used by this BufferStream.  If the buffer is null,

	 * a growable array is used.

	 * @param buffer the buffer to use.

	 * @param start the start position of the buffer

	 * @param count the number of bytes to access

	 */

	public void setBuffer(byte[] buffer,int start,int count)

	{

	  if (stream!=null)

		  return;

		if (buffer==null)

		{

		  if (wbuffer==null||!isGrowable)

  			wbuffer=new byte[BUFFER_SIZE];

			rbuffer=null;

			readAvailable=0;

			writeAvailable=wbuffer.length;

			initStart=0;

			initCount=-1;

			readPos=0;

			writePos=0;

			isGrowable=true;

		}

		else

		{

			rbuffer=buffer;

			wbuffer=buffer;

			initStart=start;

			initCount=count;

			readPos=start;

			writePos=start;

			readAvailable=count;

			writeAvailable=count;

			isGrowable=false;

		}

		haveRead=false;

		haveWritten=false;

	}



  /**

   * Reads bytes from the stream until the stream finishes or the given

   * byte is found.

   * @param b the byte to look for

   * @return a byte array containing all the bytes up to but not including

   * the wanted byte.

   */

  public byte[] readBytesUntil(byte b)

  {

		if (closed)

		  return null;

    byte[] dummy=new byte[1];

    byte[] ret=null;

    int bytesAdded=0;

    while(true)

    {

      // hack to fill the buffer if it is empty

      if (readAvailable==0)

      {

        // if it's the end of the stream, return what we've got

        if (readBytes(dummy,0,1)==0)

          return ret;



        // backtrack that one byte

        readPos--;

        readAvailable++;

      }

      int i;

      for(i=0;i<readAvailable&&rbuffer[readPos+i]!=b;i++);

      if (ret==null)

        ret=new byte[i];

      else

      {

        byte[] temp=new byte[bytesAdded+i];

        Vm.copyArray(ret,0,temp,0,bytesAdded);

        ret=temp;

      }

      bytesAdded+=readBytes(ret,bytesAdded,i);

      if (i<readAvailable)

        return ret;

    }

  }



  /**

   * Reads bytes from the stream. Returns the

   * number of bytes actually read or -1 if an error prevented the

   * read operation from occurring.

   * @param buf the byte array to read data into

   * @param start the start position in the array

   * @param count the number of bytes to read

   */

  public int readBytes(byte buf[], int start, int count)

  {

		if (closed)

		  return -1;

    // if we are reading and writing to a stream, make a new buffer

    if (stream!=null&&haveWritten&&!haveRead)

      rbuffer=new byte[rbuffer.length];

    haveRead=true;



    int numRead=0; // the number of bytes actually read



    // while we've still got bytes to read;

    while (count>0)

    {

      // if there are no available bytes to read in the buffer...

      if (readAvailable<=0)

      {

        // if we're working from just a buffer, quit.

        if (stream==null)

          break;



        // if we've got a stream, try reading in another bufferfull.

        readAvailable=stream.readBytes(rbuffer,0,rbuffer.length);

        readPos=0;

        if (readAvailable==-1) // read error, pass it on

          return -1;

        else

        if (readAvailable==0) // no bytes available, so quit

          break;

      }

      // copy bytes from our buffer to the given one.

      int toRead=(readAvailable<count?readAvailable:count);

      Vm.copyArray(rbuffer,readPos,buf,start,toRead);



      // update our variables

      readPos+=toRead;

      numRead+=toRead;

      start+=toRead;

      count-=toRead;

      readAvailable-=toRead;

    }

    return numRead;

  }



  /**

   * Writes bytes to the the stream. Returns the

   * number of bytes actually written or -1 if an error prevented the

   * write operation from occurring.

   * @param buf the byte array to write data from

   * @param start the start position in the byte array

   * @param count the number of bytes to write

   */

  public int writeBytes(byte buf[], int start, int count)

  {

		if (closed)

		  return -1;

    // if we are reading and writing to a stream, make a new buffer

    if (stream!=null&&haveRead&&!haveWritten)

      wbuffer=new byte[rbuffer.length];

    haveWritten=true;



    int numWritten=0; // the number of bytes actually written



    // while we've still got bytes to write...

    while (count>0)

    {

      // if we can grow our buffer and it isn't big enough to hold the new

      // bytes, make it bigger

      if (isGrowable&&writeAvailable<count)

      {

        // find out how many increments of BUFFER_SIZE we need to add to

        // the buffer to make it fit count

        int inc=((count-writeAvailable-1)/BUFFER_SIZE+1)*BUFFER_SIZE;

        writeAvailable+=inc;

        byte[] temp=new byte[wbuffer.length+inc];

        Vm.copyArray(wbuffer,0,temp,0,writePos);

        wbuffer=temp;

      }

      // if there isn't any room left...

      if (writeAvailable<=0)

      {

        // if we're working from just a buffer, quit.

        if (stream==null)

          break;



        // if we've got a stream, try writing out the buffer

        int streamWritten=stream.writeBytes(wbuffer,0,wbuffer.length);



        if (streamWritten==-1) // write error, pass it on

          return -1;

        if (streamWritten==0) // couldn't write any so quit.

          break;

        // if we wrote some but not all, copy the remaining down

        if (streamWritten!=wbuffer.length)

        {

          Vm.copyArray(wbuffer,streamWritten,wbuffer,0,wbuffer.length-streamWritten);

          writeAvailable=streamWritten;

          writePos=wbuffer.length-streamWritten;

        }

        else // set up for an empty buffer

        {

          writeAvailable=wbuffer.length;

          writePos=0;

        }

      }



      // copy bytes from the given buffer to our one

      int toWrite=(writeAvailable<count?writeAvailable:count);

      Vm.copyArray(buf,start,wbuffer,writePos,toWrite);



      // update our variables

      start+=toWrite;

      writePos+=toWrite;

      numWritten+=toWrite;

      count-=toWrite;

      writeAvailable-=toWrite;

    }

    return numWritten;

  }



  /**

   * For stream based buffers, sends any remaining data in the buffer.

   * You must call this before closing the underlying stream or some

   * data may be lost.

   * @return the number of bytes flushed down the stream, or -1 if a

   * write error occured

   */

  public int flush()

  {

    if (stream==null||writePos==0)

      return 0;

    int ret=stream.writeBytes(wbuffer,0,writePos);

    writePos=0;

    writeAvailable=wbuffer.length;

    return ret;

  }



  /**

   * For non stream based buffers, resets the read and write positions

   * to their initial location.  The actual data is not reset.

   */

  public void reset()

  {

    if (stream==null)

    {

      readPos=initStart;

      writePos=initStart;

      writeAvailable=initCount!=-1?initCount:wbuffer.length;

      readAvailable=(rbuffer==null?0:writeAvailable);

      closed=false;

    }

  }



  /**

	 * Closes the stream. Returns true if the operation is successful

	 * and false otherwise.

	 */

	public boolean close()

	{

		if (stream!=null)

		{

			flush();

			return stream.close();

	  }

	  closed=true;

	  return true;

	}

}