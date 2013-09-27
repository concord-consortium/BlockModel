import waba.fx.*;
import waba.io.*;
import waba.ui.*;
import graph.*;

class VProbeObject extends CanvasObject
{
    final static String defTempString = " ??.?";

    int rF,gF,bF;
    int rB,gB,bB;
    String label;
    boolean showTemp = false;
    CanvasObject tip = new CanvasObject();
    ThermalPatch underTip;
    String dispStr;
    CanvasObject dispCore = new CanvasObject();
    int coreW, coreH;
    int dispBorder, dispNubX, dispNubY;
    Font font = MainWindow.defaultFont;
    Graph2D graph = null;
    char curChar = 'A';
    VProbeObject parent = null;
    Object graphID = null;

    public void writeExt(DataStream ds)
    {
	ds.writeString("VProbeObject");
	super.writeExt(ds);
	ds.writeString(label);
	ds.writeByte((byte)curChar);
	ds.writeBoolean(showTemp);
	tip.writeExt(ds);  // Do we need this
	ds.writeString(dispStr);
	dispCore.writeExt(ds); // watch for null
	ds.writeShort(coreW);
	ds.writeShort(coreH);
	ds.writeShort(dispBorder);
	ds.writeShort(dispNubX);
	ds.writeShort(dispNubY);	
    }

    public void readExt(DataStream ds)
    {
	super.readExt(ds);
	label = ds.readString();
	curChar = (char)ds.readByte();
	showTemp = ds.readBoolean();
	tip = new CanvasObject();
	tip.readExt(ds);
	dispStr = ds.readString();
	dispCore = new CanvasObject();
	dispCore.readExt(ds);
	coreW = ds.readShort();
	coreH = ds.readShort();
	dispBorder = ds.readShort();
	dispNubX = ds.readShort();
	dispNubY = ds.readShort();
    }

    VProbeObject()
    {
	dispBorder = 2;
	rF = 0;
	gF = 0;
	bF = 0;
	rB = 255;
	gB = 255;
	bB = 255;
	underTip = null;
	coreW = -1;
	coreH = -1;
	orient = 0;

	width = 27;
	height = 27;
	tip.x = 0;
	tip.y = height-1;
    }

    VProbeObject(Font f, String label)
    {
	this();

	this.label = label;
	this.font = f;
	if(label == null){
	    this.label = "";
	    showTemp = true;
	}
    }

    public void copyTo(CanvasObject co)
    {
	super.copyTo(co);
	if(co instanceof VProbeObject){
	    VProbeObject me = (VProbeObject)co;
	    me.dispBorder = dispBorder;
	    me.dispCore = dispCore.copy();
	    me.dispNubX = dispNubX;
	    me.dispNubY = dispNubY;
	    me.tip = tip.copy();
	    me.parent = this;

	    me.font = font;
	    me.showTemp = showTemp;
	}
    }

    public CanvasObject copy()
    {
	VProbeObject me = new VProbeObject(font, "");
	copyTo(me);
	return me;
    }

    public CanvasObject cpyCore()
    {
	CanvasObject dispTmp;
	if(dispCore.x != -1){
	    dispTmp  = dispCore.copy();

	    dispTmp.x = x + dispCore.x;
	    dispTmp.y = y + dispCore.y;
	    
	    return dispTmp;
	} else {
	    dispTmp = this.copy();
	    dispTmp.x = x;
	    dispTmp.y = y;
	    return dispTmp;
	    
	}
    }

    public boolean overlapRect(Rect rect)
    {
	return cpyCore().overlapBounds(rect);
    }

    public boolean overlapShape(CanvasObject o)
    {
	return o.overlapRect(cpyCore());
    }

    float lastTemp = (float)-1;
	
    public float getTemp()
    {
	if(underTip != null){
	    return underTip.getTemp(x+tip.x, y+tip.y);
	} else {
	    // hmmm
	    return (float)0.0;
	}

    }
	       
    public void updateDisp()
    {
	float newTemp = 0;

	if(showTemp){
	    if(underTip != null){
		dispStr = fToString(underTip.getTemp(x+tip.x, y+tip.y), 1);
	    } else {
		dispStr = defTempString;
	    }
	} else {
	    dispStr = label;
	    if(graphID != null){
		newTemp = getTemp();
		if(lastTemp != newTemp){
		    graph.addPoint(graphID, 0, newTemp);
		    lastTemp = newTemp;
		}
	    }
	}

	if(coreW == -1){
	    // Uninizalized core dimensions
	    FontMetrics fm = canvas.getFontMetrics(font);		
	    coreW =  fm.getTextWidth(dispStr) + 1 + dispBorder*2;
	    coreH = fm.getHeight() + dispBorder*2;
	}


    }


