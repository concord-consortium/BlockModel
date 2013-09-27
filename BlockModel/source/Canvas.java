import waba.ui.*;
import waba.util.*;
import waba.fx.*;
import waba.io.*;
import graph.*;

public class Canvas extends Control implements IKeys
{
    Layer [] layers;
    Vector curObjects;
    int offX, offY;
    int dragX, dragY;
    //    Graphics myG = null;
    boolean live = false;
    PropWindow prop;
    Timer downTimer;

    ColorAxis axis = null;
    Graph2D graph = null;
    GraphView gv = null;
    ThermalPlane tp = null;

    static CanvasObject selected;

    public Canvas()
    {
	curObjects = new Vector();
	offX = -1;
	offY = -1;
	dragX = -1;
	dragY = -1;	
    }

    public Canvas(int w, int h, int numLayers)
    {
	this();

	width = w;
	height = h;

	setupCanvas(numLayers);
    }

    void setupCanvas(int numLayers)
    {
	int i;
	Layer l;
	layers = new Layer [numLayers];

	for(i=0; i<numLayers; i++){
	    l = layers[i] = new Layer();
	    l.index = i;
	    l.canvas = this;
	    l.width = width - 2;
	    l.height = height - 2;
	}
	prop = new PropWindow();   
    }

    public void writeExt(DataStream ds)
    {
	int i;
	Object [] objList;

	ds.writeString("Canvas");
	ds.writeShort(x);
	ds.writeShort(y);
	ds.writeShort(width);
	ds.writeShort(height);
	ds.writeByte(layers.length);
	for(i=0; i<layers.length; i++){
	    ds.writeInt(layers[i].gridSpace);
	    ds.writeInt(layers[i].gridDist);
	}

	ds.writeBoolean(live);

	objList = curObjects.toObjectArray();
	ds.writeShort(objList.length);

	for(i=0; i<objList.length; i++){
	    ds.writeByte(((CanvasObject)objList[i]).layer.index);
	    ((CanvasObject)objList[i]).writeExt(ds);
	}	
    }

    Vector newObjs = null; 
    // This is a hack until there is some "Class.forName" funct
    // in waba
    public boolean readCanvasObj(DataStream ds)
    {
	CanvasObject ret = null;
	
	int layer = (int)ds.readByte();

	String name = ds.readString();
	if(name.equals("BlockObject")){
	    ret = (CanvasObject)new BlockObject();
	} else if(name.equals("VProbeObject")){
	    ret = (CanvasObject)new VProbeObject();
	} else if(name.equals("ModelObject")){
	    ret = (CanvasObject)new ModelObject();
	} else if(name.equals("GarbageObject")){
	    ret = (GarbageObject)new GarbageObject();
	}
	if(ret == null){
	    return false;
	}

	ret.readExt(ds);
	newObjs.add(ret);
	addObject(ret, layers[layer]);
	return true;
    }

    public void readCanvasObjs(DataStream ds)
    {
	int numObj = ds.readShort();
	CanvasObject co;

	newObjs = new Vector();

	for(int i=0; i < numObj; i++){
	    if(!readCanvasObj(ds)){
		return;
	    }
	}
    }


    public Vector readExt(DataStream ds)
    {
	int numLayers;
	int numObjs;
	int i;
	int layerIndex;
	
	String name = ds.readString();
	if(!name.equals("Canvas")){
	    return null;
	}

	x = (int)ds.readShort();
	y = (int)ds.readShort();
	width = (int)ds.readShort();
	height = (int)ds.readShort();
	
	numLayers = (int)ds.readByte();
	setupCanvas(numLayers);
	for(i=0; i<layers.length; i++){
	    layers[i].gridSpace = ds.readInt();
	    layers[i].gridDist = ds.readInt();
	}

	live = ds.readBoolean();

	readCanvasObjs(ds);

	return newObjs;
    }

    public void setPos(int x, int y)
    {
	setRect(x, y, width, height);
    }

    public boolean visable()
    {
	// are we visible
	return enabled;
    }

    public Layer getLayer(int i)
    {
	return layers[i];
    }

    public void interactAll(CanvasObject o, int action)
    {
	int i;
	Object [] objList;

	objList = curObjects.toObjectArray();
	for(i=0; i<objList.length; i++){
	    ((CanvasObject)objList[i]).interact(o, action);
	}

	/*
	 *  I don't think this should be here
	 *  but lets see what happens when I take it out
	 */
	if(gv != null)
	    gv.plot();
	
    }

