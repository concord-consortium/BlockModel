import waba.ui.*;
import waba.fx.*;
import waba.sys.*;
import waba.util.*;
import waba.io.*;
import graph.*;

class ThermalPlane extends Container
{
    ThermalArea [] plane;
    int step;
    int pWidth, pHeight;
    ThermalArea [] nextPlane;
    Canvas canvas;
    GraphView graph = null;
    Vector objects = new Vector();
    int grid;
    int transNum;
    int transDenom;
    float heatFac = (float)1/(float)100;

    public int trans(int val){
	return val * transNum / transDenom;
    }

    /*
     * Note: the thermal area grid should be <= the blockGrid
     * if it can't be larger or a single thermal area could cover
     * two blocks.
     */
    public ThermalPlane(Canvas c, int grid, int blockGrid, int w, int h)
    {
	this.grid = grid;

	// rounded up num of thermal areas that fit in one blockGrid
	int insideNum = (blockGrid + grid - 1)/ grid;
	
	// figure multiple used in getTemp
	// this converts from a (x,y) point in the blockGrid to 
	// a point in the thermal area grid;
	transNum = insideNum;
	transDenom = blockGrid;

	// round up
	// this is screwy this width becomes the width of the container
	width  = trans(w) + 1;
	height = trans(h) + 1;
	pWidth = width+2;
	pHeight = height+2;
	plane = new ThermalArea [pWidth  * pHeight];
	nextPlane = new ThermalArea [pWidth * pHeight];
	canvas = c;

    }

    public ThermalPlane()
    {
    }

    public void readExt(DataStream ds)
    {
	x = ds.readShort();
	y = ds.readShort();
	width = ds.readShort();
	height = ds.readShort();
	grid = ds.readShort();
	transNum = ds.readShort();
	transDenom = ds.readShort();
	pWidth = width+2;
	pHeight = height+2;

	plane = new ThermalArea [pWidth  * pHeight];
	nextPlane = new ThermalArea [pWidth * pHeight];

    }
    
    public void writeExt(DataStream ds)
    {
	ds.writeShort(x);
	ds.writeShort(y);
	ds.writeShort(width);
	ds.writeShort(height);
	ds.writeShort(grid);
	ds.writeShort(transNum);
	ds.writeShort(transDenom);
	pWidth = width+2;
	pHeight = height+2;
	
    }
    
    public void setPos(int x, int y)
    {
	setRect(x,y, width,height);
    }

    /* we are going to round to our units */

    public void addPatch(ModelObject mo, int x, int y)
    {
       ThermalArea area;
	int i, j, width, height;
	int index;
	x = trans(x);
	y = trans(y);
	width = trans(mo.width);
	height = trans(mo.height);

	if(mo.temps == null){
	    for(i=0; i<width; i++){
		for(j=0; j<height; j++){
		    index = (x+i+1) + (y+j+1)*pWidth; 
		    plane[index] = new ThermalArea(mo);
		    nextPlane[index] = new ThermalArea(mo);
		}
	    }	
	} else {
	    setAllTemps(mo);
	}
	objects.add(mo);
    }

    public void removePatch(ModelObject mo, int x, int y)
    {
	int i, j, width, height;
	int index;
	x = trans(x);
	y = trans(y);
	width = trans(mo.width);
	height = trans(mo.height);

	for(i=0; i<width; i++){
	    for(j=0; j<height; j++){
		index = (x+i+1) + (y+j+1)*pWidth;
		plane[index] = null;
		nextPlane[index] = null;
	    }
	}	
	
	index = objects.find(mo);
	if(index >= 0){
	    objects.del(index);
	}
    }

    public float getTemp(int x, int y)
    {
	x =  trans(x);
	y =  trans(y);

	ThermalArea area = plane[x+1 + (y+1)*pWidth];
	if(area != null){
	    return area.temp;
	} else {
	    return (float)-1000.0;
	}
    }

    public void getAllTemps(ModelObject mo)
    {
	int i, j;
	int transWidth, transHeight;
	int transX, transY;

	mo.tempGridNum = transNum;
	mo.tempGridDenom = transDenom;
	transX = trans(mo.x) + 1;
	transY = trans(mo.y) + 1;
	transWidth = trans(mo.width);
	transHeight = trans(mo.height);
	mo.temps = new float [transWidth*transHeight];

	for(j=0; j < transHeight; j++){
	    for(i=0; i < transWidth; i++){
		mo.temps[j*transWidth + i] = 
		    plane[i+transX + (j+transY)*pWidth].temp;
	    }
	}

    }