    public void draw(Graphics g)
    {
	updateDisp();
	if(dispCore.x  == -1){
	    dispCore.width =  coreW;
	    dispCore.height = coreH;

	    if(orient == 0 || orient == 3){
		if(orient == 0){
		    dispCore.x = width - dispCore.width;
		} else {
		    dispCore.x = 0;
		}
		dispCore.y = 0;	    
		dispNubY = dispCore.y+dispCore.height;
	    } else {
		if(orient == 1){
		    dispCore.x= width - dispCore.width;
		} else {
		    dispCore.x = 0;
		}
		dispCore.y = height - dispCore.height;
		dispNubY = dispCore.y;
	    }
	    dispNubX = dispCore.x+dispCore.width/2;
	}

	if(g != null){

	    g.setColor(rF,gF,bF);
	    g.drawLine(x+tip.x,y+tip.y,x+dispNubX,y+dispNubY);
	    g.setColor(255,255,255);
	    g.fillRect(x+dispCore.x,y+dispCore.y,dispCore.width,dispCore.height);
	    g.setColor(0,0,0);
	    g.drawText(dispStr,x+dispCore.x+dispBorder,y+dispCore.y+dispBorder);
	    if(selected){
		// draw black line 
		g.setColor(0,0,0);
		g.drawRect(x+dispCore.x+1,y+dispCore.y+1,
			   dispCore.width-2,dispCore.height-2);
	    } else {
		g.setColor(150,150,150);
	    }
	    g.drawRect(x+dispCore.x,y+dispCore.y,dispCore.width,dispCore.height);
	}
    }

    public void drawDrag(Graphics g, int x, int y)
    {
	if(g != null){
	    // draw black line 
	    g.setColor(0,0,0);
	    g.drawRect(x+dispCore.x,y+dispCore.y, 
		       dispCore.width, dispCore.height);	    
	    g.drawLine(x+tip.x,y+tip.y,x+dispNubX,y+dispNubY);	    
	}
    }	

    void moved()
    {
	CanvasObject under;

	if(canvas != null){
	    under = canvas.layers[0].getObject(x+tip.x,y+tip.y);
	    if(under != null &&
	       under instanceof ThermalPatch){
		underTip = (ThermalPatch)under;
	    } else {
		underTip = null;
	    }
	    updateDisp();
	}	
    }

    public void move(int x, int y)
    {
	super.move(x, y);
	if(canvas != null){
	    moved();
	}
    }

    public void rotate(int rot)
    {
	if(rot > 0){
	    orient++;
	} else {
	    orient--;
	}
	orient = (orient + 4) % 4;

	tip.x = 0;
	tip.y = 0;
	switch(orient){
	case 0:
	    tip.y = height-1;
	    break;
	case 2:
	    tip.x = width-1;
	    break;
	case 3:
	    tip.x = width-1;
	    tip.y = height-1;
	    break;
	default:
	}

	dispCore.x = -1;
	moved();
    }
    
    public void add()
    {
	super.add();
	if(canvas.live && !showTemp && label.equals("") &&
	   parent != null){
	    label = parent.curChar + "";
	    parent.curChar++;
	}	    
	if(canvas.live && canvas.gv != null && !showTemp){
	    graph = canvas.graph;
	    graphID = graph.addBin(0, label);
	}

	moved();
    }
    
    public void remove()
    {
	super.remove();
	if(graph != null){
	    if(graphID != null){
		graph.removeBin(graphID);
		graphID = null;
	    }
	}
    }

    // This only works for single layer below
    public void interact(CanvasObject o, int action)
    {
	switch(action){
	case ADD:
	case MOVE:
	    CanvasObject tipTmp = tip.copy();
	    tipTmp.x += x;
	    tipTmp.y += y;

	    if(o instanceof ThermalPatch &&
	       (ThermalPatch)o == underTip){
		// we might be loosing our block
		if(!o.overlapShape(tipTmp)){
		    underTip = null;
		    redraw();

		}
	    } else if(o instanceof ThermalPatch && 
		      o.overlapShape(tipTmp)){
		underTip = (ThermalPatch)o;
		redraw();
	    }
	    break;
	case REMOVE:
	    if(o == underTip){
		// lost our block
		underTip = null;
		redraw();
	    }
	    break;
	case UPDATE:
	    if(o == underTip){
		// updating our block
		redraw();
	    }
	    break;
	}
    }

    public void redraw()
    {
	float newTemp = 0;
	// We need to watch the x point "0" 
	// perhaps we can put a curTime in the canvas
	// or use a tick() function or event
	if(graphID != null){
	    newTemp = getTemp();
	    if(newTemp != lastTemp){
		graph.addPoint(graphID, 0, newTemp);
		lastTemp = newTemp;
	    }
	}
	super.redraw();
    }	    
}







