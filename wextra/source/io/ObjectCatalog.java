/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 23/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 * 02/06/1999  New      1.1.0    Rob Nielsen

 * Modified to allow objects to be stored in any order.

 *

 * 16/10/1999  New      1.2.0    Rob Nielsen

 * Modified to use DataStream for saving and loading

 *

 ****************************************************************************/



package extra.io;



import waba.io.*;

import waba.util.*;



/**

 * An extension to Catalog that allows storage and retrieval of objects

 * that implement the Storable interface.  Create an ObjectCatalog and use

 * the addObject() method on the objects you want to store.  If you want a

 * particular object you can use loadObjectAt() to load the stored details

 * into an object or to search through all records call resetSearch() and

 * then loop with nextObject() until it returns false.  The example below

 * shows an example of it's use with a catalog of identical data:

 * <p><blockquote><pre>

 *   ObjectCatalog oc=new ObjectCatalog("Test.DATA");

 *   MyObject obj=new MyObject();

 *   oc.resetSearch();

 *   while (oc.nextObject(obj))

 *   {

 *     // do something with obj

 *   }  

 * </pre></blockquote>

 * <p>

 * Here's an example using unknown data.  The two sections of code save a

 * vector containing a number of Lines, Circles, and Squares (all

 * implementing Storable) in no particular order, then loads it back in

 * again.

 * <p><blockquote><pre>

 *   // save data

 *   ObjectCatalog oc=new ObjectCatalog("Test.DATA",ObjectCatalog.CREATE);

 *   for(int i=0,size=objs.getCount();i++)

 *     oc.addObject((Storable)objs.get(i));

 *   oc.close();

 *

 *   // load data

 *   ObjectCatalog oc=new ObjectCatalog("Test.DATA");

 *   oc.registerClass(new Line());

 *   oc.registerClass(new Circle());

 *   oc.registerClass(new Square());

 *   objs=new Vector();

 *   oc.resetSearch();

 *   Storable obj;

 *   while ((obj=oc.nextObject())!=null)

 *   {

 *     objs.add(obj);

 *   }

 *   oc.close();

 * </pre></blockquote>

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.2.0 16 October 1999

 */

public class ObjectCatalog extends Catalog

{

  /* the registered classes */

  protected Vector classes;



  /* the position in the search through the records */

  protected int cnt=0;



	protected byte[] buf;



	protected BufferStream bs;



	protected DataStream ds;



  /**

   * Constructs a new ObjectCatalog

   * @param name the name of the catalog

   * @param type the mode to open the catalog with

   */

  public ObjectCatalog(String name,int type)

  {

    super(name,type);

  }



  /**

   * Construct a new ObjectCatalog

   * @param name the name of the catalog

   */

  public ObjectCatalog(String name)

  {

    super(name,Catalog.CREATE);

  }



  /**

   * Registers this class with the catalog.  Classes must be registered

   * before using the loadObjectAt(int) method.

   * @param s an instance of the class to register.  The contents are

   * ignored.

   */

  public void registerClass(Storable s)

  {

    if (classes==null)

      classes=new Vector();

    classes.add(s);

  }



  /**

   * Add an object to this catalog.

   * @param s the storable object to add

   */

  public boolean addObject(Storable s)

  {    

    if (bs==null)

		{

      bs=new BufferStream();

      ds=new DataStream(bs);

		}

		else

		  bs.setBuffer(null);



    if (s.getID()!=0)

      ds.writeByte(s.getID());

    s.saveState(ds);

    byte[] buf=bs.getBuffer();

    if (addRecord(buf.length)!=-1)

    {

      writeBytes(buf,0,buf.length);

      setRecordPos(-1);

      return true;

    }

    return false;

  }

  

  /**

   * Load an object from the catalog into the given storable.

   * Unpredictable results will occur if the object in the catalog is not

   * of the same class as the storable given.  Good for when you know what

   * each record will contain.

   * @param s the object to load the data into

   * @param i the index in the catalog to load from

   * @returns true if sucessful, false otherwise

   */

  public boolean loadObjectAt(Storable s,int i)

  {

    //bs=null;

		//buf=null;

    if (setRecordPos(i))

    {

      int size=getRecordSize();

			if (buf==null||buf.length<size)

        buf=new byte[size];

      readBytes(buf,0,size);

      setRecordPos(-1);

			if (bs==null)

			{

			  bs=new BufferStream(buf);

				ds=new DataStream(bs);

			}

      else

			  bs.setBuffer(buf);



      if (s.getID()!=0)

        ds.readByte();

      s.loadState(ds);

      return true;

    }

    return false;

  }



  /**

   * Loads an object from the catalog.  Good for when you don't know which classes are going

   * to be in records.  Note that you must call the registerClass() with each storable class

   * before this method will work properly.

   * @param i the index in the catalog to load from

   * @returns the loaded object, or null if unsucessful

   */

  public Storable loadObjectAt(int i)

  {

    Storable s=null;

    if (setRecordPos(i)&&classes!=null)

    {

      int recsize=getRecordSize();

      if (buf==null||buf.length<recsize)

        buf=new byte[recsize];      

      readBytes(buf,0,recsize);



			if (bs==null)

			{

			  bs=new BufferStream(buf);

				ds=new DataStream(bs);

			}

      else

			  bs.setBuffer(buf);



      setRecordPos(-1);

      byte type=ds.readByte();



      for(int j=0,size=classes.getCount();j<size;j++)

        if ((s=(Storable)classes.get(j)).getID()==type)

        {

          s=s.getInstance();

          s.loadState(ds);

          break;

        }

    }

    return s;

  }

  

  /**

   * Delete an object from the catalog

   * @param i the index to delete from

   * @returns true if sucessful, false otherwise

   */

  public boolean deleteObjectAt(int i)

  {

    if (setRecordPos(i))

    {

      deleteRecord();

      return true;

    }

    return false;

  }

  

  /**

   * Get the size of this catalog

   * @returns the number of records contained by it

   */

  public int size()

  {

    return getRecordCount();

  }



  /**

   * Resets a counter for iterating through the catalog.  Should be called before

   * iterating with nextObject().

   */

  public void resetSearch()

  {

    setSearchIndex(0);

  }



  /**

   * Sets the search counter at the given index in the catalog.

   * @param i the index to start

   */

  public void setSearchIndex(int i)

  {

    cnt=i;

  }



  /**

   * Gets the next object in the catalog and places it in the given storable.

   * @returns true if sucessful, false if the end of the catalog has been reached

   */

  public boolean nextObject(Storable s)

  {

    return cnt<size()&&loadObjectAt(s,cnt++);

  }



  /**

   * Gets the next object in the catalog.

   * @returns the next object, or null on error or if the end has been reached

   */

  public Storable nextObject()

  {

    return cnt<size()?loadObjectAt(cnt++):null;

  }

}