package waba.io;

import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import waba.sys.*;

/** this class is used to create an resizable record. you can use it with DataStream. example:
   <pre>
      Catalog cat = new Catalog(type+"."+creator+"."+type,Catalog.READ_WRITE);
      ResizeStream rs = new ResizeStream(cat,512);
      DataStream ds = new DataStream(rs);
      rs.startRecord();
      ds.writeStringArray(aStringArray);
      rs.endRecord();
      ds.close();
   </pre>
   <br>ps: if you dont call startRecord, writeBytes will simply call catalog.writeBytes and will not resize the record.
   <br>created by guich (guich@email.com)
*/
public class ResizeStream extends Stream
{
   /** the associated catalog */
   Catalog cat;
   /** the intial size of the record */
   int initialSize;
   /** how many bytes were writted */
   int writted;
   /** current record size */
   int size;
   
   /** constructs the resize stream. 
   @param cat the catalog associated
   @param initialSize the initial size of the record
   */
   public ResizeStream(Catalog cat, int initialSize)
   {
      this.cat = cat;
      this.initialSize = initialSize;
   }
   
   /** add a new record to the catalog */
   public void startRecord()
   {
      cat.addRecord(initialSize);
      writted = 0;
      size = initialSize;
   }

   /** must be called after the record is finished so it can be resized */
   public void endRecord()
   {
      cat.resizeRecord(writted);
      size = 0;
   }
   
   /** simply read the bytes from the associated catalog */
   public int readBytes(byte buf[], int start, int count)
   {
      return cat.readBytes(buf,start,count);
   }
   /** writes to the buffer, growing the record if necessary. */
   public int writeBytes(byte buf[], int start, int count)
   {
      if (size == 0) 
      	return cat.writeBytes(buf,start,count);
      if (count - start <= 0) return 0;
      
      int total = writted + (count-start);
      
      while (total > size) // nao tem mais espaco
      {
         size += initialSize;
         if (!cat.resizeRecord(size)) // expande
            return -1;
      }
      int n = cat.writeBytes(buf,start,count);
      if (n >= 0) writted += n;
      return n;
   }
   /** closes the catalog */
   public boolean close()
   {
      return cat.close();
   }
}