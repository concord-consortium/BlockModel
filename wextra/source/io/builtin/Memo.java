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



import extra.io.*;

/**

 * Provides a link to the standard Palm Memo database.  See BuiltinTest.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.0 16 October 1999

 */

public class Memo implements Storable

{

  /** the memo catalog */

  private static ObjectCatalog memoCat=new ObjectCatalog("MemoDB.memo.DATA");



  /**

   * Gets the number of Memos in the database

   * @return the number of memos

   */

  public static int memoCount()

  {

    return memoCat.getRecordCount();

  }



  /**

   * Gets a Memo from the database

   * @param i the index to get

   * @return the retrieved memo

   */

  public static Memo getMemo(int i)

  {

    Memo memo=new Memo();

    if (memoCat.loadObjectAt(memo,i))

      return memo;

    return null;

  }



  /**

   * Gets a Memo from the database and places it into the given Memo.

   * Any previous data in the memo is erased.

   * @param i the index to get

   * @param memo the memo object to place the memo into.  

   */

  public static boolean getMemo(int i,Memo memo)

  {

    return memoCat.loadObjectAt(memo,i);

  }



  /**

   * Adds a new Memo to the database

   * @param memo the memo to add

   * @return true if successful, false otherwise

   */

  private static boolean addMemo(Memo memo)

  {

    return memoCat.addObject(memo);

  }



  // *************************** //

  // individual memo stuff below //

  // *************************** //



  /** the text of the memo */

  public String text;



  /**

   * Constructs a new empty memo

   */

  public Memo()

  {

  }



  /**

   * Send the state information of this object to the given object catalog

   * using the given DataStream. If any Storable objects need to be saved

   * as part of the state, their saveState() method can be called too.

   */

  public void saveState(DataStream ds)

  {

    ds.writeCString(text);

  }



  /**

   * Load state information from the given DataStream into this object

   * If any Storable objects need to be loaded as part of the state, their

   * loadState() method can be called too.

   */

  public void loadState(DataStream ds)

  {

    // memos can be big so free up this one before loading in the

    // new one

    text=null;



    text=ds.readCString();

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

    return new Memo();

  } 

}