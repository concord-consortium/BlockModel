package waba.util;

import waba.sys.*;
import waba.io.*;

/**
 * A int vector is an array of int's. The vector grows and shrinks
 * dynamically as objects are added and removed.
 * <p>
 * Here is an example showing a vector being used:
 *
 * <pre>
 * ...
 * IntVector vec = new IntVector();
 * vec.add(int1);
 * vec.add(int22);
 * ...
 * vec.insert(3, int3);
 * vec.del(2);
 * if (vec.getCount() > 5)
 * ...
 * </pre>
 * <br>for efficiency, get and set is made directly through the <items> public array. <b>please use add and del to insert and remove elements</b>
 * <br> created by guich from waba.util.Vector.
 */
public class IntVector
{
   public int items[];
   int count;

   /** Constructs an empty vector. */
   public IntVector()
   {
   	this(20);
   }
   /** Constructs an vector readen from the stream */
   public IntVector(DataStream in)
   {
      count = (int)in.readShort();
      items = new int[count+5];
      for (int i =0; i < count; i++)
         items[i] = in.readInt();
   }

   /**
    * Constructs an empty vector with a given initial size. The size is
    * the initial size of the vector's internal object array. The vector
    * will grow as needed when objects are added.
    */
   public IntVector(int size)
   {
   	items = new int[size];
   }

   /** Adds an object to the end of the vector. */
   public void add(int obj)
   {
   	if (count < items.length)
   		items[count++] = obj;
   	else
   		insert(count, obj);
   }

   /** Inserts an object at the given index. */
   public void insert(int index, int obj)
   {
   	if (count == items.length)
   	{
   		// grows 10%
   		int newItems[] = new int[items.length + items.length/10+1];
   		Vm.copyArray(items, 0, newItems, 0, count);
   		items = newItems;
   	}
   	if (index != count)
   		Vm.copyArray(items, index, items, index + 1, count - index);
   	items[index] = obj;
   	count++;
   }

   /** Deletes the object reference at the given index. */
   public void del(int index)
   {
   	if (index != count - 1)
   		Vm.copyArray(items, index + 1, items, index, count - index - 1);
   	items[count - 1] = 0;
   	count--;
   }

   /**
    * Finds the index of the given object. The list is searched using a O(n) linear
    * search through all the objects in the vector.
    */
   public int find(int obj)
   {
   	for (int i = 0; i < count; i++)
   		if (items[i] == obj)
   			return i;
   	return -1;
   }

   /** Returns the number of objects in the vector. */
   public int getCount()
   {
   	return count;
   }
  
   /** writes this int vector to the stream */
   public void writeTo(DataStream out)
   {
      out.writeShort(count);
      for (int i=0; i < count; i++)
         out.writeInt(items[i]);
   }
   /** used to let this int vector act like a bit vector. returns true if the bit specified is set. 
   you must guarantee that the index exists in the vector. guich@102 */
   public boolean isBitSet(short index)
   {
      return (items[index/32] & ((int)1 << (index % 32))) != 0;
   }
   
   /** used to let this int vector act like a bit vector. returns true if the bit specified is set. 
   you must guarantee that the index exists in the vector. guich@102 */
   public void setBit(short index, boolean on)
   {
      if (on)
         items[index/32] |= ((int)1 << (index % 32));  // set
      else
         items[index/32] &= ~((int)1 << (index % 32)); // reset
   }
}