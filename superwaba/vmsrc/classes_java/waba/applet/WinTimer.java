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

import waba.ui.MainWindow;

public class WinTimer extends Thread
{
MainWindow win;
private int interval = 0;
private boolean shouldStop = false;

public WinTimer(MainWindow win)
	{
	this.win = win;
	start();
	}
public void run()
	{
	while (!shouldStop)
		{
		boolean doTick = true;
		int millis = interval;
		if (millis <= 0)
			{
			// NOTE: Netscape navigator doesn't support interrupt()
			// so we sleep here less than we would normally need to
			// (1 second) if we're not doing anything to check if
			// the timer should start in case interrupt didn't work
			millis = 1 * 1000;
			doTick = false;
			}
		try { sleep(millis); }
		catch (InterruptedException e) { doTick = false; }
		if (doTick)
			synchronized(Applet.uiLock)
				{
				win._onTimerTick();
				}
		}
	}
public void setInterval(int millis)
	{
	interval = millis;
	interrupt();
	}
public void stopGracefully()
	{
	// NOTE: It's not a good idea to call stop() on threads since
	// it can cause the JVM to crash.
	shouldStop = true;
	interrupt();
	}
}