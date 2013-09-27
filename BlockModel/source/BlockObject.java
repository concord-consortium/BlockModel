import waba.fx.*;
import waba.ui.*;
import waba.io.*;

class BlockObject extends CanvasObject 
  implements ThermalPatch
{
    int rF,gF,bF;
    int rB,gB,bB;
    float temps [];
    String label;
    int blockNum;
    int psId;
    boolean drawLabel;
    Font font;
    int tempLen;

    public void writeExt(DataStream ds)
    {
	int i;
	ds.writeString("BlockObject");
	super.writeExt(ds);

	ds.writeShort(tempLen);
	ds.writeInt(temps.length);
	for(i=0; i<temps.length; i++){
	    ds.writeFloat(temps[i]);
	}
	ds.writeString(label);
	ds.writeInt(blockNum);
	ds.writeBoolean(drawLabel);

    }
    public void readExt(DataStream ds)
    {
	int i, numTemps;
	super.readExt(ds);

	tempLen = ds.readShort();
	numTemps = ds.readInt();
	setLen(numTemps);

	for(i=0; i<numTemps; i++){
	    temps[i] = ds.readFloat();
	}
	label = ds.readString();
	blockNum = ds.readInt();
	drawLabel = ds.readBoolean();
    }

    BlockObject()
    {
	super();       

	blockNum = 0;
	rF = 255;
	gF = 0;
	bF = 0;
	rB = 255;
	gB = 255;
	bB = 255;
	drawLabel = true;
	disposable = false;
	font = MainWindow.defaultFont;
    }

    BlockObject(Font f, int numTemps, int w, int h)
    {
	this();

	font = f;
	width = w;
	tempLen = h / numTemps;
	height = h;
	setLen(numTemps);
    }

    public void setLen(int len)
    {
	if((orient % 2) == 0){
	    height = tempLen * len;
	} else {
	    width = tempLen * len;
	}
	temps = new float [len];
    }


    public float getTemp(int x, int y)
    {
	int myX,myY;

	myX = x - this.x;
	myY = y - this.y;

	if(myX < 0 ||
	   myY < 0 ||
	   myX >= width ||
	   myY >= height){
	    // out of bounds ????
	    return (float)0.0;
	}

	switch(orient){
	case 0 :
	    return temps[myY/tempLen];
	case 1 :
	    return temps[(width - myX - 1)/tempLen];
	case 2 :
	    return temps[(height - myY - 1)/tempLen];
	case 3 :
	    return temps[myX/tempLen];
	}

	return (float)0;
    }


    public void copyTo(CanvasObject co)
    {
	super.copyTo(co);
	if(co instanceof BlockObject){
	    BlockObject me = (BlockObject)co;
	    me.font = font;
	    me.temps = new float [temps.length];
	}
    }

    public CanvasObject copy()
    {
	BlockObject me = new BlockObject(font,temps.length,width,height);
	copyTo(me);

	return me;
    }

    /* 
     * This is a bit tricky: the orientations are:
     * 0,2 vertical
     * 1,3 horizontal
     * In the 0 orientation the temps[0] is the top
     *   of the block.  
     * The orientation values coorespond to rotating the
     *   block clockwise.  So here is the correlation 
     *   table:
     *     Orient      temps[0] position
     *       0            top
     *       1            right
     *       2            bottom
     *       3            left
     *
     */

    public void draw(Graphics g)
    {
	int i;

	if(g != null && canvas != null){
	    FontMetrics fm = canvas.getFontMetrics(font);
	    int tempStart, tempEnd, tempStep;
	    
	    // Determine if temps[] is the same as or
	    // inverse to the coordinate system of the screen
	    tempStart = 0;
	    tempEnd = temps.length;
	    tempStep = 1;
	    if((orient % 3) != 0){
		// orients 0 & 3 are inverted
		tempStart = temps.length - 1;
		tempEnd = -1;
		tempStep = -1;
	    }

	    if((orient % 2) == 0){
		// Vertical block
		int yPos = y;
		for(i=tempStart; i != tempEnd; i += tempStep){
		    if(canvas.axis != null){
			canvas.axis.setTempColor(g,temps[i]);
		    } else {
			g.setColor(255,255,255);
		    }

		    g.fillRect(x,yPos,width,tempLen);
		    g.setColor(100,100,100);
		    g.drawLine(x, yPos, x+2,yPos);
		    g.drawLine(x+(width-3), yPos, x+(width-1),yPos);
		    yPos += tempLen;
		}
		
		g.drawRect(x,y,width,height);

		if(drawLabel){
		    int arrowTop, arrowBot, textY, textHeight;
		    int arrowHeight = fm.getAscent()/2;
		    int tWidth;
		    if(blockNum >= 100){
			tWidth = fm.getTextWidth("00");
			textHeight = fm.getAscent()*2 + 1;			
		    } else {
			tWidth = fm.getTextWidth(blockNum + "");
			textHeight = fm.getAscent();
		    }
		    int tHeight = textHeight + arrowHeight + 1;
		    int textX = x + (width - tWidth - 1)/2;
		    int topY = y + (height - tHeight)/2;
		    int arrowSpace = (width - fm.getAscent()*2/3)/2;
		    if(orient == 0){
			arrowTop = topY;
			arrowBot = topY + arrowHeight - 1;
			textY = topY + arrowHeight + 1;
		    } else {
			textY = topY;
			arrowBot = topY + textHeight + 1;
			arrowTop = arrowBot + arrowHeight - 1;
		    }


		    g.setFont(font);
		    g.setColor(0,0,0);
		    if(blockNum < 100){
			g.drawText(blockNum + "", textX, textY); 
		    } else {
			g.drawText((blockNum / 100) + "", textX + tWidth/4, textY);
			String tmpLbl = (blockNum % 100) + ""; 
			if((blockNum % 100) < 10) tmpLbl = "0" + tmpLbl;
			g.drawText(tmpLbl, textX, textY + fm.getAscent() + 1);
		    }
		    g.drawLine(x+arrowSpace, arrowTop, x+(width-1)/2, arrowBot);
		    g.drawLine(x+width-1-arrowSpace, arrowTop,
			       x+width-1-(width-1)/2,arrowBot);
		}	   
	    } else {
		// Horizontal Block
		int xPos = x;
		for(i=tempStart; i != tempEnd; i += tempStep){
		    if(canvas.axis != null){
			canvas.axis.setTempColor(g,temps[i]);
		    } else {
			g.setColor(255,255,255);
		    }

		    g.fillRect(xPos,y,tempLen,height);
		    g.setColor(100,100,100);
		    g.drawLine(xPos, y, xPos,y+2);
		    g.drawLine(xPos, y+(height-3), xPos,y+(height-1));
		    xPos += tempLen;
		}
		
		g.drawRect(x,y,width,height);
		
		if(drawLabel){
		    int tWidth = fm.getTextWidth(blockNum + ">");
		    int tHeight = fm.getHeight();
		    int tX = x + (width - tWidth)/2;
		    int tY = y + (height - tHeight)/2;
		    g.setFont(font);
		    g.setColor(0,0,0);
		    if(orient == 1){
			g.drawText(blockNum + "<",tX,tY);
		    } else {
			g.drawText(">" + blockNum,tX,tY);
		    }
		}
	    } 

	    if(selected){
		// draw black line 
		g.setColor(0,0,0);
		g.drawRect(x,y,width,height);
		g.drawRect(x+1,y+1,width-2,height-2);
	    }
	}
    }

}



