    public void setAllTemps(ModelObject mo)
    {
	int i, j;
	int transWidth, transHeight;
	int transX, transY;
	int convNum, convDenom;
	int index;

	convDenom = mo.tempGridDenom * transNum;
	convNum = mo.tempGridNum * transDenom;

	transX = trans(mo.x) + 1;
	transY = trans(mo.y) + 1;
	transWidth = trans(mo.width);
	transHeight = trans(mo.height);
	if(convDenom == convNum){
	    for(j=0; j < transHeight; j++){
		for(i=0; i < transWidth; i++){
		    index = i+transX + (j+transY)*pWidth;
		    plane[index] = new ThermalArea(mo);
		    nextPlane[index] = new ThermalArea(mo);
		    plane[index].temp =
			mo.temps[j*transWidth + i]; 
		}
	    }
	} else {
	    for(j=0; j < transHeight; j++){
		for(i=0; i < transWidth; i++){
		    index = i+transX + (j+transY)*pWidth;
		    plane[index] = new ThermalArea(mo);
		    nextPlane[index] = new ThermalArea(mo);
		    plane[index].temp =
			mo.temps[j*transWidth*convNum/convDenom + i*convNum/convDenom]; 
		}
	    }	   
	}
    }


    Graphics myG = null;
    public void onPaint(Graphics g)
    {
	myG = g;
    }

    void printTime(int y)
    {
	myG.setColor(255,255,255);
	myG.fillRect(1,y,50,15);
	myG.setColor(0,0,0);
	myG.drawText("" + (Vm.getTimeStamp() % 10000), 1,y);
	
	
    }

    public void start()
    {
	if(timer == null){
	    timer = addTimer(25);
	    pos = 0;
	} 
    }

    public void stop()
    {
	if(timer != null){
	    removeTimer(timer);
	    timer = null;
	}
    }

    Timer timer = null;    
    int pos;

    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.TIMER &&
	   e.target == this){
	    switch(pos){
	    case 0:
	    case 1:
		step();
		step();
		step();
		step();
		step();
		step();
		step();
		step();
		break;
	    case 2:
		updateObj();

		if(graph != null){
		    graph.plot();
		}

		break;
	    }
	    pos++;
	    pos %= 3;
	}
    }

    public void removeAll()
    {
	int i;
	Object [] objArray = objects.toObjectArray();
	ModelObject mo; 
	for(i=0; i<objArray.length; i++){
	    mo = (ModelObject) objArray[i];
	    removePatch(mo, mo.x, mo.y);
	}
    }
	    
    public void updateObj()
    {
	int i;
	Object [] objArray = objects.toObjectArray();
	ModelObject mo; 
	for(i=0; i<objArray.length; i++){
	    mo = (ModelObject) objArray[i];
	    mo.updateTemp();
	    canvas.repaintLayers(mo, 0);
	}
    }

    /*
     *  This is currently a bit
     */
    public void step()
    {
	int i,j,m;
	float myS, myC, myT, nextT;
	float neiS, neiC, neiT;
	ThermalArea tmp [];
	ThermalArea me, next, neibr;
	int index, endIndex;
	i = 1;
	j = 1;
	float sumT, sumWeights, weightedAvg;
	float sumQ, avgC, neibrTemp;
	float newTemp, maxTemp, minTemp;
	int totalT;

	maxTemp = 1000;
	minTemp = -1000;

	endIndex = pWidth-1 + pWidth*(pHeight-2); 
	for(index=pWidth+1;index<endIndex; index++){
	    me  = plane[index];
	    if(me != null){
		myS = me.mo.specHeat;
		if(myS < (float)0.001){
		    continue;
		}
		myC = me.mo.conduct;
		myT = me.temp;		    
		next = nextPlane[index];
		sumT = (float)0.0;
		totalT = 0;

		neibr = plane[index -1 ];
		if(neibr != null){
		    //		    avgC = (neibr.mo.conduct + myC)/(float)2.0;
		    sumT += neibr.temp;
		    totalT++;
		}
		neibr = plane[index + 1];
		if(neibr != null){
		    // avgC = (neibr.mo.conduct + myC)/(float)2.0;
		    sumT += neibr.temp;
		    totalT++;
		}
		neibr = plane[index - pWidth];
		if(neibr != null){
		    // avgC = (neibr.mo.conduct + myC)/(float)2.0;
		    sumT += neibr.temp;
		    totalT++;
		}
		neibr = plane[index + pWidth];
		if(neibr != null){
		    // avgC = (neibr.mo.conduct + myC)/(float)2.0;
		    sumT += neibr.temp;
		    totalT++;
		}

		newTemp = (myC / myS) * heatFac * (sumT - myT * (float)totalT) + myT;
		if(newTemp > maxTemp)
		    newTemp = maxTemp;
		else if(newTemp < minTemp)
		    newTemp = minTemp;
		next.temp = newTemp; 
	    } 
	    
	}
	tmp = plane;
	plane = nextPlane;
	nextPlane = tmp;


    }

}










