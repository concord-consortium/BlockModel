package graph;

import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import waba.sys.*;
import waba.util.*;

class Bin
{
    public final static int START_DATA_SIZE = 10000;

    int [] points;
    float [] values;
    int numPoints;
    int lastPlottedPoint;
    int c;
    int collection;
    Axis xaxis;
    int [] binPtr;
    int lastPlottedY;
    int lastPlottedX;
    int curX;
    int sumY;
    int numXs;

    public Bin()
    {
	points = new int [START_DATA_SIZE*2];
	values = new float [START_DATA_SIZE*2];
	numPoints = 0;
	lastPlottedPoint = -1;
    }
}
    
public class LineGraph extends Graph2D
{
    public final static int DEFAULT_STOR_SIZE = 5;

    public final static float X_MIN = 0;
    public final static float X_MAX = 100;
    public final static float Y_MIN = -200;
    public final static float Y_MAX = 200;

    boolean palm = false;
    public int annotTopY = 3;
    public int annotPadding = 1;
    public int topPadding = 5 + Annotation.height + annotPadding;
    public int rightPadding = 8;

    int xOriginOff, yOriginOff;
    int dwWidth, dwHeight;
    public Axis xaxis = null;
    public Axis [] xaxisArray = new Axis [10];
    Axis firstXaxis = null;
    int numXaxis;
    float xRange;
    float xScale, xScaleLast;
    float yScale;

    public ColorAxis yaxis = null;
    int platform = 0;

    int width, height;
    public float minX, minY, maxX, maxY;

    int [] lineColors = { 255, 0, 0,   // red
			  0, 255, 0,   // green
			  0, 0, 255,   // blue
			  255, 255, 0, // yellow
			  255, 0, 255, // purple
			  0, 255, 255,}; // turquois

    protected int length = 0;

    // The active bins
    Vector activeBins = new Vector();
    int numBins = 0;
    int binStorSize = 0;

    Image buffer = null;
    JGraphics bufG;

    Vector annots = new Vector();
    Bin curBin;
    Bin binArray [];

    TextLine yExpLabel = new TextLine("", TextLine.UP);

    public LineGraph(int w, int h)
    {
	int i;
	width = w;
	height = h;

	dwWidth = w - 40 - 10;
	dwHeight = h - 30 - 10;
	xaxis = new Axis(X_MIN, X_MAX, dwWidth);
	xaxis.ticDir = 1;
	xaxis.orient = Axis.X_SCREEN_AXIS;
	xaxis.labelOff = 7;
	xaxis.labelEdge = TextLine.TOP_EDGE;
	xOriginOff = 35;
	xaxis.gridEndOff=-dwHeight+1;
	xaxis.minMajTicSpacing = 40;

	xaxisArray[0] = xaxis;
	numXaxis = 1;
	xRange = X_MAX - X_MIN;
	xScale = xScaleLast = xaxis.scale;
	firstXaxis = xaxis;

	yaxis = new ColorAxis(Y_MIN, Y_MAX, -dwHeight);
	yaxis.ticDir = -1;
	yaxis.orient = Axis.Y_SCREEN_AXIS;
	yaxis.labelOff = -7;
	yaxis.labelEdge = TextLine.RIGHT_EDGE;
	yaxis.axisDir = -1;
	yOriginOff = h - 30;
	yaxis.gridEndOff=dwWidth-1;
	yaxis.gridDir = 1;

	int newSize = DEFAULT_STOR_SIZE;
	binStorSize = newSize;
	numBins = 0;
	binArray = new Bin [newSize];

	reset();

	buffer = new Image(w, h);
	bufG = new JGraphics(buffer);
    }

    // need to find correct axis
    public Annotation addAnnot(String label, int pos)
    {
	Bin bin;
	Axis xa;
	float time;
	int i;

	xa = null;
	for(i=0; i < numXaxis; i++){
	    xa = xaxisArray[i];	    
	    if(pos*xa.axisDir > xa.drawnX*xa.axisDir && 
	       pos*xa.axisDir < xa.axisDir*(xa.drawnX + xa.axisDir + 
					    xa.dispLen)){
		break;
	    }
	}
	if(i != numXaxis){
	    time = (pos - xa.drawnX) / xa.scale + xa.dispMin;
	    return addAnnot(label, time, xa);
	}

	return null;
    }

