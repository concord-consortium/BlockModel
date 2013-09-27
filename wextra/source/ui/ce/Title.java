/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 17/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 ****************************************************************************/



package org.concord.waba.extra.ui;



import waba.fx.*;

import waba.ui.*;

import extra.ui.*;



/**

 * A standard palm title bar.  Doesn't do anything, just sits at the top of the screen and

 * displays the title of the application.  May link any presses on the bar to menus later.

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.0.0 17 April 1999

 */

public class Title extends Control implements PreferredSize

{

  String name;

  int textWidth=0;



  /**

   * Construct a new title with the text "Untitled"

   */

  public Title()

  {

    this("Untitled");

  }



  /**

   * Construct a new title with the given text

   * @param name the title

   */

  public Title(String name)

  {

    this.name = name;

  }



  public void setText(String title)

  {

    name=title;

    textWidth=0;

    repaint();

  }



  public int getPreferredWidth(FontMetrics fm)

  {

    return RelativeContainer.FILL;

  }



  public int getPreferredHeight(FontMetrics fm)

  {

    return fm.getHeight()+4;

  }

  

  public void onPaint(Graphics g)

  {

    //Debug.debug("paintTitle");

    // draw line across

    g.setColor(0, 0, 0);

    g.drawLine(0, height - 2, width, height - 2);

    g.drawLine(0, height - 1, width, height - 1);



    // draw title

    if (textWidth==0)

      textWidth=getFontMetrics(MainWindow.defaultFont).getTextWidth(name)+8;

    g.drawLine(1,0,textWidth-2,0);

    g.fillRect(0,1, textWidth, height - 2);

    g.setColor(255, 255, 255);

    g.drawText(name, 4, 2);

  }

  public void onEvent(Event event)

  {

	  if (event.type == PenEvent.PEN_UP)

		{

			PenEvent pe = (PenEvent)event;

			if (pe.x >= 0 && pe.x < textWidth && pe.y >= 0 && pe.y < this.height)

			{

			  if (MainWindow.getMainWindow() instanceof ExtraMainWindow)

				{

				  MenuBar menubar=((ExtraMainWindow)MainWindow.getMainWindow()).menubar;

					if (menubar!=null)

					  menubar.show();

				}

				postEvent(new ControlEvent(ControlEvent.PRESSED, this));

			}

		}

  }

}