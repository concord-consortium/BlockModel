/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                 Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 25/03/1999  New      1.0.0    Stefan Kellner

 * Class created

 *

 * 19/10/1999  New      1.0.1    Rob Nielsen

 * Added checks in onEvent to see if pushgroup is null

 *

 * 05/11/1999  New      1.1.0    Rob Nielsen

 * Rewrote to remove dependence on PushbuttonGroup.

 *

 ****************************************************************************/



package extra.ui;



import waba.ui.*;

import waba.fx.*;



/**

 * This is a standard palm pushbutton.

 * @author     <A HREF="mailto:kellner@i-clue.de">Stefan Kellner</A>,

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,

 * @version    1.1.0 5 November 1999

 */



public class Pushbutton extends Control implements PreferredSize 

{

  /** the head of the current group */

  Pushbutton headpb;



  /** the next element in the current group */

  Pushbutton nextpb;



  /** the selected pushbutton (only valid if this==headpb) */

  Pushbutton selectedpb;



  /** the name to be displayed on this button */

  String name;



  /** is this button selected? */

  private boolean selected=false;



  public static Pushbutton[] createGroup(String[] names,int selected)

  {

    Pushbutton last=null;

    Pushbutton[] ret=new Pushbutton[names.length];

    for(int i=0;i<names.length;i++)

      ret[i]=new Pushbutton(names[i],(i==selected),(i==0?null:ret[i-1]));

    return ret;

  }



  /**

   * Constructs a new unconnected, unselected push button.

   * @param name the text to go on the button

   */

  public Pushbutton(String name) 

  {

    this(name,false,null);

  }



  /**

   * Constructs a new unselected push button in the same group

   * as the given pushbutton.

   * @param name the text to go on the button

   * @param connection the pushbutton to connect to

   */

  public Pushbutton(String name,Pushbutton connection)

  {

    this(name,false,connection);

  }



  /**

   * Construct a new unconnected push button with the given

   * selection status.

   * @param name the text to go on the button

   * @param state the state of the button

   */

  public Pushbutton(String name, boolean state) 

  {

    this(name,state,null);

  }



  /**

   * Construct a new push button with the given selection status in the

   * same group as the given pushbutton.

   * @param name the text to go on the button

   * @param state the state of the button

   * @param connection the pushbutton to connect to

   */

  public Pushbutton(String name, boolean state, Pushbutton connection)

  {

    this.name=name;

    if (connection!=null)

      connect(connection);

    else

    {

      headpb=this;

      nextpb=null;

    }

    setSelected(state);

  }



  /**

   * Connects this Pushbutton with a group.  Only one pushbutton in a 

   * group can be selected at one time.

   * @param pb the group to connect to

   */

  public void connect(Pushbutton pb)

  {

    nextpb=pb.nextpb;

    headpb=pb.headpb;

    pb.nextpb=this;

  }



  /**

   * Removes this pushbutton from it's current group.

   */

  public void disconnect()

  {

    Pushbutton pb=headpb;

    if (this==headpb)

    {

      while (pb!=null)

      {

        pb.headpb=nextpb;

        pb=pb.nextpb;

      }

    }

    else

    {

      while (pb!=null&&pb.nextpb!=this)

        pb=pb.nextpb;

      if (pb!=null)

        pb.nextpb=nextpb;

    }

    if (selected)

      headpb.selectedpb=null;

  }



  /**

   * Gets the pushbutton that is selected in the current group

   * @return the selected pushbutton, or null if none

   */

  public Pushbutton getSelected()

  {

    return headpb.selectedpb;

  }



  /**

   * Is this pushbutton selected?

   * @return true if it is, false otherwise

   */

  public boolean isSelected()

  {

    return selected;

  }



  /**

   * Sets whether the push button is pressed down or not

   * @param b true if selected, false otherwise

   */

  public void setSelected(boolean b)

  {

    if (selected!=b)

    {

      if (b)

      {

        Pushbutton pb=getSelected();

        if (pb!=null)

          pb.setSelected(false);

        headpb.selectedpb=this;

      }

      selected=b;

    }

  }



  /**

   * Returns the text of this Button

   */

  public String getText()

  {

    return name;

  }

  

  public int getPreferredWidth(FontMetrics fm)

  {

    return fm.getTextWidth(name)+6;

  }

  

  public int getPreferredHeight(FontMetrics fm)

  {

    return fm.getHeight()+3;

  }

  

  /**

   * Paints the push button to the screen

   */

  public void onPaint(Graphics g)

  {

    FontMetrics fm=getFontMetrics(MainWindow.defaultFont);

    int textX = (width - fm.getTextWidth(name)) / 2;

    int textY = (height - fm.getHeight()) / 2;



    if (selected)

    {

      // make pressed or active button

      g.setColor(0,0,0);

      g.fillRect(0,0,width,height);

      g.setColor(255,255,255);

    }

    else

    {

      // make normal button

      g.setColor(0, 0, 0);

      g.drawRect(0,0,width,height);

    }

    g.drawText(name, textX, textY);

  }



  /**

   * Process pen and key events to this component

   * @param event the event to process

   */

  public void onEvent(Event event)

  {

    if (event instanceof PenEvent)

    {

      int px=((PenEvent)event).x;

      int py=((PenEvent)event).y;

      Pushbutton spb=headpb.selectedpb;

      switch (event.type)

      {

        case PenEvent.PEN_DOWN:          

          if (spb!=this&&spb!=null)

          {

            spb.selected=false;

            spb.repaint();

            selected=true;

            repaint();

          }

          break;

        case PenEvent.PEN_DRAG:

          if (selected&&!(px>=0&&px<width&&py>=0&&py<height))

          {

            selected=false;

            repaint();

          }

          else

          if (!selected&&px>=0&&px<width&&py>=0&&py<height)

          {

            selected=true;

            repaint();

          }

          break;

        case PenEvent.PEN_UP:

          if (px>=0&&px<width&&py>=0&&py<height)

          {

            if (spb!=this)

            {

              postEvent(new ControlEvent(ControlEvent.PRESSED,this));

              headpb.selectedpb=this;

            }

          }

          else

          {

            spb.selected=true;

            spb.repaint();

            spb=null;

          }

          break;

      }

    }

  }

}