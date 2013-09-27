package graph;

import waba.ui.*;
import waba.fx.*;
import waba.util.*;
import waba.sys.*;

public class AnnotView extends Container implements PropObject
{
    public GraphView curView = null;
    public GraphViewLine lgView = null;
    GraphViewBar bgView = null;

    Object lgBins [] = new Object [1];
    Object bgBins [] = new Object [1];

    int buttonSpace = 20;

    PropPage annotPage;

    Button annotButton = new Button("Add Data Pt");
    Button delButton = new Button("Del Data Pt");
    Button viewButton = new Button("View Data Pts");
    Label modeLabel = new Label(GraphViewLine.DRAG_MODE + "");

    public AnnotView(int w, int h)
    {
	width = w;
	height = h;

	int butStart = h - buttonSpace + 1;

	curView = lgView = new GraphViewLine(w, h - buttonSpace);
	bgView = new GraphViewBar(w, h - buttonSpace);	
	lgView.setPos(0,0);
	bgView.setPos(0,0);
	
	lgBins [0] = lgView.lGraph.addBin(0, "Temp");
	bgBins [0] = bgView.bGraph.addBin(0, "Probe");
	
	add(curView);

	annotButton.setRect(11, butStart, 70, 17);
	add(annotButton);

	delButton.setRect(82, butStart, 70, 17);
	add(delButton);
	delButton.setVisible(false);

	viewButton.setRect(153, butStart, 82, 17);
	add(viewButton);

	modeLabel.setRect(1, butStart + 2, 10, 17);
	add(modeLabel);

	annotPage = new PropPage(this);
	annotPage.addEdit("Label", 30);
	annotPage.addEdit("Notes", 60);

    }

    float minX, maxX;

    public void setRange(float minX, float maxX, float minY, float maxY)
    {
	lgView.lGraph.setYRange(minY, maxY - minY);
	lgView.lGraph.setXRange(0, minX, maxX - minX);

	bgView.bGraph.setYRange(minY, maxY - minY);
	
	this.minX = minX;
	this.maxX = maxX;

    }

    public void setPos(int x, int y)
    {
	setRect(x,y,width,height);
    }

    public void updateProp(PropPage pp, int action)
    {
	if(pp == annotPage){
	    if(bgView.selBar == null)
		return;

	    Annotation a = (Annotation)lgView.lGraph.annots.get(bgView.selBar.index);
	    if(a == null)
		return;

	    switch(action){
	    case PropPage.UPDATE:
		a.label = ((Edit)(pp.props.get(0))).getText();
		a.text = ((Edit)(pp.props.get(1))).getText();
		bgView.bGraph.barSet.labels[((int [])(a.bin))[0]].text = a.label;
		repaint();
		break;
	    case PropPage.REFRESH:
		((Edit)(pp.props.get(0))).setText(a.label);
		if(a.text != null)
		    ((Edit)(pp.props.get(1))).setText(a.text);
		break;
	    }
	}
    }

    public void reset()
    {
	lgView.lGraph.reset();
	bgView.bGraph.removeAllBins();

	bgBins [0] = bgView.bGraph.addBin(0, "Probe");
	bgView.bGraph.addPoint(bgBins[0], 1, curY);

	length = (float)0;

	lgView.lGraph.setXRange(0, minX, maxX - minX);

	// repaint
	repaint();
    }

    public void pause()
    {
	lgView.lGraph.endCollection();
	length = (float)0.0;
	curView.draw();
    }

    boolean barDown = false;
    Timer timer = null;

    public void onEvent(Event e)
    {
	int i;
	Annotation a = null;

	if((e.type == ControlEvent.PRESSED)){
	    if(e.target == annotButton){
		if(curView == lgView){
		    lgView.mode = lgView.ANNOT_MODE;
		    modeLabel.setText(lgView.ANNOT_MODE + "");
		} else {
		    a = lgView.lGraph.addAnnot("" + lgView.curChar, (float)(length-1));
		    // Add bar to bargraph
		    a.bin = bgView.bGraph.addBin(0, "" + lgView.curChar);
		    bgView.bGraph.addPoint(a.bin, 1, a.value);
		    lgView.curChar++;

		    repaint();
		}	    
	    } else if(e.target == viewButton){
		if(curView == lgView){
		    viewButton.setText("View Line Graph");

		    // copy settings from lineView to barview
		    bgView.bGraph.yaxis.dispMin = lgView.lGraph.yaxis.dispMin;
		    bgView.bGraph.yaxis.setScale(lgView.lGraph.yaxis.scale);
		    
		    remove(curView);
		    curView = bgView;
		    add(curView);

		    delButton.setVisible(true);
		    repaint();
		} else {
		    viewButton.setText("View Data Pts");
		    
		    remove(curView);
		    curView = lgView;
		    add(curView);

		    delButton.setVisible(false);
		    repaint();
		}

	    } else if(e.target == delButton) {
		if(bgView.selBar == null)
		    return;

		a = (Annotation)lgView.lGraph.annots.get(bgView.selBar.index - 1);
		lgView.lGraph.annots.del(bgView.selBar.index - 1);
		bgView.selBar = null;

		bgView.bGraph.removeBin(a.bin);
		repaint();
	    }

	} else if(e.type == 1000 && e.target == bgView) {
	    // Bar down event 

	    if(bgView.selBar != null && bgView.selBar.index == 0){
		bgView.bGraph.barSet.barSel[0] = false;
		bgView.selBar = null;
		
		repaint();
	    } else {
		barDown = true;
	    }
	} else if(e.type == 1001 && e.target == bgView) {
	    // Bar up event
	    barDown = false;
	    if(timer != null){
		removeTimer(timer);
		timer = null;
	    }
	} else if(e.type == 1002 && e.target == lgView) {
	    // Annotation added event
	    lgView.mode = lgView.DRAG_MODE;
	    modeLabel.setText(lgView.DRAG_MODE + "");
	    a = lgView.curAnnot;
	    if(a == null) return;

	    // Add bar to bargraph
	    a.bin = bgView.bGraph.addBin(0, a.label);
	    bgView.bGraph.addPoint(a.bin, 1, a.value);

	} else if(e.type == ControlEvent.TIMER && e.target == this){
	    if(timer != null){
		removeTimer(timer);
		timer = null;
	    }
	    if(barDown)
	       annotPage.showProp();

	    barDown = false;
	}

    }

    public float length = (float)0;
    float curY = (float)0;
    public float xStepSize = (float)1;

    public boolean addPoint(int bin, float x, float y, boolean plot)
    {	
	if(bin == 0)
	    curY = y;

	bgView.bGraph.addPoint(bgBins[bin], length, y);
	if(!lgView.addPoint(lgBins[bin], length, y, curView == lgView)){
	    return false;
	}

	if(bin == 0){
	    length += (float)xStepSize;
	}

	if(plot){
	    curView.plot();
	}

	return true;
    }

    public void update()
    {
	curView.plot();
    }
    
}