    public Annotation addAnnot(String label, float time)
    {
	return addAnnot(label, time, xaxis);
    }


    public Annotation addAnnot(String label, float time, Axis xa)
    {
	float value;
	int i,k;
	Bin bin = null;
	boolean valid = false;
	Annotation a = null;

	i = 0;
	for(k=0; k<numBins; k++){
	    bin = binArray[k];
	    if(xa == bin.xaxis){
		for(i = 0; i < bin.numPoints; i++){
		    if(time - bin.values[i*2] < (float)0.01){
			valid = true;
			break;
		    }
		}
		break;
	    }
	}

	if(valid){
	    a = new Annotation(label, time, bin.values[i*2+1], xa);
	    annots.add(a);	
	}
	
	return a;
    }

    public void drawAnnots(Graphics g)
    {
	Object [] annotObj = annots.toObjectArray();
	int i;
	Annotation a;
	int pos;

	for(i=0; i<annotObj.length; i++){
	    a = (Annotation)annotObj[i];
	    if(a.xaxis.drawnX != -1){
		pos = (int)((a.time - a.xaxis.dispMin) * a.xaxis.scale);
		if((pos*a.xaxis.axisDir >= 0) && 
		   (pos*a.xaxis.axisDir < a.xaxis.axisDir*a.xaxis.dispLen)){ 
		    a.draw(g, 
			   pos + a.xaxis.drawnX + a.xaxis.axisDir - a.width/2, 
			   annotTopY);
		}
	    }
	}
    }

    public void resize(int w, int h){}

    // return the maximum x offset plotted
    public int plot(JGraphics g)
    {
	int i,j,k;
	int lastOffset, lastPlottedOffset;
	int [] binPoints;
	int lastX, lastY, nextX, nextY;
	Bin bin;
	Axis xa;
	int curX, numXs, sumY;

	// set the clipping region
	g.setClip(xOriginOff+1, yOriginOff-dwHeight, dwWidth, dwHeight);

	for(k=0; k<numBins; k++){
	    bin = binArray[k];
	    xa = bin.xaxis;

	    if(bin.numPoints == 0 || xa.drawnX == -1)
		continue;
	    
	    binPoints = bin.points;
	    lastOffset = bin.numPoints*2;
	    
	    if(bin.lastPlottedPoint == -1){
		// No points have been plotted yet
		sumY = 0;
		numXs = 0;
		curX = binPoints[0];

		for(i=0; i<lastOffset && binPoints[i] == curX; i+= 2){
		    sumY += binPoints[i+1];
		    numXs++;
		}

		if(i >= lastOffset) {
		    return 0;
		} else {
		    lastY = sumY / numXs;
		    lastX = curX;
		    curX = binPoints[i];
		    sumY = 0;
		    numXs = 0;
		} 

	    } else {
		lastY = bin.lastPlottedY;
		lastX = bin.lastPlottedX;
		curX = bin.curX;
		sumY = bin.sumY;
		numXs = bin.numXs;
		i=bin.lastPlottedPoint*2 + 2;
	    }
		
	    g.setColor(lineColors[bin.c*3], lineColors[bin.c*3+1], lineColors[bin.c*3+2]);
	    g.translate(xa.drawnOffset, yaxis.drawnOffset);

	    for(; i<lastOffset; i+=2){
		nextX = binPoints[i];

		if(nextX == curX){
		    sumY += binPoints[i+1];
		    numXs++;
		    continue;
		}
		
		nextY = sumY / numXs;
		if(curX > (xa.dispOffset - 1) && curX <= (xa.dispOffset + xa.dispLen))
		    g.drawLine(lastX, lastY, curX, nextY);
		
		lastY = nextY;
		lastX = curX;
		curX = nextX;
		sumY = binPoints[i+1];	       
		numXs = 1;
	    }
	    
	    bin.lastPlottedPoint = bin.numPoints - 1;
	    bin.lastPlottedY = lastY;
	    bin.lastPlottedX = lastX;
	    bin.curX = curX;
	    bin.sumY = sumY;
	    bin.numXs = numXs;

	    g.translate(-xa.drawnOffset, -yaxis.drawnOffset);
	}

	g.clearClip();
	return 0;
    }


