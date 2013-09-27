/*****************************************************************************
 *                                Waba Extras
 *
 * Version History
 * Date                Version  Programmer
 * ----------  -------  -------  ------------------------------------------
 * 25/04/1999  New      1.0.0    Rob Nielsen
 * Class created
 *
 * 02/10/1999  New      1.0.1    Rob Nielsen
 * Modified to javadoc nicely with JDK1.1
 *
 ****************************************************************************/

package extra.ui;

import waba.fx.*;
import waba.ui.*;
import waba.util.Vector;
import waba.sys.Vm;

/**
 * A Container extension that supports relative placement.  ie you can specify
 * you want to place a Control to the RIGHT of the last one, or in the center of
 * the Container.  The ExtraMainWindow has a RelativeContainer built in so you
 * can use all the methods here directly on your app if you extend it instead of
 * MainWindow.  See the RelativeTest program for an example of it's use.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version    1.0.1 2 October 1999
 */
public class RelativeContainer extends Container
{
  // positioning constants

  /** the far left of the screen */
  public static final int LEFT=-1;

  /** to the left of the last component added */
  public static final int LEFTOF=-2;

  /** in the center of the screen */
  public static final int CENTER=-3;

  /** to the right of the last component added */
  public static final int RIGHTOF=-4;

  /** on the far right of the screen */
  public static final int RIGHT=-5;

  /** the same as the relative Control (centered if different widths) */
  public static final int SAME=-6;

  /** the same as the relative Control (left aligned if different widths) */
  public static final int SAME_LEFT=-7;

  /** the same as the relative Control (right aligned if different widths) */
  public static final int SAME_RIGHT=-8;

  /** at the top of the screen */
  public static final int TOP=-1;

  /** above the last component added */
  public static final int ABOVE=-2;

  /** below the last component added */
  public static final int BELOW=-4;

  /** on the bottom of the screen */
  public static final int BOTTOM=-5;

  /** the same as the relative Control (top aligned if different widths) */
  public static final int SAME_TOP=-7;

  /** the same as the relative Control (bottom aligned if different widths) */
  public static final int SAME_BOTTOM=-8;

  // size constants

  /** use the preferred size of the control */
  public static final int AUTO=0;

  /** fill the whole width or height */
  public static final int FILL=-1;

  /** use the rest of the space from the current position to the right of the container (or bottom) */
  public static final int REST=-2;

  /** a vector of RelativeLayoutInfo object containing info on the position of objects */
  protected Vector layoutControls=null;

  /** the last Control added (if none specified as relative */
  protected Control lastAdded=null;

  /** the font metrics for the default font */
  static FontMetrics fm;

  // the gap between components
  protected int xgap=3;
  protected int ygap=3;

  /**
   * Sets the standard gap between components when added with the LEFTOF, RIGHTOF, ABOVE
   * and BELOW settings.
   * @param x the x gap in pixels
   * @param y the y gap in pixels
   */
  public void setGaps(int x,int y)
  {
    xgap=x;
    ygap=y;
  }

  /**
   * Construct a new component that will fill to space provided
   */
  public RelativeContainer()
  {
  }

  /**
   * Sets the bounds of this container and redoes the layout.
   * @param x the x coord of the bounding box
   * @param y the y coord of the bounding box
   * @param width the width of the bounding box
   * @param height the height of the bounding box
   */
  public void setRect(int x,int y,int width,int height)
  {
    super.setRect(x,y,width,height);
    layout();
  }

  /**
   * Adds a control to this container at the specified position using the last added Control for
   * relative placement, if required.  The preferred size, or the size set by setRect() is used.
   * @param control the control to add
   * @param x the x coordinate to add the control, or a placement constant.
   * @param x the y coordinate to add the control, or a placement consrant.
   * @see #add(Control,int,int,int,int,Control)
   */
  public void add(Control control,int x,int y)
  {
    add(control,x,y,lastAdded);
  }

  /**
   * Adds a control to this container at the specified position relative to the given Control.
   * The preferred size, or the size set by setRect() is used.
   * @param control the control to add
   * @param x the x coordinate to add the control, or a placement constant.
   * @param x the y coordinate to add the control, or a placement consrant.
   * @param relative the control this one is to be placed relative to
   * @see #add(Control,int,int,int,int,Control)
   */
  public void add(Control control,int x,int y,Control relative)
  {
    Rect r=control.getRect();
    add(control,x,y,r.width,r.height,relative);
  }

