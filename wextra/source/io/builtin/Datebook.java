/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 16/10/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 ****************************************************************************/

package extra.io.builtin;



import waba.sys.Time;

import extra.io.*;



/**

 * Provides a link to the standard Palm Datebook database.  See BuiltinTest.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.0 16 October 1999

 */

public class Datebook implements Storable

{

  /** the datebook catalog */

  private static ObjectCatalog datebookCat=new ObjectCatalog("DatebookDB.date.DATA");



  /**

   * Gets the number of Datebook's in the database

   * @return the number of datebooks

   */

  public static int datebookCount()

  {

    return datebookCat.getRecordCount();

  }



  /**

   * Gets a Datebook from the database

   * @param i the index to get

   * @return the retrieved datebook

   */

  public static Datebook getDate(int i)

  {

    Datebook datebook=new Datebook();

    if (datebookCat.loadObjectAt(datebook,i))

      return datebook;

    return null;

  }



  /**

   * Gets a Datebook from the database and places it into the given Datebook.

   * Any previous data in the datebook is erased.

   * @param i the index to get

   * @param datebook the datebook object to place the datebook into.  

   */

  public static boolean getDate(int i,Datebook datebook)

  {

    return datebookCat.loadObjectAt(datebook,i);

  }



  /**

   * Adds a new Datebook to the database

   * @param datebook the datebook to add

   * @return true if successful, false otherwise

   */

  public static boolean addDate(Datebook datebook)

  {

    return datebookCat.addObject(datebook);

  }



  /////////////////////////////////////

  // individual datebook stuff below //

  /////////////////////////////////////

  

  // flag positions for each of the elements of an appointment

  private static final int WHEN=        1<<7;

  private static final int ALARM=       1<<6; 

  private static final int REPEAT=      1<<5;

  private static final int NOTE=        1<<4;

  private static final int EXCEPTIONS=  1<<3;

  private static final int DESCRIPTION= 1<<2;

  

  // constants for alarmUnits

  public static final int ALARM_MINUTES=0;

  public static final int ALARM_HOURS=1;

  public static final int ALARM_DAYS=2;



  // constants for repeatType

  public static final int REPEAT_NONE=0;

  public static final int REPEAT_DAILY=1;

  public static final int REPEAT_WEEKLY=2;

  public static final int REPEAT_MONTHLY_BY_DAY=3;

  public static final int REPEAT_MONTHLY_BY_DATE=4;

  public static final int REPEAT_YEARLY=5;



  /** The time and date this datebook item is to start */

  public Time startDate;



  /** 

   * The time this datebook item is to end - date info is ignored.  If this

   * value is null, this appointment has no time, regardless of any time

   * information in startDate.

   */

  public Time endTime;



  /** 

   * The units of time that the alarm should go off before the start

   * date.  Should be one of ALARM_XXX constants.

   */

  public int alarmUnits=ALARM_MINUTES;



  /** 

   * The number of <code>alarmUnit</code>s before <code>startDate</code> 

   * that the alarm should go off, or -1 for no alarm.

   */

  public int alarmAdvance=-1;



  /** The type of repeat (one of the <code>REPEAT_XXX</code> constants) */

  public int repeatType=REPEAT_NONE;



  /** The end date for repeating (or null for forever) */

  public Time repeatEndDate=null;



  /** The interval (in repeatTypes) between repetitions */

  public int repeatFrequency=1;



  /**

   * Indicates the day of week to repeat.  Only applicable when 

   * <code>repeatType</code> is <code>REPEAT_MONTHLY_BY_DAY</code>.

   * Should be in the range 0 (Sunday) to 6 (Saturday).  This field is used

   * in conjunction with <code>repeatMonthlyCount</code> to represent

   * things like the 3rd Sunday every month by setting

   * <code>repeatMonthlyDay</code> to 0 and <code>repeatMonthlyCount</code> to 3.

   */

  public int repeatMonthlyDay=0;



  /**

   * Indicates the number of <code>repeatMonthlyDay</code>s into each month

   * this appointment should repeat.  Only applicable when repeatType is

   * REPEAT_MONTHLY_BY_DAY.

   */

  public int repeatMonthlyCount=1;