    // This is the yucky one
    public boolean removeBin(Object id)
    {
	// need to remove the arrays
	// need to shift the rest of the arrays down
	// need to go through the vector of bins and trash the 
	// deleted one and update the rest of them.
	
	/*
	int newSize = binStorSize * 3 / 2;

	int newNumPoints [] = new int [newSize];
	int newLastPoint [] = new int [newSize];
	int newPoints [] = new int [newSize][];
	int newValues [] = new int [newSize][];
	
	Vm.copyArray(points, 0, newPoints, 0, numBins);
	Vm.copyArray(values, 0, newValues, 0, numBins);
	Vm.copyArray(numPoints, 0, newNumPoints, 0, numBins);
	Vm.copyArray(lastPlottedPoint, 0, newLastPoint, 0, numBins);
	binStorSize = newSize;

	*/
	return true;
    }

    public void setYRange(float min, float range)
    {
	yaxis.dispMin = min;
	yaxis.setScale((yaxis.dispLen - yaxis.axisDir)/ range);

	needRecalc = true;
    }  

    public void setYMin(float min)
    {
	if(min < yaxis.min){
	    needRecalc = true;
	}

	yaxis.dispMin = min;
    }
	

    boolean needRecalc = false;
    boolean startPosChanged = true;

    public void setXRange(float min, float range)
    {
	xaxis.min = min;
	setXscale(xaxis.dispLen / range);
	xRange = range;
    }

    public void setXRange(float range)
    {
	if(range != xRange){
	    setXscale(xaxis.dispLen / range);
	    needRecalc = true;
	    xRange = range;
	}
    }

    // Set the min for the specified axis
    // Set the size of the rest of the axis
    // ickkkkk.
    public void setXRange(int col, float min, float range)
    {
	int i;
	int curStartPos;
	float max;

	xaxis.min = min;
	if(range != xRange){
	    setXscale(xaxis.dispLen / range);
	    xRange = range;
	    needRecalc = true;
	}

    }

    int xaxisStartPos = 0;
    public void setXscale(float newScale)
    {
	int i;
	int oldScaleSP = 0;
	int newXaxisStartPos = -1;
	int curStartPos = 0;
	Axis xa;

	xScale = newScale;
	needRecalc = true;
	for(i =0; i < numXaxis; i++){
	    xa = xaxisArray[i];
	    if(newXaxisStartPos == -1 &&
	       xaxisStartPos < (oldScaleSP + xa.length)){
		newXaxisStartPos = curStartPos + (int)(xa.dispMin * xScale);
	    }
	    oldScaleSP += 10 + xa.length;
	    xa.length = (int)((xa.max - xa.min)* xScale);
	    xa.setScale(xScale);
	    curStartPos += 10 + xa.length;
	}

	if(newXaxisStartPos != -1){
	    xaxisStartPos = newXaxisStartPos;
	}

    }

    public void setYscale(float scale)
    {
	yaxis.setScale(scale);
	yScale = scale;
	needRecalc = true;
    }

    public void scroll(int dist)
    {

	xaxisStartPos += dist;
	if(xaxisStartPos < 0){
	    xaxisStartPos = 0;
	}

    }

    public void incBinStor()
    {
	int newSize = binStorSize * 3 / 2;

	Bin newBinArray [] = new Bin [newSize];
	
	Vm.copyArray(binArray, 0, newBinArray, 0, numBins);
	binArray = newBinArray;

	binStorSize = newSize;
    }

    public void endCollection()
    {
	int i;

	if(maxX < 0 || curBin.numPoints < 3) return;

	xaxis.min = minX;
	xaxis.length = (int)((maxX - minX) * xaxis.scale);
	xaxis.max = maxX;
	xaxis.fixedLength = true;

	if(numXaxis >= xaxisArray.length){
	    Axis [] newAxis = new Axis[(numXaxis * 3)/ 2];
	    Vm.copyArray(xaxisArray, 0, newAxis, 0, numXaxis);
	    xaxisArray = newAxis;
	} 
	xaxisArray[numXaxis] = xaxis = new Axis(X_MIN, dwWidth, xaxis.scale);
	xaxis.ticDir = 1;
	xaxis.orient = Axis.X_SCREEN_AXIS;
	xaxis.labelOff = 7;
	xaxis.labelEdge = TextLine.TOP_EDGE;
	xaxis.gridEndOff=-dwHeight+1;
	xaxis.minMajTicSpacing = 40;

	numXaxis++;

	Object [] binObjs = activeBins.toObjectArray();
	for(i=0; i < binObjs.length; i++){
	    addBin(0, binObjs[i]);
	    activeBins.del(0);
	    //	    ((Bin)binObjs[i]).
	}

	minX = minY = 1;
	maxX = maxY = -1;

	for(i=0; i<100; i++){
	    minX *= (float)10;
	    maxX *= (float)10;
	    minY *= (float)10;
	    maxY *= (float)10;
	}

	startPosChanged = true;
	needRecalc = true;
    }
    
