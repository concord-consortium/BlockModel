package waba.util;

/*
* This class is almost identical to java.util.Hashtable, with some
modifications.
* One important modification is that the key <b>must</b> be a String.
* added by guich@120
*/

/**
* This class implements a hashtable, which maps keys to values. Any
* non-<code>null</code> object can be used as a key or as a value.
* <p>
* To successfully store and retrieve objects from a hashtable, the
* objects used as keys must be String, because currently on SuperWaba only
* string objects implements hashCode method.
* <p>
* An instance of <code>Hashtable</code> has two parameters that
* affect its efficiency: its <i>capacity</i> and its <i>load
* factor</i>. The load factor should be between 0.0 and 1.0. When
* the number of entries in the hashtable exceeds the product of the
* load factor and the current capacity, the capacity is increased by
* calling the <code>rehash</code> method. Larger load factors use
* memory more efficiently, at the expense of larger expected time
* per lookup.
* <p>
* If many entries are to be made into a <code>Hashtable</code>,
* creating it with a sufficiently large capacity may allow the
* entries to be inserted more efficiently than letting it perform
* automatic rehashing as needed to grow the table.
* <p>
* This example creates a hashtable of numbers. It uses the names of
* the numbers as keys:
* <p><blockquote><pre>
*     Hashtable numbers = new Hashtable();
*     numbers.put("one", Convert.toString(1));
*     numbers.put("two", Convert.toString(2));
*     numbers.put("three", Convert.toString(3));
* </pre></blockquote>
* <p>
* To retrieve a number, use the following code:
* <p><blockquote><pre>
*     String n = (String)numbers.get("two");
*     if (n != null) {
*         System.out.println("two = " + Convert.toInt(n));
*     }
* </pre></blockquote>
*
* <b>Important notes</b> (added by guich)
* <p>
* There are no default constructor to decrease memory usage. Also,
* <p>
* thanks to Arthur van Hoff
*/
public class Hashtable
{
   /** Hashtable collision list. */
   class Entry
   {
      int hash;
      String key; // guich@120: key must be a string
      Object value;
      Entry next;
   }
   /** The hash table data. */
   private Entry table[];
   /** The total number of entries in the hash table. */
   private transient int count;
   /** Rehashes the table when count exceeds this threshold. */
   private int threshold;
   /** The load factor for the hashtable. */
   private float loadFactor;

   /**
   * Constructs a new, empty hashtable with the specified initial capacity
   * and default load factor of 0.75f.
   *
   * @param   initialCapacity   the initial capacity of the hashtable.
   */
   public Hashtable(int initialCapacity)
   {
      this(initialCapacity, 0.75f);
   }
   /**
   * Constructs a new, empty hashtable with the specified initial
   * capacity and the specified load factor.
   *
   * @param      initialCapacity   the initial capacity of the hashtable.
   * @param      loadFactor        a number between 0.0 and 1.0.
   */
   public Hashtable(int initialCapacity, float loadFactor)
   {
      this.loadFactor = loadFactor;
      table = new Entry[initialCapacity];
      threshold = (int)(initialCapacity * loadFactor);
   }
   /**
   * Clears this hashtable so that it contains no keys.
   */
   public void clear()
   {
      Entry tab[] = table;
      for (int index = tab.length; --index >= 0; )
      tab[index] = null;
      count = 0;
   }
   /**
   * Returns the value to which the specified key is mapped in this hashtable.
   *
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   * @see     java.util.Hashtable#put(java.lang.Object, java.lang.Object)
   * @since   JDK1.0
   */
   public Object get(String key)
   {
      int hash = key.hashCode();
      int index = (hash & 0x7FFFFFFF) % table.length;
      for (Entry e = table[index] ; e != null ; e = e.next)
      if ((e.hash == hash) && e.key.equals(key))
      return e.value;
      return null;
   }
   /**
    * Return a Vector of the keys in the Hashtable.
    * Added ds@120.
    */
   public Vector getKeys()
   {
   	Vector keys = new Vector();
   	int index = table.length;
   	Entry entry = null;
   	while ((index-- > 0) && ((entry = table[index]) == null));
   	if (entry != null)
   	{
   		Entry e = entry;
   		entry = e.next;
   		keys.add(e.key);
   	}
   	return keys;
   }
   /**
   * Maps the specified <code>key</code> to the specified
   * <code>value</code> in this hashtable. Neither the key nor the
   * value can be <code>null</code>.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method
   * with a key that is equal to the original key.
   *
   * @param      key     the hashtable key.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or <code>null</code> if it did not have one.
   * @see     java.lang.Object#equals(java.lang.Object)
   * @see     java.util.Hashtable#get(java.lang.Object)
   * @since   JDK1.0
   */
   public Object put(String key, Object value)
   {
      // Make sure the value is not null
      if (value == null)
      return null;

      // Makes sure the key is not already in the hashtable.
      Entry tab[] = table;
      int hash = key.hashCode();
      int index = (hash & 0x7FFFFFFF) % tab.length;
      for (Entry e = tab[index] ; e != null ; e = e.next)
         if ((e.hash == hash) && e.key.equals(key))
         {
            Object old = e.value;
            e.value = value;
            return old;
         }
      if (count >= threshold)
      {
         // Rehash the table if the threshold is exceeded
         rehash();
         return put(key, value);
      }

      // Creates the new entry.
      Entry e = new Entry();
      e.hash = hash;
      e.key = key;
      e.value = value;
      e.next = tab[index];
      tab[index] = e;
      count++;
      return null;
   }
   /**
   * Rehashes the contents of the hashtable into a hashtable with a
   * larger capacity. This method is called automatically when the
   * number of keys in the hashtable exceeds this hashtable's capacity
   * and load factor.
   *
   * @since   JDK1.0
   */
   protected void rehash()
   {
      int oldCapacity = table.length;
      Entry oldTable[] = table;

      int newCapacity = oldCapacity * 3 / 2 + 1; // guich@120 - grows 50% instead of 100%
      Entry newTable[] = new Entry[newCapacity];

      threshold = (int)(newCapacity * loadFactor);
      table = newTable;

      for (int i = oldCapacity ; i-- > 0 ;)
         for (Entry old = oldTable[i] ; old != null ; )
         {
            Entry e = old;
            old = old.next;

            int index = (e.hash & 0x7FFFFFFF) % newCapacity;
            e.next = newTable[index];
            newTable[index] = e;
         }
   }
   /**
   * Removes the key (and its corresponding value) from this
   * hashtable. This method does nothing if the key is not in the hashtable.
   *
   * @param   key   the key that needs to be removed.
   * @return  the value to which the key had been mapped in this hashtable,
   *          or <code>null</code> if the key did not have a mapping.
   */
   public Object remove(String key)
   {
      Entry tab[] = table;
      int hash = key.hashCode();
      int index = (hash & 0x7FFFFFFF) % tab.length;
      for (Entry e = tab[index], prev = null ; e != null ; prev = e, e = e.next)
         if ((e.hash == hash) && e.key.equals(key))
         {
            if (prev != null)
               prev.next = e.next;
            else
               tab[index] = e.next;
            count--;
            return e.value;
         }
      return null;
   }
   /**
   * Returns the number of keys in this hashtable.
   *
   * @return  the number of keys in this hashtable.
   */
   public int size()
   {
      return count;
   }
   /**
   * Returns a rather long string representation of this hashtable.
   *
   * @return  a string representation of this hashtable.
   * @since   JDK1.0
   */
   public String toString()
   {
      return "This hashtable has: "+count+" elements and threshold of "+threshold;
   }
}