/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

Created by Guich

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

package waba.sys;

import waba.fx.*;
import waba.util.*;

/** this class provides some preferences from palms config. */

public class Settings
{
   /** month day year */
   public static final byte DATE_MDY = 1;
   /** day month year */
   public static final byte DATE_DMY = 2;
   /** year month day */
   public static final byte DATE_YMD = 3;

   /** stop initialization */
   private Settings()
   {
   }

   /** called from the vm at startup */
   private Settings(byte dateFormat, char dateSeparator, byte weekStart,
                    boolean is24Hour, char timeSeparator,char thousandsSeparator,
                    char decimalSeparator, int screenWidth, int screenHeight)
   {
      this.dateFormat = dateFormat;
      this.dateSeparator = dateSeparator;
      this.weekStart = weekStart;
      this.is24Hour = is24Hour;
      this.timeSeparator = timeSeparator;
      this.thousandsSeparator = thousandsSeparator;
      this.decimalSeparator = decimalSeparator;
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
    }

    /** can be one of the following constants: DATE_MDY, DATE_DMY, DATE_YMD; where m = month, d = day and y = year */
    public static byte dateFormat;
    /** the date char separator. */
    public static char dateSeparator;
    /** the week day start. 0 = sunday, 6 = saturday */
    public static byte weekStart;
    /** true if the time format is 24 hour format or if it is the AM/PM format */
    public static boolean is24Hour;
    /** the time char separator */
    public static char timeSeparator;
    /** the thousands separator for numbers */
    public static char thousandsSeparator;
    /** the decimal separator for numbers */
    public static char decimalSeparator;
    /** the screen width in pixels */
    public static int screenWidth;
    /** the screen height in pixels */
    public static int screenHeight;

   public static String getString()
   {
      String []dfs = {"MDY","DMY","YMD"};
      return "Settings - dateFormat: "+dfs[dateFormat-1]
            +", dateSeparator: "+dateSeparator
            +", weekStart: "+weekStart
            +", is24Hour: "+is24Hour
            +", timeSeparator: "+timeSeparator
            +", thousandsSeparator: "+thousandsSeparator
            +", decimalSeparator: "+decimalSeparator
            +", screenWidth: "+screenWidth
            +", screenHeight: "+screenHeight;
   }
}