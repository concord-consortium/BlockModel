/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 21/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 ****************************************************************************/



package extra.ui;



import waba.fx.*;

import waba.ui.*;

import waba.util.Vector;



/**

 * This is a basic eight segment number display control which can be scaled to any size.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.0 21 April 1999

 */

public class BigNumber extends Control implements PreferredSize

{

  public static final int DOLLAR=-1;

  public static final int POINT=-2;

  

  public static final int DIRECT=0;

  public static final int UP_DOWN=1;

  public static final int NONE=2;

  

  static final int nums[]={63,6,91,79,102,109,125,7,127,111};

  

  int num=0;

  int mode=0;



  /**

   * Construct a new number initialised to 0 with DIRECT changing

   */

  public BigNumber()

  {

    setNumber(0);

  }



  /**

   * Construct a new number with DIRECT changing

   */

  public BigNumber(int num)

  {

    if (num==DOLLAR||num==POINT)

      setMode(NONE);

    setNumber(num);

  }



  /**

   * Construct a new number with the given options

   * @param num the value to start with

   * @param mode the mode to use

   */

  public BigNumber(int num,int mode)

  {

    setMode(mode);

    setNumber(num);    

  }

  

  /**

   * Sets the value of this number

   * @param num 0-9, DOLLAR (for $) or POINT (for .)

   */

  public void setNumber(int num)

  {

    if (num<0)

    {

      if (mode==UP_DOWN)

        num=(num+10)%10;

      else      

      if (mode==DIRECT)

        num=0;

    }

    else

    if (num>9)

      if (mode==UP_DOWN)

        num=(num%10);

      else

        num=9;

    if (num!=this.num)

    {

      this.num=num;

      repaint();

    }

  }

  

  /**

   * Sets the mode of changing this number.

   * Possible values are:

   * <ul>

   * <li>DIRECT - value is determined by the y position clicked - top is 0, bottom is 9, the rest evently divided between

   * <li>UP_DOWN - value is increased by one if clicked in the top half, decreased by one in the bottom half

   * <li>NONE - this number cannot change

   * </ul>

   * @param mode the mode

   */

  public void setMode(int mode)

  {

    this.mode=mode;

  }

  

  /**

   * Gets the value of the number

   * @returns the value from 0-9 (or DOLLAR or POINT)

   */

  public int getNumber()

  {

    return num;

  }



  public int getPreferredWidth(FontMetrics fm)

  {

    return 40;

  }



  public int getPreferredHeight(FontMetrics fm)

  {

    return 50;

  }

  

  public void onPaint(Graphics g)

  {

    int lw=height/10;

    if (lw<1)

      lw=1;

      

    g.setColor(0,0,0);  

    if (num==DOLLAR) // draw dollarsign

    {

      int gap=height/4;

      g.drawLine(width,gap,0,gap);

      g.drawLine(0,gap,0,gap*2);

      g.drawLine(0,gap*2,width,gap*2);

      g.drawLine(width,gap*2,width,gap*3);

      g.drawLine(width,gap*3,0,gap*3);

      g.drawLine(width/2,0,width/2,height);

    }

    else

    if (num==POINT) // draw point

      g.fillRect(width/2-lw/2,height-lw,lw,lw);

    else // draw numbers

    {

      int lp=0;

      int rp=width-lw;

      int tp=0;

      int mp=(height-lw)/2;

      int bp=height-lw;

      int wid=width;

      int th=mp;

      int bh=height-mp;



      int n=nums[num];



      if ((n&1)>0)

        g.fillRect(lp,tp,wid,lw);

      if ((n&2)>0)

        g.fillRect(rp,tp,lw,th);

      if ((n&4)>0)

        g.fillRect(rp,mp,lw,bh);

      if ((n&8)>0)

        g.fillRect(lp,bp,wid,lw);

      if ((n&16)>0)

        g.fillRect(lp,mp,lw,bh);

      if ((n&32)>0)

        g.fillRect(lp,tp,lw,th);

      if ((n&64)>0)

        g.fillRect(lp,mp,wid,lw);

    }

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

      

      switch (event.type)

      {

        case PenEvent.PEN_DOWN: 

          if (mode==UP_DOWN)

          {

            setNumber(num+(py<height/2?1:-1));

            postEvent(new ControlEvent(ControlEvent.PRESSED,this));

            break;

          }

        case PenEvent.PEN_DRAG:   

          if (mode==DIRECT)

          {

            setNumber(py/(height/10));

            postEvent(new ControlEvent(ControlEvent.PRESSED,this));

            break;

          }

      }

    }

  }

}