  /**

   * Indicates which days of the week to repeat on.  Only applicable

   * when repeatType is REPEAT_WEEKLY.  The array holds 7 elements

   * with Sunday at position 0 through to Saturday at position 6. If

   * the value is true, this appointment should appear on that day.

   */

  public boolean[] repeatWeekdays=new boolean[7];



  /**

   * Indicated whether the week display should start with Sunday (false)

   * or Monday (true).  Only applicable with repeatType is REPEAT_WEEKLY

   * and I'm not entirely sure why it's here.  It's probably best not

   * to mess with it.

   */

  public boolean repeatWeekStartsOnMonday=false;



  /** A note giving extra information */

  public String note=null;



  /** 

   * An array of dates in chronological order that the appointment

   * shouldn't repeat on.  Time information is ignored.

   */

  public Time[] exceptions=null;



  /** A description for this appointment */

  public String description=null;



  /**

   * Constructs a new empty datebook

   */

  public Datebook()

  {

  }



 /**

   * Send the state information of this object to the given object catalog

   * using the given DataStream. If any Storable objects need to be saved

   * as part of the state, their saveState() method can be called too.

   */

  public void saveState(DataStream ds)

  {

    if (endTime==null) // no time

      ds.writeInt(-1); // two empty times in a row

    else

    {

      writeTime(ds,startDate);

      writeTime(ds,endTime);

    }

    writeDate(ds,startDate);

    int flags=(alarmAdvance!=-1?ALARM:0)|

      (repeatType!=REPEAT_NONE?REPEAT:0)|(note!=null?NOTE:0)|

      (exceptions!=null?EXCEPTIONS:0)|DESCRIPTION;

    ds.writeByte(flags);

    ds.writeByte(0); // align.

    if ((flags&ALARM)>0)

    {      

      ds.writeByte(alarmAdvance);

      ds.writeByte(alarmUnits);      

    }



    if ((flags&REPEAT)>0)

    {

      ds.writeByte(repeatType);

      ds.writeByte(0); // align.

      writeDate(ds,repeatEndDate);

      ds.writeByte(repeatFrequency);

      int repeatOn=0;

      if (repeatType==REPEAT_WEEKLY)

      {

        if (repeatWeekdays==null||repeatWeekdays.length!=7)

          repeatOn=1;

        else

        {

          for(int i=0,mult=1;i<7;i++,mult<<=1)

            repeatOn|=repeatWeekdays[i]?mult:0;

        }

      }

      else

      if (repeatType==REPEAT_MONTHLY_BY_DAY)

        repeatOn=(repeatMonthlyCount-1)*7+repeatMonthlyDay;

           

      ds.writeByte(repeatOn);

      ds.writeByte(repeatType==REPEAT_WEEKLY&&repeatWeekStartsOnMonday?1:0);

      ds.writeByte(0); // align.

    }



    if ((flags&EXCEPTIONS)>0)

    {

      int numExceptions=exceptions.length;

      ds.writeShort(numExceptions);

      for(int i=0;i<numExceptions;i++)

        writeDate(ds,exceptions[i]);

    }



    if (description==null)

      ds.writeCString("none");

    else

      ds.writeCString(description);



    if ((flags&NOTE)>0)

      ds.writeCString(note);

  }



  /**

   * Load state information from the given DataStream into this object

   * If any Storable objects need to be loaded as part of the state, their

   * loadState() method can be called too.

   */

  public void loadState(DataStream ds)