    public Object addBin(int location, Object oldPtr)
    {
	Bin oldBin = (Bin)oldPtr;
	Object newPtr = addBin(0, "");
	int [] newBinPtr = (int [])newPtr;
	Bin newBin = binArray[newBinPtr[0]];

	newBin.c = oldBin.c;
	oldBin.binPtr[0] = newBin.binPtr[0];
	newBin.binPtr = oldBin.binPtr;
	return newPtr;
    }


    // return a Object linked to this location
    // we are ignoring location for now
    public Object addBin(int location, String label)
    {
	int [] binPtr = new int [1];
	Bin bin;

	if(numBins >= binStorSize){
	    incBinStor();
	}

	// setup points, reset to the begining of the graph
	binPtr[0] = numBins;
	binArray[numBins] = curBin = new Bin();	
	curBin.xaxis = xaxis;
	curBin.c = activeBins.getCount();
	curBin.binPtr = binPtr;

	numBins++;
	activeBins.add(curBin);

	return binPtr;
    }

    void drawXaxis(JGraphics g, int x, int y)
    {
	int xaxisOffset = xOriginOff;
	int curStartPos = 0;
	Axis xa;
	int aRange;
	int aDispRange;
	float aMax, aMin;
	int i;

	xRange = dwWidth / xScale;
	
	curStartPos = 0;
	// first figure out which axis to start with
	for(i = 0; i<(numXaxis-1); i++){
	    xa = xaxisArray[i];
	    if(xaxisStartPos < (curStartPos + xa.length)){
		// We've got a winner draw the axis
		xa.dispOffset = xaxisStartPos - curStartPos;
		aDispRange = xa.length - xa.dispOffset;
		if(aDispRange >= dwWidth){
		    xa.dispLen = dwWidth;
		} else {
		    g.setColor(0,0,0);
		    g.drawLine(x + xaxisOffset + aDispRange + 1, y+yOriginOff, 
			       x+xaxisOffset + aDispRange + 1, y+yOriginOff - dwHeight);
		    xa.dispLen = aDispRange;
		}
		xa.draw(g, x + xaxisOffset, y+ yOriginOff);
		curStartPos += xa.length;
		curStartPos += 10;
		i++;
		break;
	    } else {
		xa.drawnX = -1;
		curStartPos += xa.length + 10;
		if(xaxisStartPos < curStartPos){
		    i++;
		    break;
		}
	    }
	}

	for(;i<(numXaxis-1);i++){
	    if((curStartPos - xaxisStartPos) >= dwWidth){
		//our work is done
		xaxisArray[i].drawnX = -1;
		continue;
	    }
	    // draw the next axis
	    g.setColor(0,0,0);
	    g.drawLine(x + xaxisOffset + curStartPos - xaxisStartPos, y+yOriginOff, 
		       x+xaxisOffset + curStartPos - xaxisStartPos, y+yOriginOff - dwHeight);	    
	    curStartPos++;
	    xa = xaxisArray[i];
	    xa.dispOffset = 0;
	    if((xa.length + (curStartPos - xaxisStartPos)) >= dwWidth){
		xa.dispLen = aDispRange = dwWidth - (curStartPos - xaxisStartPos);	      
	    } else {
		xa.dispLen = aDispRange = xa.length;	       
		g.setColor(0,0,0);
		g.drawLine(x + xaxisOffset + curStartPos - xaxisStartPos + aDispRange + 1, y+yOriginOff, 
			   x + xaxisOffset + curStartPos - xaxisStartPos + aDispRange + 1, y+yOriginOff - dwHeight);
	    }
	    xa.draw(g, x + xaxisOffset + (curStartPos - xaxisStartPos), y+ yOriginOff);
	    curStartPos += aDispRange + 10;
	}

	/*
	endTime = Vm.getTimeStamp();
	_g.drawText(endTime - startTime + "", xText, yText);
	startTime = endTime;
	xText += 20;
	*/		

	if((curStartPos - xaxisStartPos) >= dwWidth){
	    //our work is done
	    xaxisArray[numXaxis-1].drawnX = -1;
	    return;
	}

	// Now the nasty stuff we must draw the most current axis
	// it's max may be in a screwy state
	xa = xaxisArray[numXaxis-1];
	if(curStartPos <= xaxisStartPos){
	    aDispRange = dwWidth;
	    xa.dispOffset = xaxisStartPos - curStartPos;
	    xa.dispLen = dwWidth;
	    if(xa.length < (xa.dispLen + xa.dispOffset)){
		xa.length = (xa.dispOffset + xa.dispLen) * 2 / 3;
		xa.needCalcTics = true;
	    }
	    xa.draw(g, x + xaxisOffset, y+ yOriginOff);
	    curStartPos += aDispRange + 10;
	} else {
	    g.setColor(0,0,0);
	    g.drawLine(x + xaxisOffset + curStartPos - xaxisStartPos, y+yOriginOff, 
		       x+xaxisOffset + curStartPos - xaxisStartPos, y+yOriginOff - dwHeight);
	    curStartPos++;
	    xa.dispOffset = 0;
	    xa.dispLen = dwWidth - (curStartPos - xaxisStartPos);
	    if(xa.length < (xa.dispLen + xa.dispOffset)){
		xa.length = (xa.dispOffset + xa.dispLen) * 2 / 3;
		xa.needCalcTics = true;
	    }
	    xa.draw(g, x + xaxisOffset + (curStartPos - xaxisStartPos), y+ yOriginOff);
	}
    }
	

