package waba.ui;

import waba.fx.*;
import waba.sys.Vm;
import waba.ui.*;

/**
 * TabPanel is a bar of tabs. (Very) Modified by Guich.
 * The panels are created automaticaly and switched when the user press the corresponding tab. 
 * Makes an sound when the tab is pressed.
 * <p>
 * Here is an example showing a tab bar being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    TabPanel tp;
 * 
 *    public void onStart()
 *    {
 *    	String tabs[] = new String[]{"One","Two"};
 *    	TabBar tb = new TabBar(tabs);
 *    	tb.setRect(10, 10, 80, 30);
 *       tb.setGaps(2,2,2,2);
 *       tb.getPanel(0).add(new Edit());
 *       tb.getPanel(1).add(new Label("Hi!"));
 *    	add(tabBar);
 * 	}
 *
 * 	public void onEvent(Event event)
 * 	{
 *    	if (event.type == ControlEvent.PRESSED && event.target == tp)
 *    	{
 *       	int activeIndex = tp.getActiveTab();
 *       	... handle tab one being pressed
 *    	}
 * 	}
 * }
 * </pre>
 */

public class TabPanel extends Container
{
	int activeIndex=-1;
	String []tabCaptions;
	public JustifiedContainer panels[];
   int gapL,gapT,gapB,gapR;
   int count;
   int tabH;
   int xx;

	/** Constructs a tab bar control. */
	public TabPanel(String []tabCaptions)
	{
	   this.tabCaptions = tabCaptions;
	  count = tabCaptions.length;
	  panels = new JustifiedContainer[count];
	  for (int i =0; i < count; i++)
	  	panels[i] = new JustifiedContainer();
	}
	private int drawOrHitCheck(Graphics g, int hitX, int hitY)
	{
		xx = 2;
	   boolean draw = g != null;
	   for (int i =0; i < tabCaptions.length; i++)
		{
			String label = tabCaptions[i];
			int width = fm.getTextWidth(label); // + 6

			int x1 = xx;
			int x2 = xx + width + 4;
			int y1 = 2;
			int y2 = tabH - 1;
			if (i == activeIndex)
			{
				x1 -= 1;
				x2 += 1;
				y1 -= 1;
				y2 += 1;
				if (draw)
				{
					if (x1 != 0)
						g.drawLine(0, tabH, x1 - 1, tabH);
					if (x2 != this.width - 1)
						g.drawLine(x2 + 1, tabH, this.width - 1, tabH);
				}
			}

			if (draw)
			{
				g.setColor(0, 0, 0);
				g.drawText(label, xx + 3, y1 + 1);
			}

			boolean hasLeft = true;
			boolean hasRight = true;
			if (i > 0 && i-1 == activeIndex)
				hasLeft = false;
			else if (i < tabCaptions.length-1 && i+1 == activeIndex)
				hasRight = false;

			if (draw)
				drawTab(g, hasLeft, hasRight, x1, y1, x2, y2);
			else
			{
				if (!hasLeft)
					x1 += 2;
				else if (!hasRight)
					x2 -= 2;
				if (hitX >= x1 && hitX <= x2 && hitY >= y1 && hitY <= y2)
					return i;
			}
			xx += width + 3 + 1;
		}
		return -1;
	}
	private void drawTab(Graphics g, boolean hasLeft, boolean hasRight,int x1, int y1, int x2, int y2)
	{
		// left side and hatch dot
		if (hasLeft)
		{
			g.drawLine(x1, y2 - 1, x1, y1 + 2);
			g.drawLine(x1 + 1, y1 + 1, x1 + 1, y1 + 1);
		}

		// top line
		g.drawLine(x1 + 2, y1, x2 - 2, y1);

		// right hatch dot and side
		if (hasRight)
		{
			g.drawLine(x2 - 1, y1 + 1, x2 - 1, y1 + 1);
			g.drawLine(x2, y1 + 2, x2, y2 - 1);
		}
	}
	public int getActiveTab()
	{
		return activeIndex;
	}
   /** returns the JustifiedContainer for tab <i> */
   public JustifiedContainer getPanel(int i)
   {
	  return panels[i];
   }   
   /** used internally. resizes all the containers. */
   protected void onBoundsChanged()
   {
	  tabH = fm.getHeight() + 4;
	  for (int i =0; i < count; i++)
	  	panels[i].setRect(gapL,gapT+tabH,width-gapL-gapR,height-gapT-gapB-tabH);
	  setActiveTab(0);   
   }   
	/** Called by the system to pass events to the tab bar control. */
	public void onEvent(Event event)
	{
		if (event.type == PenEvent.PEN_DOWN && event.target == this)
		{
			PenEvent pe = (PenEvent)event;
		 if (pe.y < tabH)
		 {
				int tab = drawOrHitCheck(null, pe.x, pe.y);
				if (tab != -1 && tab != activeIndex)
				{
				   Sound.tone(300,50);
					setActiveTab(tab);
				}
		 }            
		}
	}
	/** Called by the system to draw the tab bar. */
	public void onPaint(Graphics g)
	{
		g.setColor(0, 0, 0);
		drawOrHitCheck(g, 0, 0);
		g.setColor(0, 0, 0);
		int y = tabH;
		int w = width-1;
	  int h = height-1;
		g.drawLine(0,y,0,h);
		g.drawLine(xx+2,y,w,y);
		g.drawLine(w,y,w,h);
		g.drawLine(0,h,w,h);
	}
	/**
	 * Sets the currently active tab. A PRESSED event will be posted to
	 * the given tab if it is not the currently active tab. the panels will be switched.
	 */
	public void setActiveTab(int tab)
	{
		if (tab != activeIndex)
		{
			if (activeIndex != -1) remove(panels[activeIndex]);
			activeIndex = tab;
		 add(panels[activeIndex]);
			repaint();
			postEvent(new ControlEvent(ControlEvent.PRESSED, this));
		}
	}
   /** sets gaps between the containers and the TabPanel. */
   public void setGaps(int gapL, int gapR, int gapT, int gapB)
   {
   	this.gapL = gapL;
	  this.gapR = gapR;
	  this.gapT = gapT;
	  this.gapB = gapB;
   }   
}