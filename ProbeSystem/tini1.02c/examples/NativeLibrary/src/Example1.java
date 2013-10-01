/*
 * Example1.java - TAC, 05/20/2000
 */

/* Copyright (C) 1999, 2000 Dallas Semiconductor Corporation.
 * All rights Reserved. Printed in U.S.A.
 * This software is protected by copyright laws of
 * the United States and of foreign countries.
 * This material may also be protected by patent laws of the United States
 * and of foreign countries.
 * This software is furnished under a license agreement and/or a
 * nondisclosure agreement and may only be used or copied in accordance
 * with the terms of those agreements.
 * The mere transfer of this software does not imply any licenses
 * of trade secrets, proprietary technology, copyrights, patents,
 * trademarks, maskwork rights, or any other form of intellectual
 * property whatsoever. Dallas Semiconductor retains all ownership rights.
 */
/*
 $Workfile: Example1.java $
 $Revision: 5 $
 $Date: 11/29/00 12:45p $
 $Author: Chenot $
 $Modtime: 11/29/00 12:45p $
*/
/** This class demonstrates TINI native method usage. It is simple
 *  example that demonstrates parameter and return value processing in a native
 *  method. It is assumed that the native library has been loaded into TINI's
 *  file system - otherwise an UnsatisfiedLinkError will be thrown.
 *
 *  This example passes three parameters down to the native library in method1.
 *  method1 fills in the array, stores the long parameter in a system buffer
 *  and returns the int value. method2 reads the long value from the system
 *  buffer and returns the value back to Java. method3 implements a counter
 *  using indirect registers. method4 performs a native thread sleep.
 */
public class Example1
{
  // static method with parameters and int return type
  public static native int method1(byte[] s, long l, int i);

  // virtual method with "implicit" "this" reference and long return type
  public native long method2();

  // static method with no parameters and long return type
  public static native long method3();

  // static method with int parameter and int return type
  public static native int method4(int sleepTimeMillis);

  public static void main(String[] args)
  {
    System.out.println("Starting Example1");

    try
    {
      // attempt to load our native library
      System.loadLibrary("example1.tlib");

      byte[] b = new byte[40];

      int  i = 678;
      long l = 12345L;

      System.out.println("b, l, i: "+new String(b)+", "+l+", "+i);

      i = method1(b, l, i);
      System.out.println("b, l, i: "+new String(b)+", "+l+", "+i);

      l = (new Example1()).method2();
      System.out.println("b, l, i: "+new String(b)+", "+l+", "+i);

      for(i = 0; i < 10; i++)
      {
        // get our count value
        System.out.println("\"indirect\" count value: "+method3());
        // sleep for 1 second
        System.out.println("method4 returned: "+method4(1000));
      }
    }
    catch(Throwable t)
    {
      System.out.println(t);
    }

    System.out.println("Exiting Example1");
  }
}
