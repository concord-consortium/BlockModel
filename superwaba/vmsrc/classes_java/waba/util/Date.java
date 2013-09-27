package waba.util;

import waba.sys.*; 

/**
 The Date class is a general date data type(Object) that is similar to those built in to other languages.  
 It supports all days from January 1st, 1983 through December 31st, 2999. It checks to make sure that the dates
 that are instanciated or changed exist and if they don't it defaults at today.  It provides 
 methods to advance the date backwards and forwards by increments of day, week, and month.  It provides 
 comparisons =,>,<.
 
 
 <b>Added by Allan C. Solomon</b> and modified by guich.
 @version 1.0 16 Aug 2000
 @author Allan C. Solomon
*/


public class Date 
{
 private int day = 0;
 private int month = 0;
 private int year = 0;
 private int dayofWeek = 0;
 private int startYear = 1983;
 
 public static final int SUNDAY = 0;
 public static final int MONDAY = 1;
 public static final int TUESDAY = 2;
 public static final int WEDNESDAY = 3;
 public static final int THRUSDAY = 4;
 public static final int FRIDAY = 5;
 public static final int SATURDAY = 6;
 
 public static final int JANUARY = 1;
 public static final int FEBRUARY = 2;
 public static final int MARCH = 3;
 public static final int APRIL = 4;
 public static final int MAY = 5;
 public static final int JUNE = 6;
 public static final int JULY = 7;
 public static final int AUGUST = 8;
 public static final int SEPTEMBER = 9;
 public static final int OCTOBER = 10;
 public static final int NOVEMBER = 11;
 public static final int DECEMBER = 12;
 
 public static final boolean FORWARD = true;
 public static final boolean BACKWARD = false; 

