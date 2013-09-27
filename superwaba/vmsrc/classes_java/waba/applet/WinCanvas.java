package waba.applet;

/*
Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.

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

import waba.ui.*;

public class WinCanvas extends java.awt.Canvas
{
MainWindow win;

public WinCanvas(MainWindow win)
	{
	this.win = win;
	}
public static int actionKeyValue(int action)
	{
	int key = 0;
	switch (action)
		{
		case java.awt.Event.PGUP:       key = IKeys.PAGE_UP; break;
		case java.awt.Event.PGDN:       key = IKeys.PAGE_DOWN; break;
		case java.awt.Event.HOME:       key = IKeys.HOME; break;
		case java.awt.Event.END:        key = IKeys.END; break;
		case java.awt.Event.UP:         key = IKeys.UP; break;
		case java.awt.Event.DOWN:       key = IKeys.DOWN; break;
		case java.awt.Event.LEFT:       key = IKeys.LEFT; break;
		case java.awt.Event.RIGHT:      key = IKeys.RIGHT; break;
		case java.awt.Event.INSERT:     key = IKeys.INSERT; break;
		case java.awt.Event.ENTER:      key = IKeys.ENTER; break;
		case java.awt.Event.TAB:        key = IKeys.TAB; break;
		case java.awt.Event.BACK_SPACE: key = IKeys.BACKSPACE; break;
		case java.awt.Event.ESCAPE:     key = IKeys.ESCAPE; break;
		case java.awt.Event.DELETE:     key = IKeys.DELETE; break;
      // guich@120 - emulate more keys 
		case java.awt.Event.F1:         key = IKeys.HARD1; break;
		case java.awt.Event.F2:         key = IKeys.HARD2; break;
		case java.awt.Event.F3:         key = IKeys.HARD3; break;
		case java.awt.Event.F4:         key = IKeys.HARD4; break;
		case java.awt.Event.F5:         key = IKeys.COMMAND; break;
		case java.awt.Event.F6:         key = IKeys.MENU; break;
		case java.awt.Event.F7:         key = IKeys.CALC; break;
		case java.awt.Event.F8:         key = IKeys.FIND; break;
		case java.awt.Event.F11:        key = IKeys.KEYBOARD_ABC; break;
		case java.awt.Event.F12:        key = IKeys.KEYBOARD_123; break;
		}
	return key;
	}
public boolean handleEvent(java.awt.Event event)
	{
	int type = 0;
	int key = 0;
	int x = 0;
	int y = 0;
	int modifiers = 0;
	if ((event.modifiers & java.awt.Event.SHIFT_MASK) > 0)
		modifiers |= IKeys.SHIFT;
	if ((event.modifiers & java.awt.Event.CTRL_MASK) > 0)
		modifiers |= IKeys.CONTROL;
	boolean doPostEvent = false;
	switch (event.id)
		{
		//case java.awt.Event.MOUSE_MOVE: - guich@120: no more mousemove event 
		case java.awt.Event.MOUSE_DRAG:
			type = PenEvent.PEN_MOVE;
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.MOUSE_DOWN:
			type = PenEvent.PEN_DOWN;
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.MOUSE_UP:
			type = PenEvent.PEN_UP;
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.KEY_PRESS:
			type = KeyEvent.KEY_PRESS;
			key = keyValue(event.key, modifiers);
			doPostEvent = true;
			break;
		case java.awt.Event.KEY_ACTION:
			{
			key = actionKeyValue(event.key);
			if (key != 0)
				{
				type = KeyEvent.KEY_PRESS;
				doPostEvent = true;
				}
			break;
			}
		}
	if (doPostEvent)
		{
		int timestamp = (int)event.when;
		synchronized(Applet.uiLock)
			{
			win._postEvent(type, key, x, y, modifiers, timestamp);
			}
		}
	return super.handleEvent(event);
	}
public static int keyValue(int key, int mod)
	{
	switch (key)
		{
		case 8:
			key = IKeys.BACKSPACE;
			break;
		case 10:
			key = IKeys.ENTER;
			break;
		case 127:
			key = IKeys.DELETE;
			break;
		}
	return key;
	}
public void paint(java.awt.Graphics g)
	{
	   if (!Applet.currentApplet.started) // guich@120 - only call onStart after the canvas is valid
	      Applet.currentApplet.startApp();

	   java.awt.Rectangle r = null;
	   // getClipRect() is missing in the Kaffe distribution for Linux
	   try { r = g.getClipBounds(); }
	   catch (NoSuchMethodError e) { r = g.getClipRect(); }
	   synchronized(Applet.uiLock)
	   {   	   
	   	win.repaintParents();
		   win._doPaint(r.x, r.y, r.width, r.height);
	   }
	}
public void update(java.awt.Graphics g)
	{
	paint(g);
	}
}