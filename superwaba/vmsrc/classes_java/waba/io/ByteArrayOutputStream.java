package waba.io;

import waba.io.*;
import waba.util.*;
import waba.sys.*;

/** creates a byte array output stream. it can be useful with the method Catalog.inspectRecord, since that method
returns a byte array. added by guich. */

public class ByteArrayOutputStream extends Stream
{
   protected int pos;
   protected byte []buffer;
   
   /** sets the internal buffer to be the specified buffer param */
   public ByteArrayOutputStream(byte []buffer)
   {
	  this.buffer = buffer;
	  pos = 0;
   }   
   /** creates a new buffer with the specific size */
   public ByteArrayOutputStream(int size)
   {
	  this(new byte[size]);
   }   
   /** does nothing */
   public boolean close()
   {
	  return true;
   }   
   /** gets the buffer used */
   public byte []getBuffer()
   {
	  return buffer;
   }   
   /** transfers count bytes from class buffer to buf. <b>Does not perform range checking!</b>
   @return the number of bytes readen. */
   public int readBytes(byte buf[], int start, int count)
   {
	  Vm.copyArray(buffer,pos,buf,start,count);
	  pos += count;
	  return count;
   }   
   /** resets the position to 0 so the buffer can be reused. */
   public void reset()
   {
	  pos = 0; 
   }   
   /** does nothing */
   public int writeBytes(byte buf[], int start, int count)
   {
	  return 0;
   }   
}