  {

    int pStartTime=ds.readUnsignedShort();

    int pEndTime=ds.readUnsignedShort();

    int packedDate=ds.readUnsignedShort();

    if (pStartTime==noDateOrTime&&packedDate==noDateOrTime)

      startDate=null;

    else

    {

      if (startDate==null)

        startDate=new Time();

      readTime(pStartTime,startDate);

      readDate(packedDate,startDate);

    }

    if (pEndTime==noDateOrTime||pStartTime==noDateOrTime)

      endTime=null;

    else

    {

      if (endTime==null)

        endTime=new Time();

      readTime(pEndTime,endTime);

      readDate(packedDate,endTime);

    }

    int flags=ds.readUnsignedByte();

    ds.skip(1);

    if ((flags&ALARM)>0)

    {      

      alarmAdvance=ds.readByte();

      alarmUnits=ds.readByte();

      

    }

    else

      alarmAdvance=-1;



    if ((flags&REPEAT)>0)

    {

      repeatType=ds.readByte();

      ds.skip(1);

      packedDate=ds.readUnsignedShort();

      if (packedDate==noDateOrTime)

        repeatEndDate=null;

      else

      {

        if (repeatEndDate==null)

          repeatEndDate=new Time();

        readDate(packedDate,repeatEndDate);

      }

      repeatFrequency=ds.readUnsignedByte();

      int repeatOn=ds.readUnsignedByte();

      repeatWeekStartsOnMonday=ds.readUnsignedByte()>0;

      if (repeatType==REPEAT_WEEKLY)

      {

        if (repeatWeekdays==null||repeatWeekdays.length!=7)

          repeatWeekdays=new boolean[7];

        for(int i=0,mult=1;i<7;i++,mult<<=1)

          repeatWeekdays[i]=(repeatOn&mult)>0;

      }

      else

        repeatWeekdays=null;



      if (repeatType==REPEAT_MONTHLY_BY_DAY)

      {

        repeatMonthlyDay=repeatOn%7;

        repeatMonthlyCount=repeatOn/7+1;

      }

      ds.skip(1);

    }

    else

      repeatType=REPEAT_NONE;



    if ((flags&EXCEPTIONS)>0)

    {

      int numExceptions=ds.readUnsignedShort();

      exceptions=new Time[numExceptions];

      for(int i=0;i<numExceptions;i++)

      {

        exceptions[i]=new Time();

        readDate(ds.readUnsignedShort(),exceptions[i]);

      }

    }

    else

      exceptions=null;



    description=((flags&DESCRIPTION)>0)?ds.readCString():null;



    // notes can be big so free up this one before loading in the

    // new one

    note=null;



    note=((flags&NOTE)>0)?ds.readCString():null;

  }



  /**

   * Gets a unique ID for this class.  It is up to the user to ensure that the ID of each

   * class of Storable contained in a single ObjectCatalog is unique and the ID of each instance

   * in a class is the same.

   */

  public byte getID()

  {

    return 0; // don't read or write type byte

  }



  /**

   * Returns an object of the same class as this object.

   * @returns a class.  Any data is irrelevent.

   */

  public Storable getInstance()

  {

    return new Datebook();

  } 



  ////////////////////////////////////////////////////

  //  Utility methods for reading and writing dates //

  ////////////////////////////////////////////////////



  /** Palm notation for no date or time */

  protected static int noDateOrTime=(1<<16)-1;



  /**

   * Writes the time information to the stream as a Palm TimeType

   * @param ds the stream to write to

   * @param time the time to write (only hour and minutes used)

   */

  protected void writeTime(DataStream ds,Time time)

  {

    if (time==null)

      ds.writeShort(-1);

    else

      ds.writeShort(((time.hour&255)<<8)|(time.minute&255));

  }



  /**

   * Writes the date information to the stream as a Palm DateType

   * @param ds the stream to write to

   * @param date the date to write (only day, month, year used)

   */

  protected void writeDate(DataStream ds,Time date)

  {

    if (date==null)

      ds.writeShort(-1);

    else

      ds.writeShort((((date.year-1904)&127)<<9)|((date.month&15)<<5)|

        (date.day&31));

  }



  /**

   * Reads time information from a short read from a stream in TimeType

   * format

   * @param packedTime the packed representation

   * @param time the object to load the information into. (h,m,s,millis set)

   */

  protected void readTime(int packedTime,Time time)

  {

    if (packedTime==noDateOrTime)

      time.minute=time.hour=0;

    else

    {

      time.minute=packedTime&255;

      packedTime>>>=8;

      time.hour=packedTime&255;

    }

    time.second=time.millis=0;

  }



  /**

   * Reads date information from a short read from a stream in DateType

   * format

   * @param packedDate the packed representation

   * @param date the object to load the information into (day,month,year set)

   */

  protected void readDate(int packedDate,Time date)

  {

    if (packedDate==noDateOrTime)

      date.day=date.month=date.year=0;

    else

    {

      date.day=(packedDate&31);

      packedDate>>>=5;

      date.month=(packedDate&15);

      packedDate>>>=4;

      date.year=(packedDate&127)+1904;

    }

  }

}