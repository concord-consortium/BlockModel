import waba.fx.*;
import waba.ui.*;
import waba.sys.*;
import waba.io.*;
import graph.*;

public class CanvasObject extends Rect implements PropObject
{
    public final static int ADD = 1;
    public final static int MOVE = 2;
    public final static int REMOVE = 3;
    public final static int UPDATE = 4;

    public final static int EXT_DRAG_NO = 0;
    public final static int EXT_DRAG_COPY = 1;
    public final static int EXT_DRAG_MOVE = 2;

    boolean model;
    Layer layer;   
    boolean selected;
    int orient;
    Canvas canvas;
    PropPage pp = null;
    boolean disposable = true;
    int dragAction = EXT_DRAG_NO;
    int targetLayerIndex = 0;
    boolean rectangular = true;

    public CanvasObject()
    {
	super(-1,-1,1,1);

	canvas = null;
	orient = 0;
	selected = false;
	pp = null;
    }

    public void writeExt(DataStream ds)
    {
	int tLayerIndex = -1;

	ds.writeShort(x);
	ds.writeShort(y);
	ds.writeShort(width);
	ds.writeShort(height);
	ds.writeByte(orient);
	ds.writeBoolean(disposable);
	ds.writeByte(dragAction);
	ds.writeByte(targetLayerIndex);
    }

    public void readExt(DataStream ds)
    {
	int tLayerIndex = -1;

	x = ds.readShort();
	y = ds.readShort();
	width = ds.readShort();
	height = ds.readShort();
	orient = ds.readByte();
	disposable = ds.readBoolean();
	dragAction = ds.readByte();
	targetLayerIndex = ds.readByte();

    }

    public boolean checkMove(int x, int y)
    {

	x = (x+layer.gridDist)/layer.gridSpace*layer.gridSpace;
	y = (y+layer.gridDist)/layer.gridSpace*layer.gridSpace;

	if(x == this.x && y == this.y){
	    return false;
	}

	return true;
    }

    public static float floatValue(String s)
    {
	return Convert.toFloat(s);
    }

    public static String fToString(float f, int dec)
    {
	return Convert.toString(f + "", dec);
    }

    public void setupPropPage(){}

    public void updateProp(PropPage pp, int action){}

    public String toString()
    {
	return "(" + x + ", " + y + ", " + width +", " + height + ")"; 
    }

    public void interact(CanvasObject o, int action){}

    // Check if we overlap the bounding boxes
    public boolean overlapBounds(Rect o)
    {
	if(((o.x + o.width) > x && o.x <= (x+width-1)) && 
	   ((o.y + o.height) > y && o.y <= (y+height-1))){
	    return true;
	}

	return false;
    }

    public boolean overlapRect(Rect rect)
    {
	return overlapBounds(rect);
    }

    // Overlap the solid shape of the object
    public boolean overlapShape(CanvasObject o)
    {
	// We are rectangular are they?
	return o.overlapRect(this);
    }

    public void copyTo(CanvasObject co)
    {
	co.x = x;
	co.y = y;
	co.width = width;
	co.height = height;
	co.model = model;
	co.layer = layer;
	co.orient = orient;
    }

    public CanvasObject copy()
    {
	CanvasObject me = new CanvasObject();
	this.copyTo(me);

	return me;
    }

    public void select(boolean on)
    {
	selected = on;
    }

    public boolean action()
    {
	return false;
    }
    
    public void move(int x, int y)
    {
	this.x = x;
	this.y = y;
    }

    public void rotate(int rot)
    {
	int oldX, oldY, oldW, oldH;
	int oldOrient = orient;

	oldX = x;
	oldY = y;
	oldW = width;
	oldH = height;

	orient += rot;
	orient = (orient + 4) % 4;

	if((((oldOrient - orient) + 4) % 2) == 1){
	    x = oldX + oldW/2 - oldH/2;
	    y = oldY + oldH/2 - oldW/2;
	    width = oldH;
	    height = oldW;
	} 
	    

    }

    public void move(int rot, int x, int y)
    {
	rotate(rot);
	move(x, y);
    }

    public void add(){
    }
    
    public void remove(){}

    public void redraw()
    {
	if(canvas != null){	    
	    canvas.repaintLayers(this, 0);
	}
    }

    public void draw(Graphics g)
    {
    }

    public void drawDrag(Graphics g, int x, int y)
    {
	if(g != null){
	    // draw black line 
	    g.setColor(0,0,0);
	    g.drawRect(x,y,width,height);
	    
	}
    }	

}












