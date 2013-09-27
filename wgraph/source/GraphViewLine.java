package graph;

import waba.ui.*;
import waba.fx.*;
import waba.util.*;
import waba.sys.*;

public class GraphViewLine extends GraphView
{
    final static float Y_MAX = (float)50.0;
    final static float Y_MIN = (float)-5.0;
    final static float X_MAX = (float)60.0;
    final static float X_MIN = (float)0.0;

    final static char DRAG_MODE = 'D';
    final static char ZOOM_MODE = 'Z';
    final static char ANNOT_MODE = 'A';

    public char mode = 'D';

    public char curChar = 'A';

    public float maxY, minY, maxX, minX;
    public String units = null;

    PropPage yAxisPage;
    PropPage xAxisPage;

    LineGraph lGraph;
    Annotation curAnnot = null;

    public GraphViewLine(int w, int h)
    {
	super(w,h);

	minY = Y_MIN;
	maxY = Y_MAX;
	minX = X_MIN;
	maxX = X_MAX;

	graph = lGraph = new LineGraph(w, h);
	graph.setYRange(minY, maxY - minY);
	lGraph.setXRange(0, minX, maxX - minX);

	units = new String("C");
	
	// Make the popup!
	yAxisPage = new PropPage(this);
	yAxisPage.addEdit("Max", 50);
	yAxisPage.addEdit("Min", 50);

	xAxisPage = new PropPage(this);
	xAxisPage.addEdit("Max", 50);
	xAxisPage.addEdit("Min", 50);
    }

    public void updateProp(PropPage pp, int action)
    {
	if(pp == yAxisPage){
	    switch(action){
	    case PropPage.UPDATE:
		maxY = Convert.toFloat(((Edit)(pp.props.get(0))).getText());
		minY = Convert.toFloat(((Edit)(pp.props.get(1))).getText());
		graph.setYRange(minY, maxY - minY);
		repaint();
		break;
	    case PropPage.REFRESH:
		maxY = lGraph.yaxis.min + (float)lGraph.yaxis.length/lGraph.yaxis.scale;
		minY = lGraph.yaxis.min;
		((Edit)(pp.props.get(0))).setText(maxY+ "");
		((Edit)(pp.props.get(1))).setText(minY + "");
		break;
	    }
	} else if(pp == xAxisPage){
	    switch(action){
	    case PropPage.UPDATE:
		maxX = Convert.toFloat(((Edit)(pp.props.get(0))).getText());
		minX = Convert.toFloat(((Edit)(pp.props.get(1))).getText());
		graph.setXRange(minX, maxX - minX);
		repaint();
		break;
	    case PropPage.REFRESH:
		maxX = lGraph.xaxis.min + (float)lGraph.xaxis.dispLen/lGraph.xaxis.scale;
		minX = lGraph.xaxis.min;
		((Edit)(pp.props.get(0))).setText(maxX + "");
		((Edit)(pp.props.get(1))).setText(minX + "");
		break;
	    }
	} 
    }

    boolean autoScroll = true;
    float scrollFract = (float)0.25;
    float scrollStepSize = (float)0.15;
    int scrollSteps = 5;

    public boolean addPoint(Object bin, float x, float y, boolean plot)
    {	    
	float range;
	float scrollEnd;

	if(!enabled){
	    return false;
	}

	// Plot data
	if(!lGraph.addPoint(bin, x, y)){
	    return false;
	}

	if(plot){
	    if(lGraph.maxX > (lGraph.xaxis.dispMin + (float)lGraph.xaxis.dispLen / lGraph.xScale ) ||
	       lGraph.xaxis.drawnX == -1){
		// scroll
		range = lGraph.xRange;
		scrollEnd = lGraph.maxX - range * scrollFract;
		//		System.out.println("xRange: " + lGraph.xRange + ", scrollEnd: " + scrollEnd);
		if(scrollEnd < (float)0)
		    scrollEnd = (float)0;
		while((lGraph.xaxis.dispMin < scrollEnd) || 
		      (lGraph.xaxis.drawnX > (lGraph.xOriginOff + 4)) ||
		      (lGraph.xaxis.drawnX == -1)){
		    lGraph.scroll((int)(lGraph.dwWidth * scrollStepSize));
		    draw();
		}
	    } else {
		graph.plot(myG);
	    }
	} 
	    
	return true;
    }

