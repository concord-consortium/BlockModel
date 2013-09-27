/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 25/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 * 20/08/1999  New      1.0.1    Rob Nielsen

 * Fixed bug when popup object is in a container.

 *

 ****************************************************************************/



package extra.ui;



import waba.ui.*;

import waba.fx.*;

import waba.sys.Vm;



/**

 * Convienience class for managing controls that pop up.  The only one at the

 * moment is List but Menu will also use it.  Controls create a new Popup

 * object then call popup() on it with their new size.  Popup will remove the

 * Control from it's parent and add it to the mainWindow, adjusting the

 * positions accordingly.  When the popup is finished, unpop() is called and

 * the Control is removed and returned to it's parent at the original size.

 * If any events need to be sent from the popped up control use

 * Popup.postEvent() rather than Control.postEvent().  It works the same way,

 * but ensures all intervening containers get all the messages.  Alternatively,

 * you can wait until you call unpop() before using the standard postEvent()

 * system.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.1 20 August 1999

 */

public class Popup extends Container

{

  Rect oldRect;

  Control target;

  Container targetsParent;

  int xofs;

  int yofs;

  boolean poppedup=false;



  /**

   * Construct a new Popup for the given Control

   * @param control the control to popup

   */

  public Popup(Control control)

  {

    target=control;

  }



  /**

   * Popup the control now but leave it the same size.

   */

  public void popup()

  {

    Rect r=target.getRect();

    popup(r.x,r.y,r.width,r.height);

  }



  /**

   * Popup the control now with the given new size.  The control will be

   * shifted if the new size doesn't fit on the screen.

   *

   * @param px the x coordinate of the rect

   * @param py the y coordinate of the rect

   * @param pw the width of the rect

   * @param ph the height of the rect

   */

  public void popup(int px,int py,int pw,int ph)

  {

    if (poppedup)

      return;

    poppedup=true;

    if ((targetsParent=target.getParent())!=null)

		{

			oldRect=target.getRect();

			Control c = targetsParent;

			while (!(c instanceof MainWindow))

			{

				Rect r=c.getRect();

				px += r.x;

				py += r.y;

				c = c.getParent();

				if (c == null)

					break;

			}

			targetsParent.remove(target);

		}

		Rect r=MainWindow.getMainWindow().getRect();

		if (px<0)

			px=0;

		else

		if (px+pw>r.width)

			px=r.width-pw;

		if (py<0)

			py=0;

		else

		if (py+ph>r.height)

			py=r.height-ph;

    target.setRect(px,py,pw,ph);

		MainWindow mw=MainWindow.getMainWindow();

    mw.add(target);

		if (mw.getFocus()!=target)

  		mw.setFocus(target);

  }



  /**

   * Return the control to it's original state

   */

  public void unpop()

  {

	  if (!poppedup)

		  return;

    target.getParent().remove(target);

		poppedup=false;

		if (targetsParent!=null)

		{

			target.setRect(oldRect.x,oldRect.y,oldRect.width,oldRect.height);

			targetsParent.add(target);

			if (targetsParent instanceof RelativeContainer)

				((RelativeContainer)targetsParent).layout();

		}

  }



  /**

	 * Posts an event. The event pass will be posted to this control

	 * and all the parent controls of this control (all the containers

	 * this control is within).  This method should be used instead of

	 * Control.postEvent() when a popup is being used to ensure proper

	 * results when the control is inside another container.

	 */

	public void postEvent(Event event)

	{

		Control c;

	

	  target.onEvent(event);

		c = targetsParent;

		while (c != null)

		{

			c.onEvent(event);

	 		c = c.getParent();

		}

	}

}