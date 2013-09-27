package graph;

import waba.ui.*;
import waba.fx.*;

public abstract class GraphView extends Container implements PropObject
{
    JGraphics myG = null;
    boolean drawn = false;

    public Graph2D graph = null;
    
    public GraphView(int w, int h)
    {
	width = w;
	height = h;
    }

    public void plot()
    {
	if(!drawn || graph.redraw)
	    draw();

	if(enabled && myG != null){
	    graph.plot(myG);
	}
    }

    public void draw()
    {
	if(enabled && myG != null){
	    graph.draw(myG, 0, 0);
	    drawn = true;
	}
    }


    public void setPos(int x, int y)
    {
	setRect(x,y,width,height);
    }

    public void onPaint(Graphics g)
    {
	// Give our new graphics the same clip
	Rect r	= getRect();
	g.getClip(r);
	if(myG == null){
	    myG = new JGraphics(this);
	}

	myG.setClip(r.x,r.y,r.width,r.height);

	// redraw graph with latest data
	graph.draw(myG,0,0);
	myG.clearClip();
    }

}
