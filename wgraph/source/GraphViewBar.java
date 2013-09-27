package graph;

import waba.ui.*;
import waba.fx.*;
import waba.util.*;
import waba.sys.*;

public class GraphViewBar extends GraphView
{
    final static float DEFAULT_RANGE = (float)30.0;
    final static float DEFAULT_MIN = (float)10.0;

    public float range, minValue;
    public String units = null;

    int numBars;
    int length = 0;

    PropPage yAxisPage;
    BarGraph bGraph;

    public GraphViewBar(int w, int h)
    {
	super(w,h);

	range = DEFAULT_RANGE;
	minValue = DEFAULT_MIN;

	graph = bGraph = new BarGraph(w, h);
	graph.setYRange(minValue, range);
	
	units = new String("C");
	
	numBars = 0;	
	
	// Make the popup!
	yAxisPage = new PropPage(this);
	yAxisPage.addEdit("Max", 50);
	yAxisPage.addEdit("Min", 50);
    }

    public void updateProp(PropPage pp, int action)
    {
	if(pp == yAxisPage){
	    switch(action){
	    case PropPage.UPDATE:
		minValue = Convert.toFloat(((Edit)(pp.props.get(0))).getText());
		range = Convert.toFloat(((Edit)(pp.props.get(1))).getText()) - minValue;
		graph.setYRange(minValue, range);
		length = 0;
		repaint();
		break;
	    case PropPage.REFRESH:
		((Edit)(pp.props.get(0))).setText(minValue + range + "");
		((Edit)(pp.props.get(1))).setText(minValue + "");
		break;
	    }
	}

    }

    boolean barDown, yAxisDown;
    Bar selBar = null;
    int downX, downY, dragX, dragY;

    public void onEvent(Event e)
    {
	PenEvent pe = null;
	int i;
	int moveX, moveY;
	float xChange;
	float yChange;

	if(e.target == this){
	    if(e instanceof PenEvent){
		pe = (PenEvent)e;
		switch(e.type){
		case PenEvent.PEN_DOWN:
		    barDown = yAxisDown = false;
		    if(pe.y < bGraph.yOriginOff && pe.x < bGraph.xOriginOff){
			yAxisDown = true;		
		    } else {
			Bar oldBar = selBar;
			selBar = null;

			for(i=0; i<bGraph.barSet.nBars; i++){
			    bGraph.barSet.barSel[i] = false;
			    if(!yAxisDown && 
			       (pe.x > bGraph.barSet.barPos[i] && pe.x < 
				(bGraph.barSet.barPos[i] + bGraph.barSet.barWidth))){
				bGraph.barSet.barSel[i] = true;
				selBar = (Bar)bGraph.bars.get(i);
				barDown = true;
			    }
			}
			
			if(oldBar != selBar)
			    draw();

			if(selBar != null)
			    postEvent(new Event(1000, this, 0));

		    }
		    
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

		    if(yAxisDown && (dragY > 10 || dragX > 10)){ 
			if(bGraph.yOriginOff - pe.y > 20){
			    yChange = (float)(bGraph.yOriginOff - pe.y)/ (float)(bGraph.yOriginOff - downY);
			    
			    bGraph.yaxis.setScale(bGraph.yaxis.scale * yChange);
			    draw();
			}
		    }

		    downX = pe.x;
		    downY = pe.y;
		    if(e.type == PenEvent.PEN_UP){
			yAxisDown = false;
			barDown = false;
			postEvent(new Event(1001, this, 0));
		    }
		    break;		    
		}
	    }
	}

    }

    /*
    public void setupBarGraph()
    {
	int i;

	bGraph.removeAllBins();
	selBarIndex = -1;
	Object [] annotObjs = lGraph.annots.toObjectArray();
	Annotation a;
	for(i=0; i<annotObjs.length; i++){
	    a = (Annotation)annotObjs[i];
	    a.bin = bGraph.addBin(0, a.label);
	    bGraph.addPoint(a.bin, 1, a.value);
	}
	curBarBin = bGraph.addBin(0, "Probe");
	bGraph.addPoint(curBarBin, 1, curY);
    }
    */



}












