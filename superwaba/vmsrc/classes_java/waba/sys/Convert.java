package waba.sys;

/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

/**
 * Convert is used to convert between objects and basic types.
 */
public class Convert {
	public static int PRECISION = 6; // ds@110
private Convert()
	{
	}
   /** returns the abs of the number */
   static public int abs(int i)
   {
	  return (i < 0)?-i:i;
   }   
   /** adds two "doubles". for better precision, this method use the representation of the string as doubles, compute and then returns an String. Added by guich */
   public static String add(String d1, String d2) {
	   try {
		   double d = Double.valueOf(d1).doubleValue() + Double.valueOf(d2).doubleValue();
		   return formatDouble(d, PRECISION);
	   } catch (Exception e) {
		   return "";
	   }
   }
   

   
   /** divides two "doubles". for better precision, this method use the representation of the string as doubles, compute and then returns an String. Added by guich */
   public static String div(String d1, String d2) {
   	   try {
		   double d = Double.valueOf(d1).doubleValue() / Double.valueOf(d2).doubleValue();
		   return formatDouble(d, PRECISION);
	   } catch (Exception e) {
		   return "";
	   }
   }      
/*
 * ds@110 try to keep in line with palm/win ce implementation for formatting
 * doubles
 */
private static String formatDouble(double doubleValue, int precision) {
	double d, fator = 0;
	int i;
	StringBuffer sb = new StringBuffer(60);
	if (doubleValue == 0)
		return "0";
	else {
		if (precision > 0) {
			fator = 10;
			for (i = 0; i < precision; i++)
				fator *= 10;
		}
		if (doubleValue < 0) {
			sb.append('-');
			doubleValue = -doubleValue;
		}
		i = (int) doubleValue;
		d = doubleValue - i;
		d += (double) 5 / fator; // round

		sb.append((int) i);
		if (precision > 0) {
			sb.append('.');
			for (i = 0; i < precision; i++) {
				d *= 10;
				sb.append((char) ('0' + ((int) d % 10)));
			}
		}
	}
	return sb.toString();
}
   /** returns the maximum of the 2 arguments */
   static public int max(int i, int j)
   {
	  return (i > j)?i:j;
   }   
   /** returns the minimum of the 2 arguments */
   static public int min(int i, int j)
   {
	  return (i < j)?i:j;
   }   
   /** multiplies two "doubles". for better precision, this method use the representation of the string as doubles, compute and then returns an String. Added by guich */
   public static String mul(String d1, String d2) {
	   try {
		   double d = Double.valueOf(d1).doubleValue() * Double.valueOf(d2).doubleValue();
		   return formatDouble(d, PRECISION);
	   } catch (Exception e) {
		   return "";
	   }
   }       
   /** subtracts two "doubles". for better precision, this method use the representation of the string as doubles, compute and then returns an String. Added by guich */
   public static String sub(String d1, String d2) {
	   try {
		   double d = Double.valueOf(d1).doubleValue() - Double.valueOf(d2).doubleValue();
		   return formatDouble(d, PRECISION);
	   } catch (Exception e) {
		   return "";
	   }
   }       
   /** Converts the given String to an float. If the string passed is not a valid float, 0 is returned. */
   public static float toFloat(String s) {
	   try {
		   return Float.valueOf(s).floatValue();
	   } catch (Exception e) {
		   return 0;
	   }
   }      
/** Converts the given IEEE 754 bit representation of a float to a float. */

public static float toFloatBitwise(int i)
	{
	return Float.intBitsToFloat(i);
	}
/**
 * Converts the given String to an int. If the string passed is not a valid
 * integer, 0 is returned.
 */

public static int toInt(String s)
	{
	int i = 0;
	try { i = java.lang.Integer.parseInt(s); }
		catch (Exception e) {}
	return i;
	}
/** Converts the given float to its bit representation in IEEE 754 format. */

public static int toIntBitwise(float f)
	{
	return Float.floatToIntBits(f);
	}
   /** converts the char to lower case letter */
   public static char toLowerCase(char c)
   {
	  if ('A' <= c && c <= 'Z')
		 c += 32; else
	  if ('À' <= c && c <= 'Ý')
		 c += 32;
	  return c;
   }   
   /** converts the string to lower case letters */
   public static String toLowerCase(String s)
   {
	  char[] c = s.toCharArray();
	  for (int i=0; i < c.length; i++)
		 c[i] = toLowerCase(c[i]);
		return new String(c);
   }   
/** Converts the given char to a String. */

public static String toString(char c)
	{
	return "" + c;
	}
/** Converts the given float to a String. */

public static String toString(float f)
	{
	return java.lang.Float.toString(f);
	}
 /** Converts the given float to a String, formatting it with d decimal places. added by guich */
   public static String toString(float f, int d) {
	   return formatDouble(f, d);
   }
/** Converts the given int to a String. */

public static String toString(int i)
	{
	return java.lang.Integer.toString(i);
	}
   /** formats an String as a double, with n decimal places. if the number is invalid, "0" is returned. */
   public static String toString(String doubleValue, int n) {
	   return formatDouble(Double.valueOf(doubleValue).doubleValue(), n);
   }                
/** Converts the given boolean to a String. */

public static String toString(boolean b)
	{
	return "" + b;
	}
   /** converts the char to upper case letter */
   public static char toUpperCase(char c)
   {
	  if ('a' <= c && c <= 'z')
		 c -= 32; else
	  if ('à' <= c && c <= 'ý' && c != 247)
		 c -= 32;
	  return c;
   }   
   /** converts the string to upper case letters */
   public static String toUpperCase(String s)
   {
	  char[] c = s.toCharArray();
	  for (int i=0; i < c.length; i++)
		 c[i] = toUpperCase(c[i]);
	  return new String(c);
   }   
   /** pads the string with zeroes at left */
   static public String zeroPad(String s, int size)
   {
	  while (s.length() < size) s = "0"+s;
	  return s;
   }      
   ////////////////////////////////////////////////////////////////////////////////
   /** used in international character convertions */
   // guich@120
   public static String charEncoding = "iso_8859-1"; // character scheme used in Palm OS
   public static byte[] java2palmString(String s)
   {
      try
      {
         return s.getBytes(charEncoding);
      } catch (java.io.UnsupportedEncodingException e) {e.printStackTrace();}
      return s.getBytes();      
   }
   /** used in international character convertions */
   // guich@120
   public static String palm2javaString(byte []in)
   {
      try
      {
         return new String(in,charEncoding);
      } catch (java.io.UnsupportedEncodingException e) {e.printStackTrace();}
      return new String(in);
   }
   
   // guich@120 - used for debugging.
   public static String toHex(byte bytes[])
   {
      StringBuffer sb = new StringBuffer(5000);
      for (int j = 0; j < bytes.length; j++)
      {
         int ii = bytes[j];
         if (ii < 0) ii += 256;
         String ss = Integer.toHexString(ii);
         if (ss.length() == 1) ss = "0"+ss;
         if ((j+1) % 16 == 0) ss += "\r\n";
         sb.append(' ');
         sb.append(ss);
      }
      return sb.toString().toUpperCase();
   }
}