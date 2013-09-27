/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 08/05/1999  New      0.9.0    Stefan Kellner

 * Class created

 *

 * 27/03/2000  New      1.0.0    Rob Nielsen

 * Fairly massive reorganization.  Removed PulldownMenu class.

 ****************************************************************************/



package org.concord.waba.extra.ui;





/**

 * This is a standard palm menu bar.

 * @author     <A HREF="mailto:kellner@no-information.de">Stefan Kellner</A>,

 * @version    1.0.0 08 May 1999

 */

public class MenuBar

{

  java.awt.MenuBar menubar;



  public MenuBar()

  {

    menubar = new java.awt.MenuBar();

  }





  /**

   * Add a new Menu to the menu bar

   * @param name the Menu to add

   */

  public void add(Menu menu)

  {

  	menubar.add(menu.getAWTMenu());

  }



   public java.awt.MenuBar getAWTMenuBar() {return menubar;}

}