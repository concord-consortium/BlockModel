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
import waba.sys.Vm;

/**
 * Button is a push button control.
 * <p>
 * Here is an example showing a push button being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * Button pushB;
 *
 * public void onStart()
 *  {
 *  pushB = new Button("Push me");
 *  pushB.setRect(10, 10, 80, 30);
 *  add(pushB);
 *  }
 * 
 * public void onEvent(Event event)
 *  {
 *  if (event.type == ControlEvent.PRESSED &&
 *      event.target == pushB)
 *   {
 *   ... handle pushB being pressed
 * </pre>
 */

public class Button extends Control
{
String text;
//Font font; removed by guich
boolean armed;

/** Creates a button displaying the given text. */
public Button(String text)
	{
	this.text = text;
	name = "Button" + controlCount++;
	//this.font = MainWindow.defaultFont; removed by guich
	}

/** Sets the text that is displayed in the button. */
public void setText(String text)
	{
	this.text = text;
	repaint();
	}

/** Gets the text displayed in the button. */
public String getText()
	{
	return text;
	}

/** returns the preffered width of this control. added by guich */
public int getPreferredWidth()
{
   return fm.getTextWidth(text)+6;
}

/** returns the preffered height of this control. added by guich */
public int getPreferredHeight()
{
   return fm.getHeight()+2;
}
/** Called by the system to pass events to the button. */
public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		{
		armed = true;
		repaint();
		}
	else if (event.type == PenEvent.PEN_UP)
		{
		armed = false;
		repaint();
		PenEvent pe = (PenEvent)event;
		if (0 <= pe.x && pe.x < this.width && 0 <= pe.y && pe.y < this.height)
			postEvent(new ControlEvent(ControlEvent.PRESSED, this));
		}
	else if (event.type == PenEvent.PEN_DRAG)
		{
		PenEvent pe = (PenEvent)event;
		boolean lArmed = false;
		if (0 <= pe.x && pe.x < this.width && 0 <= pe.y && pe.y < this.height)
			lArmed = true;
		if (armed != lArmed)
			{
			armed = lArmed;
			repaint();
			}
		}
	}

public static void drawButton(Graphics g, boolean armed, int width, int height)
	{
	boolean isColor = Vm.isColor();
	int x2 = width - 1;
	int y2 = height - 1;
	if (!isColor)
		{
		// draw top, bottom, left and right lines
		g.setColor(0, 0, 0);
		g.drawLine(3, 0, x2 - 3, 0);
		g.drawLine(3, y2, x2 - 3, y2);
		g.drawLine(0, 3, 0, y2 - 3);
		g.drawLine(x2, 3, x2, y2 - 3);

		if (armed)
			g.fillRect(1, 1, width - 2, height - 2);
		else
			{
			// draw corners (tl, tr, bl, br)
			g.drawLine(1, 1, 2, 1);
			g.drawLine(x2 - 2, 1, x2 - 1, 1);
			g.drawLine(1, y2 - 1, 2, y2 - 1);
			g.drawLine(x2 - 2, y2 - 1, x2 - 1, y2 - 1);

			// draw corner dots
			g.drawLine(1, 2, 1, 2);
			g.drawLine(x2 - 1, 2, x2 - 1, 2);
			g.drawLine(1, y2 - 2, 1, y2 - 2);
			g.drawLine(x2 - 1, y2 - 2, x2 - 1, y2 - 2);
			}

		}
	else
		{
		// top, left
		if (armed)
			g.setColor(0, 0, 0);
		else
			g.setColor(255, 255, 255);
		g.drawLine(0, 0, x2 - 1, 0);
		g.drawLine(0, 0, 0, y2 - 1);

		// top, left shadow
		if (armed)
			{
			g.setColor(130, 130, 130);
			g.drawLine(1, 1, x2 - 1, 1);
			g.drawLine(1, 1, 1, y2 - 1);
			}

		// bottom, right
		if (armed)
			g.setColor(255, 255, 255);
		else
			g.setColor(0, 0, 0);
		g.drawLine(0, y2, x2, y2);
		g.drawLine(x2, y2, x2, 0);

		// bottom, right shadow
		if (!armed)
			{
			g.setColor(130, 130, 130);
			g.drawLine(1, y2 - 1, x2 - 1, y2 - 1);
			g.drawLine(x2 - 1, y2 - 1, x2 - 1, 1);
			}
		}
	}

/** Called by the system to draw the button. it cuts the text if the button is too small. */
public void onPaint(Graphics g)
{
	drawButton(g, armed, this.width, this.height);

	// draw label
	if (armed && !Vm.isColor())
		g.setColor(255, 255, 255);
	else
		g.setColor(0, 0, 0);
	//g.setFont(font);  removed by guich
	//FontMetrics fm = getFontMetrics(font); removed by guich
	int x = (this.width - fm.getTextWidth(text)) / 2;
	int y = (this.height - fm.getHeight()) / 2;
	if (armed)
		{
		x++;
		y++;
		}
	g.setClip(2,1,width-4,height-2); // guich@101: cut text if button is too small
	g.drawText(text, x, y);
	}
public String toString()
{
   return name + ", text: "+text;;
}
}