  /**
   * Adds a control to this container at the specified position using the last added Control for
   * relative placement, if required.  The size is also specified and can be absolute values
   * or constants too.
   * @param control the control to add
   * @param x the x coordinate to add the control, or a placement constant.
   * @param x the y coordinate to add the control, or a placement consrant.
   * @param width the width of the control, or a size constant
   * @param height the height of the control, or a size constant
   * @see #add(Control,int,int,int,int,Control)
   */
  public void add(Control control,int x,int y,int width,int height)
  {
    add(control,x,y,width,height,lastAdded);
  }

  /**
   * Adds a component to this container at the specified position relative to the given Control.
   * The available relative constants for the x coordinate are:
   * <ul>
   * <li> LEFT the far left of the container
   * <li> LEFTOF to the left of the last component added
   * <li> CENTER in the center of the container
   * <li> RIGHTOF to the right of the last component added
   * <li> RIGHT on the far right of the container
   * <li> SAME the same x coordinate as the previous component (centered if the widths are different)
   * <li> SAME_LEFT as above but left aligned if different
   * <li> SAME_RIGHT as above but right aligned if different
   * </ul>
   * The available relative constants for the y coordinate are:
   * <ul>
   * <li> TOP the top of the container
   * <li> ABOVE above the last component added
   * <li> CENTER in the center of the container
   * <li> BELOW below the last component added
   * <li> BOTTOM on the bottom of the container
   * <li> SAME the same y coordinate as the previous component (centered if the heights are different)
   * <li> SAME_TOP as above but top aligned if different
   * <li> SAME_BOTTOM as above but bottom aligned if different
   * </ul>
   * The size is also specified and can be either absolute values or one of the following constants:
   * <ul>
   * <li> AUTO use the preferred width or height of this control
   * <li> FILL use the full width of height of the container
   * <li> REST use the rest of the space to the right or down from the current position
   * <li> SAME the same as the relative control
   * </ul>
   * @param control the control to add
   * @param x the x coordinate to place it, or one of the constants for relative placement.
   * @param y the y coordinate to place it, or one of the constants for relative placement.
   * @param width the width of the control, or a size constant
   * @param height the height of the control, or a size constant
   * @param relative the control this one is to be placed relative to
   */
  public void add(Control control,int x,int y,int width,int height,Control relative)
  {
    add(control);
    RelativeLayoutInfo li=new RelativeLayoutInfo(control,x,y,width,height,relative,(y==ABOVE||y==BELOW?ygap:xgap));
    if (layoutControls==null)
      layoutControls=new Vector();
    int i=findControl(control);
    if (i!=-1)
      layoutControls.set(i,li);
    else
      layoutControls.add(li);
    layout(li);
    lastAdded=control;
  }

  /**
   * Remove this control permanently from this container.  If the straight remove(Control) method
   * is used the layout is saved and the control can be re-added with add(Control).  This is useful
   * for Popup and temporarily hiding Controls.  This method, however, is to be used when you never
   * plan to add the Control again.
   * @param control the control to remove
   */
  public void removePermanently(Control control)
  {
    int i=findControl(control);
    if (i!=-1)
      layoutControls.del(i);
    remove(control);
  }

  protected int findControl(Control control)
  {
    for(int i=0,size=layoutControls.getCount();i<size;i++)
      if (((RelativeLayoutInfo)layoutControls.get(i)).control==control)
        return i;
    return -1;
  }

  /**
   * Layout all the Controls in this container
   */
  public void layout()
  {
    if (layoutControls!=null)
      for(int i=0,size=layoutControls.getCount();i<size;i++)
        layout((RelativeLayoutInfo)layoutControls.get(i));
  }

