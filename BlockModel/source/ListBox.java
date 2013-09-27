/*****************************************************************************
 *                                Waba Extras
 *
 * Version History
 * Date                Version  Programmer
 * ----------  -------  -------  ------------------------------------------
 * 21/04/1999  New      1.0.0    Rob Nielsen
 * Class created
 *
 * 20/08/1999  New      1.0.1    Rob Nielsen
 * Changed getMainWindow() call to MainWindow.getMainWindow()
 *
 ****************************************************************************/

import waba.fx.*;
import waba.ui.*;
import waba.util.Vector;

/**
 * This is a standard palm drop down list.  It normally displays a down arrow followed
 * by the currently selected item.  When it is tapped it will drop down a list of all the
 * options.  When one of these is tapped, the list will fold back up with the new option
 * displayed.  A PRESSED event is also propogated down the hierachy.
 * The selected string get be obtained with getSelected() and it's index with
 * getSelectedIndex().  Items can be added to the list with the add() method.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.1 20 August 1999
 */
public class ListBox extends Control
{
  String name;

  Image arrow;
  Vector options;
  int selected=-1;
  int oldselected=0;
  int textHeight;
  int numDisplayed=0;
  int scrollOffset=0;
  int maxScrollOffset=0;
  boolean scrollUp;
  Timer scrollTimer=null;
  FontMetrics fm=null;
    Window myWin = null;

  /**
   * Construct a new empty list of default size
   */
  public ListBox()
  {
    this(new Vector());
  }

    public ListBox(Window w)
    {
	this();
	myWin = w;
    }

  /**
   * Construct a new list with the given options
   * @param options an array of the choices available
   */
  public ListBox(String[] options)
  {
    this();
    for(int i=0,size=options.length;i<size;i++)
      add(options[i]);
  }

  /**
   * Construct a new list with the given options
   * @param options a vector of the choices available
   */
  public ListBox(Vector options)
  {
    this.options=options;
  }

  /**
   * Add a new option to the end of the list
   * @param name the option to add
   */
  public void add(String name)
  {
    options.add(name);
    /*
    int w;
    if (fm!=null&&(w=fm.getTextWidth(name))>expandedWidth)
      expandedWidth=w;
    */
  }

  /**
   * Gets the text of the currently selected item
   * @returns the selected text
   */
  public String getSelected()
  {
    return (selected == -1)?"":(String)options.get(selected); //guich
  }

  /**
   * Get the index of the currently selected option
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
    if (-1 <= i && i < options.getCount()) //guich
    {
      selected=i;
      oldselected=i;
      if (i != -1) postEvent(new ControlEvent(ControlEvent.PRESSED,this)); //guich
    }
  }

  public boolean calcSizes()
  {
    if (fm==null)
      fm=getFontMetrics(MainWindow.defaultFont);
    if (fm==null)
      return false;
    textHeight = fm.getHeight();
    numDisplayed=options.getCount();
    scrollOffset=0;
    if (numDisplayed*textHeight>(height-2))
	numDisplayed=(height-2)/textHeight;
    maxScrollOffset=options.getCount()-numDisplayed;
    oldselected=selected;

    return true;
  }

  public void onPaint(Graphics g)
  {
    calcSizes();

    drawList(g);
    g.setColor(0,0,0);
    g.drawLine(1,0,width-3,0);
    g.drawLine(0,1,0,height-3);
    g.drawLine(width-2,1,width-2,height-2);
    g.drawLine(1,height-2,width-3,height-2);
    g.drawLine(2,height-1,width-3,height-1);
    g.drawLine(width-1,2,width-1,height-3);
  }

  public void drawList(Graphics g)
  {
    g.setColor(255,255,255);
    g.fillRect(1,1,width-3,height-3);
    g.setColor(0,0,0);
    for(int i=0;i<numDisplayed;i++)
    {
      if (i+scrollOffset==selected)
      {
        g.fillRect(1,i*textHeight+1,width-3,textHeight);
        g.setColor(255,255,255);
      }
      g.drawText((String)options.get(i+scrollOffset),3,i*textHeight+1);
      if (i+scrollOffset==selected)
        g.setColor(0,0,0);
    }
  }

  /**
   * Process pen and key events to this component
   * @param event the event to process
   */
  public void onEvent(Event event)
  {
    if (event.type==ControlEvent.TIMER)
    {
      scrollOffset+=(scrollUp?-1:1);
      if (selected == -1) selected = 0; //guich
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
            int position=py/textHeight;
            if ((py<=5&&scrollOffset>0)||(py>=height-5&&scrollOffset<maxScrollOffset))
            {
              if (scrollTimer==null)
              {
                scrollUp=(py<=5);
                scrollTimer=addTimer(200);
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
              g.setColor(255,255,255);
              if (oldselected != -1) g.fillRect(1,(oldselected-scrollOffset)*textHeight+1,width-3,textHeight);
              g.setColor(0,0,0);
              if (oldselected != -1) g.drawText((String)options.get(oldselected),3,(oldselected-scrollOffset)*textHeight+1); //guich
              if (selected != -1) g.fillRect(1,(selected-scrollOffset)*textHeight+1,width-3,textHeight);
              g.setColor(255,255,255);
              if (selected != -1) g.drawText((String)options.get(selected),3,(selected-scrollOffset)*textHeight+1); //guich
              oldselected=selected;
            }
          break;
        case PenEvent.PEN_UP:
	    // Should check if the event was outside our bounds
            if (scrollTimer!=null)
            {
              removeTimer(scrollTimer);
              scrollTimer=null;
            }
            postEvent(new ControlEvent(ControlEvent.PRESSED,this));
	    if(myWin != null){
		myWin.unpop();
	    }
	    break;
      }
    }
  }
}