    public void drawAxis(JGraphics g, int x, int y)
    {
	g.setColor(255,255,255);
	g.fillRect(x,y,width,height);
	
	g.setColor(0,0,0);
	
	yaxis.draw(g,x+xOriginOff,y+yOriginOff);
       
	if(yaxis.labelExp != 0){
	    yExpLabel.setText("10^" + yaxis.labelExp);
	    yExpLabel.draw(g, 1, 10);
	} 
      
	/*
	endTime = Vm.getTimeStamp();
	_g.drawText(endTime - startTime + "", xText, yText);
	startTime = endTime;
	xText += 20;
	*/

	drawXaxis(g, x, y);

    }

    public int yText = 0;
    int xText =0;
    int beginTime, startTime,endTime;

    public JGraphics _g = null;

    public void draw(JGraphics _g, int x, int y)
    {
	JGraphics g = bufG;
	yText = y+height+40;
	xText =0;
	boolean dataWinChanged;
	int i;
	int curStartPos;
       
	/*
	_g.setColor(255,255,255);
	_g.fillRect(0, yText, 200, 30);
	_g.setColor(0,0,0);

		startTime = beginTime = Vm.getTimeStamp();
	*/

	this._g = _g;
	drawAxis(g, x, y);

	for(i=0; i<numBins; i++){
	    binArray[i].lastPlottedPoint = -1;
	}
       
	/*
	endTime = Vm.getTimeStamp();
	_g.drawText(endTime - startTime + "", xText, yText);
	startTime = endTime;
	xText += 20;
	*/

	Bin bin;
	Axis xa;

	if(needRecalc){
	    for(int k=0; k<numBins; k++){
		bin = binArray[k];
		xa = bin.xaxis;
		
		for(i=0; i<bin.numPoints*2; i++){
		    bin.points[i] = (int)((bin.values[i] - xa.min)* xa.scale);	  
		    i++;
		    bin.points[i] = (int)((bin.values[i] - yaxis.min) * yaxis.scale);
		}
	    }
	}
 
	plot(g);

	/*
	endTime = Vm.getTimeStamp();
	_g.drawText(endTime - startTime + "", xText, yText);
	startTime = endTime;
	xText += 20;
	*/	

	// 2 ms when empty
	drawAnnots((Graphics)g);
	
	// 24 ms 
	if(_g != null){
	    _g.copyRect(buffer, 0, 0, width, height, x, y); 	    
	}
     
	/*
	endTime = Vm.getTimeStamp();
	_g.drawText(endTime - beginTime + "", xText, yText);
	startTime = endTime;
	xText += 20;      
	*/

	redraw = false;
    }

