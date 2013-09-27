/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 08/05/1999  New      0.9.0    Stefan Kellner

 * Class created

 *

 ****************************************************************************/



package org.concord.waba.extra.ui;



import waba.fx.*;

import waba.ui.*;

import waba.util.Vector;

import extra.ui.*;

import org.concord.waba.extra.event.*;



/**

 * This is a standard palm menu. Use this Menu with MenuBar. A large amaount of

 * code was stolen from List.

 * @author     <A HREF="mailto:kellner@no-information.de">Stefan Kellner</A>,

 * @version    1.0.0 08 May 1999

 */

public class Menu extends Control

{

  MenuBar menubar;

  String name = null;

  boolean dropped=false;

  Popup popup=null;

  Vector options;

  int selected=0;

  int oldselected=0;

  int textHeight;

  int expandedWidth=0;

  int numDisplayed=0;

  int scrollOffset=0;

  int maxScrollOffset=0;

  boolean scrollUp;

  Timer scrollTimer=null;

  boolean clicked;

  FontMetrics fm=null;

  ActionListener	listener;//dima



  /**

   * Construct a new empty menu of default size

   */

  public Menu()

  {

    this(new Vector());

  }

  public Menu(String name)

  {

    this(new Vector());

    this.name = name;

  }



  /**

   * Construct a new menu

   * @param options an array of the items available

   */

  public Menu(Vector items)

  {

    options = items;

  }



  /**

   * Construct a new menu with the given items. The first item defines

   * the name of the menu. To insert a separator simply add an option 

   * "-" to the menu.

   * @param options an array of the menu items available

   */

  public Menu(String[] items)

  {

    this();

    for(int i=0,size=items.length;i<size;i++)

      add(items[i]);      

  }



	/**

	 * Sets the menubar that this menu is attached to

	 */

	void setMenuBar(MenuBar menubar)

	{

	  this.menubar=menubar;

	}

    public void setName(String name) {

	    this.name = name;

    }

    public String getName() {

        return name;

    }



  /**

   * Add a new item to the end of the menu. The first item defines

   * the name of the menu. To insert a separator simply add an option 

   * "-" to the menu.

   * @param name the item to add

   */

  public void add(String nameItem)

  {

    options.add(nameItem);

    int w;

    if (fm!=null&&(w=fm.getTextWidth(nameItem))>expandedWidth)

      expandedWidth=w;

  }



  /**

   * Gets the text of the currently selected item

   * @returns the selected text

   */

  public String getSelected()

  {

    return (String)options.get(selected);

  }



  /**

   * Get the index of the currently selected item

   * @returns the index

   */

  public int getSelectedIndex()

  {

    return selected;

  }



  /**

   * Sets the currently selected index

   * @param i the index of the item to select

   */

  public void setSelectedIndex(int i)

  {    

    if (i>=0&&i<options.getCount())

    {

      selected=i;

      oldselected=i;

      postEvent(new ControlEvent(ControlEvent.PRESSED,this));

      repaint();

    }

  }



  int getPreferredWidth(FontMetrics fm)

  {

    return 0;

  }



  int getPreferredHeight(FontMetrics fm)

  {

    return 0;

  }



  public boolean calcSizes()

  {

    int pos=1;

    

    if (fm==null)    

      fm=getFontMetrics(MainWindow.defaultFont);

    if (fm==null)

      return false;

    int size=options.getCount();

    expandedWidth=0;

    int t;

    for(int i=0;i<size;i++)

      if ((t=fm.getTextWidth((String)options.get(i)))>expandedWidth)

        expandedWidth=t;

    return true;

  }

  

  public void onPaint(Graphics g)

  {

    if (fm==null)

      calcSizes();

    if (popup != null)

    {

      drawList(g);

      g.setColor(0,0,0);

      g.drawLine(0,1,0,height-3);

      g.drawLine(width-2,1,width-2,height-2);

      g.drawLine(width-1,2,width-1,height-3);  

      g.drawLine(0,0,width-2,0);

    }

  }

  

  public void drawList(Graphics g)

  {

    g.setColor(255,255,255);

    g.fillRect(1,1,width-3,height-3);

    g.setColor(0,0,0);

    for(int i=0;i<numDisplayed;i++)

    {

      if ((i+scrollOffset==selected)&!((String)options.get(i+scrollOffset)).equals("-"))

      {

        g.fillRect(1,i*textHeight+1,width-3,textHeight);

       	g.setColor(255,255,255);

      }

      if (((String)options.get(i+scrollOffset)).equals("-"))

      {

        for(int k = 0; k < width-3; k += 2)

            g.drawLine(k,i*textHeight + (textHeight /2),k, i*textHeight + (textHeight /2));   

      } else

          g.drawText((String)options.get(i+scrollOffset),3,i*textHeight+1);

      if (i+scrollOffset==selected)

        g.setColor(0,0,0);

    }

  }