    public boolean addObject(CanvasObject o, Layer l)
    {
	CanvasObject testO;

	// find space for this object
	testO = (CanvasObject)o.copy();
	if(l == null){
	    l = layers[0];
	}
	if(testO.x == -1 || testO.y == -1){
	    l.findSpace(testO);
	}

	return addObject(o, l, testO.x, testO.y);
    }

    public boolean addObject(CanvasObject o, Layer l, int x, int y)
    {
	o.x = x;
	o.y = y;

	if(l == null){
	    l = layers[0];
	}

	if(l.addObject(o)){
	    // its a valid completed add, notify objects
	    o.canvas = this;
	    interactAll(o, CanvasObject.ADD);
	    
	    curObjects.add(o);
	    repaintLayers(o, 0);
	    return true;
	}

	return false;
    }


    public void update()
    {
	int i;
	Graphics myG = null;

	myG = createGraphics();

	if(!visable() || myG == null) return;
	myG.translate(1,1);

	for(i=0; i<layers.length; i++){
	    layers[i].redrawAll(myG);
	}
	myG.free();
    }	

    public void onPaint(Graphics g)
    {
	int i;
	Graphics myG = g;

	if(myG == null){
	    return;
	}
	myG.setColor(255,255,255);
	myG.fillRect(0,0,width,height);
	myG.setColor(0,0,0);
	myG.drawRect(0,0,width,height);
	myG.translate(1,1);
	
	for(i=0; i<layers.length; i++){
	    layers[i].redrawAll(myG);
	}

	myG.translate(-1,-1);
    }

    public static void setSelected(CanvasObject sel)
    {
	if(sel != selected && selected != null){		
	    selected.select(false);
	    if(selected.canvas != null){
		selected.canvas.repaintLayers(selected, -1);
	    }
	}	  
	selected = sel;
	
	if(selected != null){
	    selected.select(true);
	    if(selected.canvas != null){
		selected.canvas.repaintLayers(selected, -1);
	    }
	}

    }

    boolean checkSelected()
    {
	return (selected != null) && (selected.canvas == this);
    }

    public void removeObject(CanvasObject o)
    {

	if(o.layer.removeObject(o)){
	    // its a valid completed remove, notify objects
	    curObjects.del(curObjects.find(o));
	    repaintLayers(o, -1);

	    o.canvas = null;
	    interactAll(o, CanvasObject.REMOVE);
	    
	}	    

    }

    public void removeAll()
    {
	int i;
	Object [] objList;

	objList = curObjects.toObjectArray();
	for(i=0; i<objList.length; i++){
	    removeObject((CanvasObject)objList[i]);
	}
    }

    public void propSelected()
    {
	if(checkSelected()){
	    if(!selected.action()){
		selected.setupPropPage();
		if(selected.pp != null){
		    selected.pp.showProp();
		}
		// It's up the object to clear the prop page
	    }
	}
    }

    public void rotateSelected(int rot)
    {
	CanvasObject oldO;

	if(!checkSelected()){
	    // no object
	    return;
	}

	oldO = selected.copy();
	if(selected.layer.rotateObject(selected, rot)){
	    // it's a valid rotate
	    // erase old object
	    repaintLayers(oldO, -1);
	    
	    // redraw object
	    repaintLayers(selected, 0);

	    interactAll(selected, CanvasObject.MOVE);
	}
    }

    void repaintLayers(CanvasObject o, int firstLayer)
    {
	int i;
	Graphics myG = null;

	myG = createGraphics();

	if(!visable() || myG == null) return;
	myG.translate(1,1);

	// need to add clipping regions
	myG.setClip(o.x,o.y,o.width,o.height);
	if(firstLayer == -1){
	    myG.setColor(255,255,255);
	    myG.fillRect(o.x, o.y, o.width, o.height);
	    firstLayer++;
	}

	for(i=firstLayer; i<layers.length; i++){
	    layers[i].redraw(myG, o);
	}
	myG.free();
	
    }


    public void moveSelected(int x, int y)
    {
	int offX, offY;
	CanvasObject oldO;

	if(!checkSelected()){
	    return;
	}

	eraseOldDrag();

	oldO = selected.copy();
	oldO.x = selected.x;
	oldO.y = selected.y;
	if(selected.layer.moveObject(selected, x, y)){
	    // its a valid move
	    // erase old object
	    repaintLayers(oldO, -1);
	    
	    // redraw object
	    repaintLayers(selected, -1);
	    
	    interactAll(selected, CanvasObject.MOVE);

	}
    }	
	
    void clear(CanvasObject o){
	Graphics myG = null;
       
	myG = createGraphics();

	if(!visable()) return;
	myG.translate(1,1);

	if(myG != null){
	    myG.setColor(255,255,255);
	    myG.fillRect(o.x,o.y,o.width,o.height);
	}
	myG.free();
    }