    public boolean calcDataWin(JGraphics g, int w, int h)
    {	
	// This should be a bit of an iteration
	// attempting to arrive at the approx
	int widthSpace = -1*(yaxis.getOutsideSize());
	int heightSpace = xaxis.getOutsideSize();
	int bottomAxisSpace = h - yOriginOff;
	while((widthSpace + 1) > xOriginOff || (heightSpace + 1) > bottomAxisSpace){
	    xOriginOff = widthSpace + 1;
	    bottomAxisSpace = heightSpace + 1;
	    dwWidth = width - rightPadding - widthSpace + 1;
	    dwHeight = height - topPadding - bottomAxisSpace;
	    yaxis.setScale(((float)dwHeight * yaxis.scale) / (float)(yaxis.dispLen*yaxis.axisDir));
	    yaxis.dispLen = -dwHeight;
	    yaxis.gridEndOff = dwWidth;
	    xaxis.setScale((float)dwWidth / xRange);
	    xaxis.dispLen = dwWidth;
	    xaxis.gridEndOff = -dwHeight;
	    widthSpace = -1*(yaxis.getOutsideSize());
	    heightSpace = xaxis.getOutsideSize();
	}
	yOriginOff = h - bottomAxisSpace - 1;

	return true;
    }


    public void reset()
    {
	int i;

	minX = minY = 1;
	maxX = maxY = -1;

	for(i=0; i<100; i++){
	    minX *= (float)10;
	    maxX *= (float)10;
	    minY *= (float)10;
	    maxY *= (float)10;
	}


	length = 0;

	xaxisArray[0] = xaxis;
	xaxis.min = xaxis.dispMin = X_MIN;
	xaxis.dispOffset = 0;
	xaxis.length = xaxis.dispLen = dwWidth;
	xaxis.setScale((X_MAX - X_MIN) / (float)xaxis.length);
	xRange = X_MAX - X_MIN;
	numXaxis = 1;
	xaxisStartPos = 0;

	Object [] binObjs = activeBins.toObjectArray();
	numBins = binObjs.length;

	Bin bin;
	for(i=0; i<numBins; i++){
	    bin = (Bin)binObjs[i];
	    binArray[i] = bin;
	    bin.binPtr[0] = i;
	    bin.numPoints = 0;
	    bin.lastPlottedPoint = -1;
	}
	

	// remove annotations
	annots = new Vector();
    }

    /*
     * This doesn't make since in the new 
     * form of stuff 
     */
    public boolean addPoint(float x, float newValues[])
    {
	int k;
	int offset;
	Bin bin;


	for(k=0; k<numBins; k++){
	    bin = binArray[k];

	    offset = bin.numPoints*2;

	    // should check the current config
	    if(offset >= bin.points.length){
		// x is out of bounds
		endCollection();
		return false;
	    }
	
	    if(maxX < x) maxX = x;
	    if(minX > x) minX = x;
	    bin.values[offset] = x;

	    bin.points[offset] = (int)((x - xaxis.min)* xaxis.scale);
	    //	    xaxis.max = x;
	    offset++;
	    
	    if(maxY < newValues[k]) maxY = newValues[k];
	    if(minY > newValues[k]) minY = newValues[k];
	    bin.values[offset] = newValues[k];

	    bin.points[offset] = (int)((newValues[k] - yaxis.min) * yaxis.scale);
	    
	    bin.numPoints++;
	}

	return true;
    }

    public boolean addPoint(Object binID, float x, float value)
    {
	int [] binPtr = (int []) binID;
	int k = binPtr[0];
	Bin bin = binArray[k];
	int offset = bin.numPoints*2;

	// should check the current config
	if(offset >= bin.points.length){
	    // x is out of bounds
	    endCollection();
	    return false;
	}
	
	if(maxX < x) maxX = x;
	if(minX > x) minX = x;
	bin.values[offset] = x;

	bin.points[offset] = (int)((x - xaxis.min) * xaxis.scale);
	// xaxis.max = x;
	offset++;
	
	if(maxY < value) maxY = value;
	if(minY > value) minY = value;
	bin.values[offset] = value;

	bin.points[offset] = (int)((value - yaxis.min) * yaxis.scale);	
	bin.numPoints++;

	return true;
    }

}