  /**
   * Layout the Control described in the give layout info object
   * @param li the layout info object
   */
  protected void layout(RelativeLayoutInfo li)
  {
    if (parent==null)
      return;
    //Debug.debug(li.toString());
    int cx;
    int cy;
    int cw;
    int ch;
    Rect rel;
    if (li.relative!=null)
    {
      rel=li.relative.getRect();
    }
    else
      rel=new Rect(0,0,0,0);

    // look for constant sizes
    if (li.width==AUTO)
      cw=getPreferredWidth(li.control);
    else
    if (li.width==SAME)
      cw=rel.width;
    else
      cw=li.width;
    if (cw==FILL)
      cw=width;

    if (li.height==AUTO)
      ch=getPreferredHeight(li.control);
    else
    if (li.height==SAME)
      ch=rel.height;
    else
      ch=li.height;
    if (ch==FILL)
      ch=height;

    // set relative positions
    switch (li.x)
    {
      case LEFT: cx=0; break;
      case LEFTOF: cx=rel.x-cw-li.gap; break;
      case CENTER: cx=width/2-cw/2; break;
      case RIGHTOF: cx=rel.x+rel.width+li.gap; break;
      case RIGHT: cx=width; break;
      case SAME_LEFT: cx=rel.x; break;
      case SAME: cx=rel.x+(rel.width-cw)/2; break;
      case SAME_RIGHT: cx=rel.x+rel.width-cw; break;
      default: cx=li.x; break;
    }
    switch (li.y)
    {
      case TOP: cy=0; break;
      case ABOVE: cy=rel.y-ch-li.gap; break;
      case CENTER: cy=height/2-ch/2; break;
      case BELOW: cy=rel.y+rel.height+li.gap; break;
      case BOTTOM: cy=height; break;
      case SAME_TOP: cy=rel.y; break;
      case SAME: cy=rel.y+(rel.height-ch)/2; break;
      case SAME_BOTTOM: cy=rel.y+rel.height-ch; break;
      default: cy=li.y; break;
    }

    // if size is REST, set that now
    if (cw==REST)
      cw=width-cx;
    if (ch==REST)
      ch=height-cy;

    // make sure the component is within the bounds of the container and shift if necessary
    if (cx<0)
      cx=0;
    else
    if (cx+cw>width)
      cx=width-cw;
    if (cy<0)
      cy=0;
    else
    if (cy+ch>height)
      cy=height-ch;

    // set the rect if it has changed
    Rect cr=li.control.getRect();
    if (cr.x!=cx||cr.y!=cy||cr.width!=cw||cr.height!=ch)
      li.control.setRect(cx,cy,cw,ch);
  }

  /** Returns the child located at the given x and y coordinates. */
  public Control findChild(int x, int y)
  {
    Control child;
    Control found;

    child = children;
    found=null;
    while (child != null)
    {
      if (child.contains(x, y))
        found=child;
      child = child.getNext();
    }
    child=found;
    if (child == null)
      return this;
    if (!(child instanceof Container))
      return child;
    Rect r=child.getRect();
    return ((Container)child).findChild(x-r.x,y-r.y);
  }

  /**
   * Gets the preferred height of the given control.  This is a bit of a hack.  Ideally
   * getPreferredWidth() and getPreferredHeight() should be methods of Control but they
   * aren't so this is the only way to do it.
   * @param control the control to check
   * @returns the preferred height
   */
  static int getPreferredHeight(Control control)
  {
    if (fm==null)
      fm=control.getFontMetrics(MainWindow.defaultFont);
    if (control instanceof PreferredSize)
      return ((PreferredSize)control).getPreferredHeight(fm);

    if (control instanceof Edit&&Vm.isColor())
      return fm.getHeight()+5;
    else
      return fm.getHeight()+3;
  }

  /**
   * Gets the preferred width of the given control.  This is a bit of a hack.  Ideally
   * getPreferredWidth() and getPreferredHeight() should be methods of Control but they
   * aren't so this is the only way to do it.
   * @param control the control to check
   * @returns the preferred width
   */
  int getPreferredWidth(Control control)
  {
    if (fm==null)
      fm=control.getFontMetrics(MainWindow.defaultFont);
    if (control instanceof PreferredSize)
      return ((PreferredSize)control).getPreferredWidth(fm);
    if (control instanceof Button)
      return fm.getTextWidth(((Button)control).getText())+14;
    if (control instanceof Label)
      return fm.getTextWidth(((Label)control).getText());
    if (control instanceof Radio)
      return fm.getTextWidth(((Radio)control).getText())+17;
    if (control instanceof Check)
      return fm.getTextWidth(((Check)control).getText())+17;
    if (control instanceof Container||control instanceof Edit)
      return REST;
    return 20;
  }
}

/**
 * A class for storing layout information
 */
class RelativeLayoutInfo extends Rect
{
  /** the control this info relates to */
  public Control control;

  /** the control any relative position is relative to */
  public Control relative;

  /** the gap between the relative control */
  public int gap;

  /** Constructs a new RelativeLayoutInfo */
  public RelativeLayoutInfo(Control control,int x,int y,int width,int height,Control relative,int gap)
  {
    super(x,y,width,height);
    this.control=control;
    this.relative=relative;
    this.gap=gap;
  }
}