    void eraseOldDrag()
    {
	CanvasObject oldPos;

	if((dragX != -1 || dragY != -1) && selected != null){
	    // there was a previous drag
	    oldPos = (CanvasObject)selected.copy();
	    oldPos.x = dragX;
	    oldPos.y = dragY;
	    repaintLayers(oldPos, -1);
	}
	dragX = -1;
	dragY = -1;
    }

    public void onEvent(Event e)
    {		
	int i;
	PenEvent pe;
	CanvasObject newSel;

	if(e.type == PenEvent.PEN_DOWN && e.target == this){
	    pe = (PenEvent)e;
	    newSel = null;
	    for(i=layers.length-1; i>=0; i--){
		if((newSel = layers[i].getObject(pe.x, pe.y)) 
		   != null){
		    break;
		}
	    }

	    setSelected(newSel);
	    if(selected != null){
		offX = pe.x - selected.x;
		offY = pe.y - selected.y;
	    }
	    
	    downTimer = addTimer(1000);
	} else if(e.type == ControlEvent.TIMER && e.target == this){
	    // popup window
	    if(downTimer != null){
		removeTimer(downTimer);
		downTimer = null;
	    }
	    propSelected();

	} else if(e.type == PenEvent.PEN_DRAG && e.target == this){
	    if(selected != null){
		pe = (PenEvent)e;
		CanvasObject check;

		int oldx = pe.x - offX;
		int oldy = pe.y - offY;
		if(!selected.checkMove(oldx, oldy)){
		    return;
		}
		if(downTimer != null){
		    removeTimer(downTimer);
		    downTimer = null;
		}
		
		check = layers[0].checkObject(selected, oldx, oldy);
		if(check != layers[0].border){
		    eraseOldDrag();
		    
		    dragX = oldx;
		    dragY = oldy;
		    Graphics tmpG = createGraphics();
		    tmpG.translate(1,1);
		    selected.drawDrag(tmpG, dragX, dragY);
		    tmpG.free();
		} else {
		    Rect rect = getAbsoluteRect();
		    int absX, absY;
		    absX = pe.x + rect.x;
		    absY = pe.y + rect.y;
		    Control under = MainWindow.getMainWindow().findChild(absX, absY);

		    if(under == null || 
		       under == this ||
		       !(under instanceof Canvas)){
			// do nothing 
			return;
		    } else {
			eraseOldDrag();

			Canvas newCan = (Canvas)under;
			newCan.dragX = -1;
			newCan.dragY = -1;
			newCan.offX = offX;
			newCan.offY = offY;
			MainWindow.getMainWindow().setFocus(under);
		    }		    

		}
	    }
	} else if(e.type == PenEvent.PEN_UP && e.target == this){	    
	    if(downTimer != null){
		removeTimer(downTimer);
		downTimer = null;
	    }
		
	    if(selected != null){
		pe = (PenEvent)e;
		if(selected.canvas != this){
		    eraseOldDrag();
		    CanvasObject cpy = null;
		    Layer targetLayer = layers[selected.targetLayerIndex];
		    if(selected.dragAction == selected.EXT_DRAG_COPY){
			cpy = selected.copy();
			if(addObject(cpy, targetLayer, 
				     pe.x - offX, pe.y - offY)) {
			    setSelected(cpy);
			}
		    } else if(selected.dragAction == selected.EXT_DRAG_MOVE){
			if(targetLayer.checkObject(selected, pe.x - offX, pe.y - offY)
			   == null){
			    // its a valid move
			    // erase old object
			    selected.canvas.removeObject(selected);
			    addObject(selected, targetLayer, 
				      pe.x - offX, pe.y - offY); 
			}
		    }
		} else {
		    moveSelected(pe.x - offX, pe.y - offY);
		}
		offX = -1;
		offY = -1;

	    }
	    
	} else if(e.type == KeyEvent.KEY_PRESS) {
	    KeyEvent ke = (KeyEvent)e;
	    if(selected != null){
		int curX = selected.x;
		int curY = selected.y;
		int gridSpace = selected.layer.gridSpace;
		switch(ke.key){
		case UP:
		    moveSelected(curX, curY-gridSpace);
		    break;
		case DOWN:
		    moveSelected(curX, curY+gridSpace);
		    break;
		case IKeys.LEFT:
		    moveSelected(curX-gridSpace, curY);
		    break;
		case IKeys.RIGHT:
		    moveSelected(curX+gridSpace, curY);
		    break;
		default:
		}
	    }
	}
    }
}














