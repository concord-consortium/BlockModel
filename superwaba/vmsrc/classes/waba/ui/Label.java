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

package waba.ui;

import waba.fx.*;
import waba.sys.*;

/**
 * Label is a text label control. It supports multiline, but you need to separate the text with |.
 * <p>
 * Here is an example showing a label being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onStart()
 *  {
 *  Label label = new Label("Value:");
 *  label.setRect(10, 10, 80, 30);
 *  add(label);
 *  }
 * </pre>
 */

public class Label extends Control
{
/** Constant for left alignment. */
public static final int LEFT = 0;
/** Constant for center alignment. */
public static final int CENTER = 1;
/** Constant for right alignment. */
public static final int RIGHT = 2;

String text;
//Font font; removed by guich
int align;
private static final String[] emptyStringArray = {""};
String []lines = emptyStringArray;
private int linesPerPage,currentLine;
boolean invert;

/** Creates a label displaying the given text with left alignment. supports inverted text, multiple lines and is scrollable */
public Label(String text)
{
   this(text, LEFT);
}

/**
 * Creates a label displaying the given text with the given alignment.
 * @param text the text displayed
 * @param align the alignment
 * @see #LEFT
 * @see #RIGHT
 * @see #CENTER
 */
public Label(String text, int align)
{
   this.text = text;
   this.align = align;
   name = "Label" + controlCount++;
   lines = parseText(text,'|');
   currentLine = 0;
   //this.font = MainWindow.defaultFont; removed by guich
}

/** if invert is true, the background is filled with black and the letters are drawn white */
public void setInvert(boolean on)
{
   invert = on;
}
/** Sets the text that is displayed in the label. */
public void setText(String text)
{
   this.text = text;
   lines = parseText(text,'|');
   currentLine = 0;
   repaint();
}

/** Gets the text that is displayed in the label. */
public String getText()
{
   return text;
}
   
/** returns the parsed text */
public static String[] parseText(String text, char delim)
{
   String []ret;
   char []chars = text.toCharArray();
   int n = chars.length;
   if (n > 0)
   {
      int sep = 1;
      for (int i=0; i < n; i++) // count how many separators are in this text
         if (chars[i] == delim)
            sep++;
      if (sep > 1) // is this a multiline text?
      {
         ret = new String[sep];
         int last = 0;
         for (int i=0,j=0; i <= n; i++)
            if (i==n || chars[i] == delim)
            {
               ret[j++] = new String(chars,last,i-last);
               last = i+1;
            }         
      } else ret = new String[]{text}; // only one line of text
   } else ret = emptyStringArray;
   return ret;
}

/** returns the preffered width of this control. added by guich */
public int getPreferredWidth()
{
   int w = 0;
   for (int i =0; i < lines.length; i++)
      w = waba.sys.Convert.max(w, fm.getTextWidth(lines[i]));
   return w+(invert?2:0);
}

/** returns the preffered width of this control. added by guich */
public int getPreferredHeight()
{
   return fm.getHeight()*lines.length + (invert?1:0); // if inverted, make sure the string is surrounded by the black box
}

protected void onBoundsChanged()
{
   linesPerPage = height / fm.getHeight();
   if (linesPerPage < 1) linesPerPage = 1;
}

/** scroll one page. returns true if success, false if no scroll possible */
public boolean scroll(boolean down)
{
   if (lines.length > linesPerPage)
   {
      int lastLine = currentLine;
      if (down)
      {
         if (currentLine+linesPerPage < lines.length)
            currentLine += linesPerPage;
      }
      else
      {
         currentLine -= linesPerPage;
         if (currentLine < 0) 
            currentLine = 0;
      }
      if (lastLine != currentLine) 
      {
         repaint();
         return true;
      }
   }
   return false;
}

/** Called by the system to draw the button. */
public void onPaint(Graphics g)
{
   // draw label
   g.setColor(0, 0, 0);
   if (invert)
   {
      g.fillRect(0,0,width,height);
      g.setColor(255,255,255);
   } 
   //g.setFont(font); removed by guich
   //FontMetrics fm = getFontMetrics(font); removed by guich
   if (text.length() > 0)
   {
      int y = (this.height - fm.getHeight()*linesPerPage) / 2; // center on y (if necessary)
      int n = Convert.min(currentLine+linesPerPage, lines.length);
      int xx = invert?1:0;
      for (int i =currentLine; i < n; i++,y+=fm.getHeight())
      {
         int x = 0;
         int ww = fm.getTextWidth(lines[i]);
         if (align == CENTER)
            x = (width - ww) / 2;
         else 
         if (align == RIGHT)
            x = width - ww;
         
         g.drawText(lines[i], xx+x, y);
      }
   }
}
public String toString()
{
   return name;
}
}