 /**
  Returns number of days in the passed month. 
  
  @param m - integer between 1 and 12. 
  @return integer containing number of days in passed month. 	
 */
 static private byte monthDays[] = new byte[]{0,31,28,31,30,31,30,31,31,30,31,30,31};
 /**
  Returns the string representation of the month passed
  
  
  @param m - integer between 1 and 12.
  @return string representation of month passed.
 */
 private static final String monthNames[] = {"","January","February","March","April","May","June","July","August","September","October","November","December"};
/**
 Constructs a Date object set to the current date.
*/
 public Date()
 {
   setToday();
 } 
 /**
 Constructs a Date object set to the passed int in the YYYYMMDD format
 
 @param sentDate - an integer in the YYYYMMDD format
 */
 public Date(int sentDate)
 {
   day = sentDate % 100;
   month = sentDate / 100 % 100;
   year = sentDate / 10000;
  
  if (doesDateExist())
	 setDayOfWeek();
  else
	 setToday();
 } 
 /**
 Constructs a Date object set to the passed day, month, and year.
 
 @param sentDay - an integer that must be between 1 and the last day in the month.
 @param sentMonth - an integer that must be between 1 and 12.
 @param sentYear - an integer that must be between 1983 and 2999.
 */
 public Date(int sentDay,int sentMonth, int sentYear)
 {
  day = sentDay;
  month = sentMonth;
  year = sentYear;
  if (doesDateExist())
	 setDayOfWeek();
  else
	 setToday();
 } 
 /**
  Constructs a Date object set to a passed string in the format specified in the palm preferences. The constructor auto-detects
  the seperator. If an invalid date is passed, sets to the current date;
  
  @param strDate- string that should have the format specified in the palm preferences. Note: does not have to be seperated by 
  the <b>'-'</b> character it can be seperated by any non-number.
 */
 public Date(String strDate)
 {
   this(strDate,Settings.dateFormat);
 } 
 /**
  Constructs a Date object set to a passed string in the format specified in the dateFormat parameter 
  (it must be one of the DATE_XXX constants). The constructor auto-detects
  the seperator. If an invalid date is passed, sets to the current date;
  
  @param strDate- string that should have the format specified in the palm preferences. Note: does not have to be seperated by 
  the <b>'-'</b> character it can be seperated by any non-number.
 */
 public Date(String strDate, byte dateFormat)
 {
 	StringBuffer nums[] = new StringBuffer[3];
   if (strDate != null && strDate.length() > 0)
   {
   	char []chars = strDate.toCharArray();
	  int j =0;
	 	nums[j] = new StringBuffer();
	 	
	  for (int i = 0; i < chars.length; i++)
	  	if ('0' <= chars[i] && chars[i] <= '9')
		 	nums[j].append(chars[i]);
		 else
		 if (++j == 3) {setToday(); return;} // too much separators
		 else
		 	nums[j] = new StringBuffer(); // initializes next StringBuffer
	  if (j != 2) {setToday(); return;} // must exist exactly 2 separators
   }
   int p0 = Convert.toInt(nums[0].toString());
   int p1 = Convert.toInt(nums[1].toString());
   int p2 = Convert.toInt(nums[2].toString());
   
   if (dateFormat == Settings.DATE_MDY) {day = p1; month = p0; year = p2;} else
   if (dateFormat == Settings.DATE_YMD) {day = p2; month = p1; year = p0;} 
   else /* DATE_DMY */ {day = p0; month = p1; year = p2;} 

   if (doesDateExist())
	  setDayOfWeek();
   else
	  setToday();
 } 
 /**
  Advances the date by a passed integer.
  
  @param numberDays - integer containing number of days that the date should change can be positive or 
  negitive
 */
 public void advance(int numberDays)
 {
  int numberofDays = getJulianDay()+numberDays;
  
  
  int y = 0;
  int i = startYear;
  
  while (numberofDays >= 366) 
  {
   if ((numberofDays == 366) && ((i % 4)==0)){break;}
   if ((i %4) == 0){numberofDays-=366;y++;} else {numberofDays-=365;y++;}
   i++;
  } 
  
  year = (startYear+y);
  i = 1;
  
  while (numberofDays > getDaysInMonth(i))
  {
	numberofDays-=getDaysInMonth(i);
	i++;
  }
   month = (i); 
   day = (numberofDays);   
   if (doesDateExist()){
	setDayOfWeek();
   }
   else {day=1;month=1;year=1983;}
 } 
 /**
  Advances the date to the beginning of the next month.
 */
 public void advanceMonth()
 {
  advanceMonth(FORWARD); 	
 } 
/**
  Advances the date to the beginning of the next or previous month.
  
  @param b - static variables FORWARD or BACKWARD instructs the method to either move to the next 
  or previous month
 */  
 public void advanceMonth(boolean b)
 {
  if (b){month++;if (month==13){year++;month=1;}}else{month--;if (month==0){year--;month=12;}}
  day=1;
  if (!doesDateExist()){day=1;month=1;year=1983;}
 } 
 /**
  Advances the date to the beginning of the next week.
 */
 public void advanceWeek()
 {
  advanceWeek(FORWARD);
 } 
 /**
  Advances the date to the beginning of the next or previous week.
  
  @param b - static variables FORWARD or BACKWARD instructs the method to either move to the next 
  or previous week
 */ 
 public void advanceWeek(boolean b)
 {
  advance(-1*getDayOfWeek());
  if (b){advance(7);}else{advance(-7);}
  if (!doesDateExist()){day=1;month=1;year=1983;}
 } 
 private boolean doesDateExist()
 { 
   if (0 <= year && year < 20) year += 2000; else
   if (20 <= year && year < 100) year += 1900;
   if ((day > 0) && (day <= getDaysInMonth() || day == 99)&&(month>=1)&&(month<=12)&&(year >=1983)&&(year <3000)){return true;} // guich: 99 is a valid day for VPFinance. sorry for this...
   else {return false;}
 } 
 /**
  Checks to see if the Date object passed occurs at the same time as the existing Date object.
  
  @param Date object to compare with existing.
  @return boolean stating whether or not it occurs at the same time as existing date.
 */
 public boolean equals(Date sentDate)
 {
  return (getDate().equals(sentDate.getDate()));
 } 
/** returns the index of the week day, where sunday = 0 and saturday = 6 
public int getDayOfWeek()
{
   int a = (year - 1582) * 365;
   int b = (int)((year - 1581) / 4);
   int c = (int)((year - 1501) / 100);
   int d = (int)((year - 1201) / 400);
   int e = (month - 1) * 31;
   int f = day;
   int g = (int)((month + 7) / 10);
   int h = (int)((month * 0.4f + 2.3f) * g);
   int i = (int)((1 / ((year % 4)+1)) * g);
   int j = (int)((1 / ((year % 100)+1)) * g);
   int k = (int)((1 / ((year % 400)+1)) * g);
   int l = a+b-c+d+e+f-h+i-j+k+5;
   int m = l % 7;
   return (int)((m > 0)?(m-1):6);
}*/

/** formats the date specified with the palm preferences, zero padded. */
public static String formatDate(int day, int month, int year)
{
   return formatDate(day,month,year,Settings.dateFormat);
}
/** formats the date specified with the dateFormat parameter, zero padded.*/
public static String formatDate(int day, int month, int year, byte dateFormat)
{
   int i1 = day, i2 = month, i3 = year;
   if (dateFormat == Settings.DATE_MDY)
   {
	  i1 = month; i2 = day; i3 = year;
   } else
   if (dateFormat == Settings.DATE_YMD)
   {
	  i1 = year; i2 = month; i3 = day;
   }
   
   return Convert.zeroPad(i1+"",2) + Settings.dateSeparator + Convert.zeroPad(i2+"",2) + Settings.dateSeparator + Convert.zeroPad(i3+"",2);
}
/** formats the day/month specified with the Settings.dateFormat, zero padded.*/
public String formatDayMonth()
{
   int i1 = day, i2 = month;
   if (Settings.dateFormat != Settings.DATE_DMY)
   {
	  i1 = month; i2 = day;
   } 
   
   return Convert.zeroPad(i1+"",2) + Settings.dateSeparator + Convert.zeroPad(i2+"",2);
}
 /**
 Returns the date in a string format.
 
 @return string representation of the date in the current palm settings
 */
 public String getDate()
 {
	return formatDate(day,month,year);
 } 
 /**
 Returns the date in a integer format.
 
 @return integer representation of the date (year * 10000) + (month *100) + day
 */
 public int getDateInt()
  {
   return (year * 10000)+ (month*100) + day;
  }  
/**
 Returns the day.
 
 @return integer value of day.
*/
 public int getDay()
 {
  return day;
 } 
/**
 Returns the day of week
 
 @return integer representation of day of week.  Integers refer to static constants of day of week.
*/
 public int getDayOfWeek()
 {
  return dayofWeek;
 } 
 private int getDaysFromMonth()
  {
   int numberofDays = 0;
   for(int i=1;i<month;i++)
   {
   numberofDays+=getDaysInMonth(i);	
   }
   return numberofDays;
  }  
 /**
  Returns number of days in the set month.
  
  @return integer containing number of days in set month. 	
 */
 public int getDaysInMonth()
 {
  return getDaysInMonth(month);
 } 
 public int getDaysInMonth(int month) // 1 <= month <= 12
 {
	if (month != 2) return monthDays[month];
	boolean leap = ((year%4 == 0) && ((year%100 != 0) || (year%400 == 0)));
	return leap?29:28;
 } 
 private int getJulianDay()
 {  
  int numberofDays = ((year-startYear)*365)+((year-startYear)/4);      
  if (year > 2000){numberofDays++;}
  numberofDays+=getDaysFromMonth();
  numberofDays+=day;
  return numberofDays;
 } 
/**
 Returns the month.
 
 @return integer value of the month.
*/
 public int getMonth()
 {
  return month;
 } 
 public static String getMonthName(int m)
 {
   if (JANUARY <= m && m <= DECEMBER)
	  return monthNames[m];
   return "";
 } 
 /**
  Calculates and returns the ordinal value of the week(1-52).
  
  @return integer representation of ordinal value of the week within the set year.
 */
 public int getWeek()
 {
  int w = (getJulianDay() / 7)%52;
  if (w==0){w=52;}
  return w;
 } 
/**
 Returns the year.
 
 @return integer value of the year.
*/
 public int getYear()
 {
  return year;
 } 
 /**
  Checks to see if the Date object passed occurs after the existing Date object.
  
  @param Date object to compare with existing.
  @return boolean stating whether or not it occurs after existing date.
 */
 public boolean isAfter(Date sentDate)
 {
  return (getDateInt() > sentDate.getDateInt()); 
 } 
 /**
  Checks to see if the Date object passed occurs before the existing Date object.
  
  @param Date object to compare with existing.
  @return boolean stating whether or not it occurs before existing date.
 */
 public boolean isBefore(Date sentDate)
 {
  return (getDateInt() < sentDate.getDateInt());
 } 
 private void setDayOfWeek()
 {
  dayofWeek = (getJulianDay() % 7)-2;
  if (dayofWeek==-1){dayofWeek=6;}
  else if (dayofWeek==-2){dayofWeek=5;}
 } 
 /** sets this date object to be the current day */ 
 public void setToday()
 {
   Time tTime = new Time();
   day = tTime.day;
   month = tTime.month;
   year = tTime.year;
   setDayOfWeek();
 } 
 /**
 Returns the date in a string format.
 
 @return string representation of the date in the current palm settings
 */
 public String toString()
 {
   return formatDate(day,month,year);
 } 
}