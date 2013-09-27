package waba.ui;

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

import waba.fx.*;
import waba.sys.*;

/**
 * Control is the base class for user-interface objects.
 */

public class Control
{
/** constant used in params width and height in setRect. added by guich */
public static final int PREFERRED = -10000;
/** constant used in param x in setRect. added by guich */
public static final int LEFT      = -10001;
/** constant used in params x and y in setRect. added by guich */
public static final int CENTER    = -10002;
/** constant used in param x in setRect. added by guich */
public static final int RIGHT     = -10003;
/** constant used in param y in setRect. added by guich */
public static final int TOP       = -10004;
/** constant used in param y in setRect. added by guich */
public static final int BOTTOM    = -10005;
/** constant used in params width and height in setRect. added by guich */
public static final int FILL      = -10006;
/** The control's x location */
protected int x;
/** The control's y location */
protected int y;
/** The control's width */
protected int width;
/** The control's height */
protected int height;
/** The parent of the control. */
protected Container parent;
/** The control's next sibling. */
protected Control next;
/** The control's previous sibling. */
protected Control prev;
/** True if the control is enabled (accepts events) or false if not */
protected boolean enabled=true;
/** the font used by the control. added by guich */
protected Font font;
/** the fontMetrics corresponding to the controls font. added by guich */
public FontMetrics fm;
/** used in the toString method. guich@102 */
static protected int controlCount;
/** used in the toString method. guich@102 */
protected String name="";

private boolean visible=true;
private int lastW,lastH;

/** creates the font for this control as the same font of the MainWindow. added by guich */
protected Control()
{
   setFont(MainWindow.defaultFont);
}
/**
 * Adds a timer to a control. Each time the timer ticks, a TIMER
 * event will be posted to the control. The timer does
 * not interrupt the program during its execution at the timer interval,
 * it is scheduled along with application events. The timer object
 * returned from this method can be passed to removeTimer() to
 * remove the timer. Under Windows, the timer has a minimum resolution
 * of 55ms due to the native Windows system clock resolution of 55ms. 
 *
 * @param millis the timer tick interval in milliseconds
 * @see ControlEvent
 */
public Timer addTimer(int millis)
   {
   MainWindow win = MainWindow.getMainWindow();
   return win.addTimer(this, millis);
   }   
/**
 * Returns true if the given x and y coordinate in the parent's
 * coordinate system is contained within this control.
 */
public boolean contains(int x, int y)
   {
   int rx = this.x;
   int ry = this.y;
   if (x < rx || x >= rx + this.width || y < ry || y > ry + this.height)
	  return false;
   return true;
   }   
/**
 * Creates a Graphics object which can be used to draw in the control.
 * This method finds the surface associated with the control, creates
 * a graphics assoicated with it and translates the graphics to the
 * origin of the control. It does not set a clipping rectangle on the
 * graphics.
 */
public Graphics createGraphics()
   {
   int x = 0;
   int y = 0;
   Control c = this;
   while (!(c instanceof Window))
	  {
	  x += c.x;
	  y += c.y;
	  c = c.parent;
	  if (c == null)
		 return null;
	  }
   Window win = (Window)c;
   Graphics g = new Graphics(win);
   g.translate(x + win.x, y + win.y); // guich@102: translate into win coords
   g.setFont(font); // added by guich
   return g;
   }   
/** returns the absolute coordinates of this control relative to the MainWindow. added by guich */
public Rect getAbsoluteRect() // guich@102: changed name from getRelativeRect to getAbsoluteRect.
{
   Rect r = getRect();
   Control c = parent;
   while (c != null)
   {
	  r.x += c.x;
	  r.y += c.y;
	  c = c.parent;
   }
   return r;
}
/** Returns the font metrics for a given font. */
public FontMetrics getFontMetrics(Font font)
   {
   MainWindow win = MainWindow.getMainWindow();
   return win.getFontMetrics(font);
   }   
/** Returns the next child in the parent's list of controls. */
public Control getNext()
   {
   return next;
   }   
/** Returns the control's parent container. */
public Container getParent()
   {
   return parent;
   }   
/** returns the preffered height of this control. added by guich */
public int getPreferredHeight()
{
   return fm.getHeight();
}
/** returns the preffered width of this control. added by guich */
public int getPreferredWidth()
{
   return 30;
}
/**
 * Returns a copy of the control's rectangle. A control's rectangle
 * defines its location and size.
 */
public Rect getRect()
   {
   return new Rect(this.x, this.y, this.width, this.height);
   }   
/** returns if this control can or not accept events */
public boolean isEnabled()
{
   return this.enabled;
}
/** returns true if this control is visible, false otherwise */
public boolean isVisible()
{
   return visible;
}
/** called after an setRect. added by guich */
protected void onBoundsChanged()
{
}
/**
 * Called to process key, pen, control and other posted events.
 * @param event the event to process
 * @see Event
 * @see KeyEvent
 * @see PenEvent
 */
public void onEvent(Event event)
   {
   }   
/**
 * Called to draw the control. When this method is called, the graphics
 * object passed has been translated into the coordinate system of the
 * control and the area behind the control has
 * already been painted. The background is painted by the top-level
 * window control.
 * @param g the graphics object for drawing
 * @see Graphics
 */
public void onPaint(Graphics g)
   {
   }   
/** called after the window has finished a paint. */
protected void onWindowPaintFinished()
{
}
/**
 * Posts an event. The event pass will be posted to this control
 * and all the parent controls of this control (all the containers
 * this control is within).
 * @see Event
 */
public void postEvent(Event event)
{
   if (!enabled) return; // added by guich
   
   Control c;

   c = this;
   while (c != null)
	  {
	  c.onEvent(event);
	  c = c.parent;
	  }
}
/**
 * Removes a timer from a control. True is returned if the timer was
 * found and removed and false is returned if the timer could not be
 * found (meaning it was not active).
 */
public boolean removeTimer(Timer timer)
   {
   MainWindow win = MainWindow.getMainWindow();
   return win.removeTimer(timer);
   }   
/** Redraws the control. */
public void repaint()
   {
   int x = 0;
   int y = 0;
   Control c = this;
   while (!(c instanceof Window))
	  {
	  x += c.x;
	  y += c.y;
	  c = c.parent;
	  if (c == null)
		 return;
	  }
   Window win = (Window)c;
   win.damageRect(x, y, this.width, this.height); 
   }   
/** Redraws the control immediately. */
public void repaintNow()
{
   onPaint(createGraphics());
}
/** sets if this control can or not accept events */
public void setEnabled(boolean enabled)
{
   this.enabled = enabled;
}
/** sets the font of this conrol. added by guich */
public void setFont(Font font)
{
   this.font = font;
   this.fm = getFontMetrics(font);
}
/** Sets or changes a control's position and size. */
public void setRect(int x, int y, int width, int height)
{
   if (parent != null || this instanceof Window)   
	  repaint();
   // added by guich
   int pw = parent==null?160:parent.width;
   int ph = parent==null?160:parent.height;
   if (width == PREFERRED) width = getPreferredWidth(); 
   if (height == PREFERRED) height = getPreferredHeight(); 
   if (x == LEFT) x = 0; else
   if (x == RIGHT) x = pw-width; else
   if (x == CENTER) x = (pw-width)/2;
   if (y == TOP) y = 0; else
   if (y == BOTTOM) y = ph-height; else
   if (y == CENTER) y = (ph-height)/2;   
   if (width == FILL) width = pw - x;
   if (height == FILL) height = ph - y;
   ////
   this.x = x;
   this.y = y;
   if (visible)
   {
	  this.width = width;
	  this.height = height;
   }
   else
   {
	  this.lastW = width;
	  this.lastH = height;
   }
   if (visible && parent != null || this instanceof Window)
	  repaint();
   onBoundsChanged(); // added by guich
}
/** Sets or changes a control's position and size. */
public void setRect(Rect r)
{
   setRect(r.x,r.y,r.width,r.height);
}
/** shows or "hides" this control. the "hide" works setting the control's size to zero. it remains attached to its container. unhidding restores the original size. you can change the controls size when hided if you want. calls repaint. */
public void setVisible(boolean visible)
{
   if (visible != this.visible)
   {  
	  this.visible = visible;
	  repaint();
	  if (visible)
	  {
		 width = lastW;
		 height = lastH;
	  }
	  else
	  {
		 lastW = width;
		 lastH = height;
		 width = height = 0; 
	  }
	  repaint();
   }
}
}