    int downX, downY, dragX, dragY;
    boolean xAxisDown, yAxisDown, graphDown, barDown;
    Timer timer = null;

    public void onEvent(Event e)
    {
	PenEvent pe;
	int moveX, moveY;
	float xChange;
	float yChange;
	int i;

	if(e.target == this){
	    if(e instanceof PenEvent){
		pe = (PenEvent)e;
		switch(e.type){
		case PenEvent.PEN_DOWN:
		    xAxisDown = yAxisDown = graphDown = false;
		    if(pe.y > lGraph.yOriginOff && pe.x > lGraph.xOriginOff){
			xAxisDown = true;
		    } else if(pe.y < lGraph.yOriginOff && pe.x < lGraph.xOriginOff){
			yAxisDown = true;		
		    } else {
			if(mode == ANNOT_MODE){
			    curAnnot = lGraph.addAnnot("" + curChar, pe.x);
			    curChar++;
			    draw();
			    if(curAnnot != null)
				postEvent(new Event(1002, this, 0));
			    return;
			} else {
			    graphDown = true;
			}
		    }

		    if(timer == null)
			timer = addTimer(750);
		    
		    downX = pe.x;
		    downY = pe.y;
		    dragY = 0;
		    dragX = 0;
		    break;
		case PenEvent.PEN_DRAG:
		case PenEvent.PEN_UP:
		    moveX = pe.x - downX;
		    moveY = pe.y - downY;
		    if(moveX < 0)
			dragX -= moveX;
		    else
			dragX += moveX;
		    if(moveY < 0)
			dragY -= moveY;
		    else
			dragY += moveY;

		    if(graphDown){
			if(timer != null){
			    removeTimer(timer);
			    timer = null;
			}
			yChange = -moveY / lGraph.yaxis.scale;
			lGraph.scroll(-moveX);
			lGraph.setYMin(lGraph.yaxis.dispMin + yChange);
			draw();
		    } else if(dragY > 10 || dragX > 10){ 
			if(timer != null){
			    removeTimer(timer);
			    timer = null;
			}
			if(yAxisDown){
			    if(lGraph.yOriginOff - pe.y > 20){
				yChange = (float)(lGraph.yOriginOff - pe.y)/ (float)(lGraph.yOriginOff - downY);
				
				lGraph.setYscale(lGraph.yaxis.scale * yChange);
				
				draw();
			    }
			}else if(xAxisDown && graph == lGraph){
			    if(pe.x - lGraph.xOriginOff > 20){
				xChange = (float)(lGraph.xOriginOff - pe.x)/ (float)(lGraph.xOriginOff - downX);
				lGraph.setXscale(lGraph.xScale * xChange);
				draw();
			    }
			}
		    }
		     
		    downX = pe.x;
		    downY = pe.y;
		    if(e.type == PenEvent.PEN_UP){
			graphDown = false;
			xAxisDown = false;
			yAxisDown = false;
			barDown = false;
			if(timer != null){
			    removeTimer(timer);
			    timer = null;
			}
		    }
		    break;
		}		
	    } else if(e.type == ControlEvent.TIMER){
		if(timer != null){
		    removeTimer(timer);
		    timer = null;
		} else {
		    // We have already cleared the timer so ignore this 
		    return;
		}
		if(xAxisDown)
		    xAxisPage.showProp();
		if(yAxisDown)
		    yAxisPage.showProp();
	    }
	}
    }
	
}