  /**

   * Show this menu up

   * @param x the x-position to start the menu

   */

  public void show(int x)

  {

    if (popup==null)

    {

        if (fm==null)    

            fm=getFontMetrics(MainWindow.defaultFont);

        textHeight = fm.getHeight();

        calcSizes();

        numDisplayed=options.getCount();

        scrollOffset=0;

        Rect mr=MainWindow.getMainWindow().getRect();

        if (numDisplayed*textHeight>mr.height)

            numDisplayed=mr.height/textHeight;

        maxScrollOffset=options.getCount()-numDisplayed;

        popup=new Popup(this);

		Rect r = MainWindow.getMainWindow().getRect();

		int mh = (textHeight*numDisplayed+3);

		popup.popup(x,r.height - 14 - mh,expandedWidth+10,mh);

        oldselected=selected=-1;

        clicked=false;

    }

  }



  /**

   * Hide the menu

   */

  public void hide()

  {

    if (popup!=null)

    {

        if (scrollTimer!=null)

        {

            removeTimer(scrollTimer);

            scrollTimer=null;

        }

        popup.unpop();

        popup=null;

    }

  }

  

  /**

   * Process pen and key events to this component

   * @param event the event to process

   */

  public void onEvent(Event event)

  {

    if (event.type==ControlEvent.FOCUS_OUT)

    {

		  if (MainWindow.getMainWindow() instanceof ExtraMainWindow &&

			  ((ExtraMainWindow)MainWindow.getMainWindow()).newFocus!=menubar)

				menubar.hide();

    }

    else

    if (event.type==ControlEvent.TIMER)

    {

      scrollOffset+=(scrollUp?-1:1);

      selected+=(scrollUp?-1:1);

      if (scrollOffset<=0||scrollOffset>=maxScrollOffset)

        removeTimer(scrollTimer);

      drawList(createGraphics());

      return;

    }

    else

    if (event instanceof PenEvent)

    {

      

      int px=((PenEvent)event).x;

      int py=((PenEvent)event).y;



      switch (event.type)

      {

        case PenEvent.PEN_DOWN: 

        case PenEvent.PEN_DRAG:   

          if (popup!=null)

          {

            clicked=true;

            int position=py/textHeight;

						if (py<0)

						{

  						MainWindow.getMainWindow().setFocus(menubar);

							return;

						}

            if ((py<=5&&scrollOffset>0)||(py>=height-5&&scrollOffset<maxScrollOffset))

            {

              if (scrollTimer==null)

              {

                scrollUp=(py<=5);

                scrollTimer=addTimer(500);

              }

              return;

            }

            else

            if (scrollTimer!=null)

            {

              removeTimer(scrollTimer);

              scrollTimer=null;

            }

            if (position<0||position>=numDisplayed)

              return;

            selected=position+scrollOffset;

            if (selected!=oldselected)

            {

              Graphics g=createGraphics();

						  if (oldselected!=-1)

							{

								g.setColor(255,255,255);

								g.fillRect(1,(oldselected-scrollOffset)*textHeight+1,width-3,textHeight);

								g.setColor(0,0,0);

								//a separator

								if (((String)options.get(oldselected)).equals("-"))

								{

									for(int k = 0; k < width-3; k += 2)

									g.drawLine(k,(oldselected-scrollOffset)*textHeight + (textHeight /2),k,(oldselected-scrollOffset)*textHeight + (textHeight /2));   

								}

								else

									g.drawText((String)options.get(oldselected),3,(oldselected-scrollOffset)*textHeight+1);

							}

              g.setColor(0,0,0);

              //a separator

              if (((String)options.get(selected)).equals("-"))

              {

                for(int k = 0; k < width-3; k += 2)

                g.drawLine(k,(selected-scrollOffset)*textHeight + (textHeight /2),k,(selected-scrollOffset)*textHeight + (textHeight /2));   

              }

              else

              {

                  g.fillRect(1,(selected-scrollOffset)*textHeight+1,width-3,textHeight);

                  g.setColor(255,255,255);

                  g.drawText((String)options.get(selected),3,(selected-scrollOffset)*textHeight+1);

              }

              oldselected=selected;

            }

          }

          break;

        case PenEvent.PEN_UP:

            if (popup!=null)

            {

                clicked=true;

                int position=py/textHeight;

                if (py<0||position>=numDisplayed)

                  return;

                if (!((String)options.get(oldselected)).equals("-"))

                {

                    postEvent(new ControlEvent(ControlEvent.PRESSED,this));

                    this.hide();

					menubar.hide();     

					if(listener != null){

						listener.actionPerformed(new ActionEvent(this,this,(String)options.get(oldselected)));

					}               

                }

            }

        break;

      }

    }

  }

  

  public void addActionListener(ActionListener l){

  	if(listener == null){

  		listener = l;

  	}

  }

  public void removeActionListener(ActionListener l){

  	if(listener == l){

  		listener = null;

  	}

  }

}