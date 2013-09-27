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

 * Provides a link to the standard Palm ToDo database.  See BuiltinTest.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.0 16 October 1999

 */

public class ToDo implements Storable

{

  /** the todo catalog */

  private static ObjectCatalog todoCat=new ObjectCatalog("ToDoDB.todo.DATA");



  /**

   * Gets the number of ToDo's in the database

   * @return the number of todos

   */

  public static int todoCount()

  {

    return todoCat.getRecordCount();

  }



  /**

   * Gets a ToDo from the database

   * @param i the index to get

   * @return the retrieved todo

   */

  public static ToDo getToDo(int i)

  {

    ToDo todo=new ToDo();

    if (todoCat.loadObjectAt(todo,i))

      return todo;

    return null;

  }



  /**

   * Gets a ToDo from the database and places it into the given ToDo.

   * Any previous data in the todo is erased.

   * @param i the index to get

   * @param todo the todo object to place the todo into.  

   */

  public static boolean getToDo(int i,ToDo todo)

  {

    return todoCat.loadObjectAt(todo,i);

  }



  /**

   * Adds a new ToDo to the database

   * @param todo the todo to add

   * @return true if successful, false otherwise

   */

  public static boolean addToDo(ToDo todo)

  {

    return todoCat.addObject(todo);

  }



  // *************************** //

  // individual todo stuff below //

  // *************************** //



  /** 

   * The time this todo item is to be completed - note only date

   * information is used

   */

  public Time dueDate;



  /** The priority of this todo from 1-5 */

  public int priority;



  /** Has this todo been completed? */

  public boolean completed;



  /** A description for this todo */

  public String description=null;



  /** A note giving extra information */

  public String note=null;



  /**

   * Constructs a new empty todo

   */

  public ToDo()

  {

  }



  /**

   * Send the state information of this object to the given object catalog

   * using the given DataStream. If any Storable objects need to be saved

   * as part of the state, their saveState() method can be called too.

   */

  public void saveState(DataStream ds)

  {

    priority=inRange(priority,1,5);

    if (dueDate==null||dueDate.year<1904||dueDate.year>1904+127||

        dueDate.month<1||dueDate.month>12||dueDate.day<1||dueDate.day>31)

      ds.writeShort(~0);

    else

    {

      int packedDate=(((dueDate.year-1904)&127)<<9)|((dueDate.month&15)<<5)|

        (dueDate.day&31);

      ds.writeShort(packedDate);

    }

    priority=(priority<1?1:priority>5?5:priority);

    int priorityByte=priority|(completed?128:0);

    ds.writeByte(priorityByte);

    ds.writeCString(description);

    ds.writeCString(note);

  }



  private int inRange(int i,int a,int b)

  {

    if (i<=a)

      return a;

    else

    if (i>=b)

      return b;

    return i;

  }



  /**

   * Load state information from the given DataStream into this object

   * If any Storable objects need to be loaded as part of the state, their

   * loadState() method can be called too.

   */

  public void loadState(DataStream ds)

  {

    int packedDate=ds.readShort();

    if ((packedDate&(1<<16)-1)==(1<<16)-1)

      dueDate=null;

    else

    {

      if (dueDate==null)

        dueDate=new Time();

      dueDate.day=(packedDate&31);

      packedDate>>>=5;

      dueDate.month=(packedDate&15);

      packedDate>>>=4;

      dueDate.year=(packedDate&127)+1904;

      dueDate.hour=dueDate.minute=dueDate.second=dueDate.millis=0;

    }



    int priorityByte=ds.readUnsignedByte();

    priority=(priorityByte&127);

    completed=(priorityByte&128)>0;



    description=ds.readCString();

    if (description.length()==0)

      description=null;



    // notes can be big so free up this one before loading in the

    // new one

    note=null;



    note=ds.readCString();

    if (note.length()==0)

      note=null;

  }



  /**

   * Gets a unique ID for this class.  It is up to the user to ensure that the ID of each

   * class of Storable contained in a single ObjectCatalog is unique and the ID of each instance

   * in a class is the same.

   */

  public byte getID()

  {

    return 0; // not used

  }



  /**

   * Returns an object of the same class as this object.

   * @returns a class.  Any data is irrelevent.

   */

  public Storable getInstance()

  {

    return new ToDo();

  } 

}