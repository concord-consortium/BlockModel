package graph;

import waba.fx.*;
import extra.util.Maths;
import waba.ui.*;

public class JGraphics extends Graphics
{
    protected Color curColor = new Color(0,0,0);

    protected Font curFont = new Font("Helvetica", Font.PLAIN, 12);
    protected FontMetrics fm = null;
    protected ISurface isurf = null;
    protected Control control = null;

    /**
     * Instantiate the class
     */
    public JGraphics(ISurface i)
    {
	super(i);
	fm = new FontMetrics(curFont, i);
	isurf = i;
    }

    public JGraphics(Control c, ISurface i)
    {
	super(i);
	Rect r = c.getRect();

	isurf = i;
	control = c;
	translate(r.x,r.y);
	setClip(0,0,r.width,r.height);

    }

    public JGraphics(Control c)
    {
	super((ISurface)MainWindow.getMainWindow());

	MainWindow mw = MainWindow.getMainWindow();
	isurf = (ISurface)mw;
	control = c;
	int x, y;
	Rect r = c.getRect();
	x = r.x;
	y = r.y;
	c = (Control)(c.getParent());
	while(c != null && 
	      c != (Control)mw){
	    r = c.getRect();
	    x += r.x;
	    y += r.y;
	    c = (Control)(c.getParent());
	}
	translate(x,y);
    }

    public void setColor(Color c)
    {
	curColor = c;
	super.setColor(c.getRed(), c.getGreen(), c.getBlue());
    }

    public void setColor(int r, int g, int b)
    {
	curColor = new Color(r, g, b);
	super.setColor(r, g, b);
    }

    public Color getColor()
    {
	return curColor;
    }

    public void setFont(Font f)
    {
	curFont = f;
	fm = new FontMetrics(curFont, isurf);
	super.setFont(f);
    }

    public Font getFont()
    {
	return curFont;
    }

    public FontMetrics getFontMetrics()
    {
	if(fm == null)
	    fm = new FontMetrics(curFont, isurf);
	return fm;
    }

    public FontMetrics getFontMetrics(Font f)
    {
	return new FontMetrics(f, isurf);
    }

    public JGraphics create()
    {
	JGraphics g;
	if(control == null){
	    g = new JGraphics(isurf);
	} else {
	    g = new JGraphics(control);
	    g.control = control;
	}

	g.isurf = isurf;
	g.curFont = curFont;
	g.fm = fm;
	g.curColor = curColor;

	return g;
    }

    public void dispose()
    {
	curColor = null;
	curFont = null;
	fm = null;
	isurf = null;
	control = null;
	super.free();
    }

    public void drawString(String s, int x, int y)
    {
	super.drawText(s, x, y);
    }

}












