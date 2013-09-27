/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 23/04/1999  New      1.0.0    Rob Nielsen

 * Class created

 *

 ****************************************************************************/

package extra.ui;



import waba.fx.FontMetrics;



/**

 *

 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,

 * @version    1.1.0 16 October 1999

 */

public interface PreferredSize

{

  public int getPreferredWidth(FontMetrics fm);



  public int getPreferredHeight(FontMetrics fm);

}