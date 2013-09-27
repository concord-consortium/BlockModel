/*****************************************************************************
 *                                Waba Extras
 *
 * Version History
 * Date                Version  Programmer
 * ----------  -------  -------  ------------------------------------------
 * 25/04/1999  New      1.0.0    Rob Nielsen
 * Class created
 *
 ****************************************************************************/

package extra.ui;

import waba.fx.*;
import waba.ui.*;
import waba.util.Vector;

/**
 * A container that lays it's component controls out in a grid.  Here's an example.
 * <pre>
 *   GridContainer gc=new GridContainer(2,2);
 *   gc.add(new Button("One"));
 *   gc.add(new Button("Two"));
 *   gc.add(new Button("Three"));
 *   gc.add(new Button("Four"));
 *   gc.setRect(0,0,160,100);
 *   add(gc);
 * </pre>
 * which should give you something like:
 * <pre>
 *  _____________________
 * |          |          |
 * |   One    |   Two    |
 * |__________|__________|
 * |          |          |
 * |  Three   |   Four   |
 * |__________|__________|
 * </pre>
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 25 April 1999
 */
public class GridContainer extends Container
{
  /** the layout of controls */
  protected Control[][] layout=null;

  /** the number of columns */
  int gridx=0;
  
  /** the number of rows */
  int gridy=0;
  
  /** the x grid position to put the next unspecified control */
  int xp=0;

  /** the y grid position to put the next unspecified control */  
  int yp=0;
  
  // the gap between controls
  protected int xgap=3;
  protected int ygap=3;

  /**
   * Sets the gap between controls.
   * @param x the x gap in pixels
   * @param y the y gap in pixels
   */
  public void setGaps(int x,int y)
  {
    xgap=x;
    ygap=y;
  }
  
  /**
   * Construct a new container with the specified grid width and height.
   * @param gridx the number of columns in the grid.
   * @param gridy the number of rows in the grid.
   */
  public GridContainer(int gridx,int gridy)
  {
    this.gridx=gridx;
    this.gridy=gridy;
    layout=new Control[gridx][gridy];
  }
  
  /**
   * Gets the control at the given grid position
   * @param x the x grid coord
   * @param y the y grid coord
   * @returns a control, or null if none at that position
   */
  public Control get(int x,int y)
  {
    return layout[x][y];
  }
  
  /**
   * Adds a control at the grid position to the left of the last one added or (0,0) if this is the first.
   * If the last added was at the maximum x position, this one is added at an x pos of 0 on the next y pos down.
   * @param c the control to add
   */
  public void add(Control c)
  {
    add(c,xp,yp);
  }
  
  /**
   * Adds a control at the specified grid position
   * @param x the x grid coord
   * @param y the y grid coord
   */
  public void add(Control c,int x,int y)
  {    
    if (yp>gridy)
      return;
    super.add(c);  
    layout[x][y]=c;
    xp=x+1;
    if (xp>=gridx)
    {
      xp=0;
      yp=y+1;
    }
    else
      yp=y;  
  }
  
  /**
   * Removes a control from this layout.
   * @param c the control to remove
   */
  public void remove(Control c)
  {
    if (c==null)
      return;
    for(int i=0;i<gridx;i++)
      for(int j=0;j<gridy;j++)
        if (layout[i][j]==c)
        {
          remove(i,j);
          return;
        }
  }
  
  /**
   * Removes a control at the given grid position.
   * @param x the x grid coord
   * @param y the y grid coord
   */
  public void remove(int x,int y)
  {
    if (layout[x][y]==null||x<0||xp>=gridx||y<0||y>=gridy)
      return;
    xp=x;
    yp=y;
    super.remove(layout[x][y]);
    layout[x][y]=null;
  }
  
  /**
   * Sets the rect for this container.  Redoes the layout.
   */
  public void setRect(int x,int y,int width,int height)
  {
    super.setRect(x,y,width,height);
    layout();
  }
  
  /**
   * Layout all the controls in this container.
   */
  public void layout()
  {
    int gw=(width+xgap)/gridx;
    int gh=(height+ygap)/gridy;
    int xofs=(width+xgap-gw*gridx)/2;
    int yofs=(height+ygap-gh*gridy)/2;
    for(int i=0;i<gridx;i++)
      for(int j=0;j<gridy;j++)
      {
        Control c=layout[i][j];
        if (c!=null)
          c.setRect(xofs+i*gw,yofs+j*gh,gw-xgap,gh-ygap);
      }
  }
}