import waba.ui.*;
import waba.fx.*;
import waba.io.*;
import waba.sys.*;
import graph.*;

public class PSConnection extends Control
{
    final static int MAX_COUNT = 200;
    final static int DEF_PORT = 1234;

    ProbeManager pm = null;
    Canvas newCanvas;
    String addr;
    int port;
    int curStatusLine;
    BlockObject [] blocks;
    MainWindow mw;
    Timer timer = null;
    GraphView graph = null;
    int count;
    DataPage dp;

    public PSConnection()
    {
	newCanvas = null;
	addr = null;
	mw = MainWindow.getMainWindow();
	

    }

    public void writeExt(DataStream ds)
    {
	ds.writeShort(x);
	ds.writeShort(y);
	ds.writeShort(width);
	ds.writeShort(height);
	ds.writeString(addr);
    }

    public void readExt(DataStream ds)
    {
	int x,y, w, h;
	x = ds.readShort();
	y = ds.readShort();
	w = ds.readShort();
	h = ds.readShort();

	setRect(x,y,w,h);

	addr = ds.readString();
    }

    public void setRect(int x, int y, int w, int h)
    {
	//	super.setRect(x,y,w,h);
	curStatusLine = 0;
	pm = null;
	timer = null;

	/*
	int numStatusL = (h-1-40)/20;
	statusLines = new Label [numStatusL];
	int yPos = 40;
	for(int i=0; i<numStatusL; i++){
	    statusLines[i] = new Label("");
	    statusLines[i].setRect(1,yPos, w-2, 15);
	    add(statusLines[i]);
	    yPos += 20;
	}
	*/
		
    }

    public PSConnection(Canvas c)
    {
	newCanvas = c;
	port = DEF_PORT;
	mw = MainWindow.getMainWindow();

	curStatusLine = 0;
	pm = null;
	timer = null;
    }

    public void setPos(int x, int y)
    {
	super.setRect(x,y, width,height);
    }

    void printStatus(String s)
    {
	/*
	statusLines[curStatusLine].setText(s);
	curStatusLine++;
	curStatusLine = curStatusLine % statusLines.length;    
	*/
    }

    public boolean open(String a)
    {
	if(pm == null){
	    addr = a;

	    // Do some error checking

	    pm = (ProbeManager) new JavaProbeManager(addr, port, 1, "wPC");
	    // should check status of create
	    if(pm.response == pm.ERROR){
		printStatus("Error opening");
		return false;
	    }

	    int numProbes = discover();
	    if(numProbes == -1){
		// error in discover
		return false;
	    }

	    printStatus(numProbes + " blocks");

	    pos = 0;
	    count = 0;
	    
	    return true;
	}

	return false;
    }

    public void close()
    {
	if(pm != null){
	    // disconnect
	    pm.close();
	    pm = null;
	    if(timer != null){
		removeTimer(timer);
		timer = null;
	    }
	}

    }


    int pos;
    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.TIMER &&
	   e.target == this){
	    if(timer != null){
		switch(pos){
		case 0:
		case 1:
		    if(!step()){
			// error remove the timer 		
			removeTimer(timer);
			timer = null;
			if(pm != null){
			    pm.close();
			    pm = null;
			}	
			dp.forceClose();
		    }
		    if(pm != null){
			pm.requestAck();
		    }

		    break;
		}
		pos++;
		pos %= 2;
		count++;
	    }
	}
    }

    // return how many blocks are attached
    // -1 for an error
    public int discover()
    {
	BlockObject [] oldBlocks = blocks;
	ProbeInfo [] probes;
	BlockObject block;
	int i,j;

	if(pm == null){
	    printStatus("Open First");
	    return -1;
	}	    

	if(timer != null){
	    // We are currently streaming 
	    // restart
	    removeTimer(timer);
	    timer = null;
	    
	    pm.close();

	    pm = (ProbeManager) new JavaProbeManager(addr, port, 0, "wPC");
	    if(pm.response == pm.ERROR){
		printStatus("Error Opening");
		dp.forceClose();
		return -1;
	    }
	}

	probes = pm.getProbes();
	if(probes == null){
	    // regardless of the reason there are no
	    // probes so remove all the blocks
	    if(oldBlocks != null){
		// delete all the old blocks
		for(j=0; j<oldBlocks.length; j++){
		    if(oldBlocks[j] != null){
			// This block doesn't exist any more
			oldBlocks[j].canvas.removeObject(oldBlocks[j]);
			oldBlocks[j] = null;
		    }
		}
	    }

	    if(pm.response == pm.ERROR){
		printStatus("getProbes failed");
		// disconnect
		dp.forceClose();
		pm = null;
		blocks = null;
		return -1;
	    } else {
		// there are no probes currently
		blocks = null;
		return 0;
	    }
	}
	
	// make a new array of blocks
	blocks = new BlockObject[probes.length];

	int blockIndex = 0;
	int realBlockId;
	String name;
	for(i=0; i < probes.length; i++){
	    name = probes[i].strVal;
	    realBlockId = Convert.toInt(name.substring(6,name.length()));
	    
	    j = 0;
	    if(oldBlocks != null){
		for(j=0; j < oldBlocks.length; j++){
		    if(oldBlocks[j] != null && 
		       oldBlocks[j].blockNum == realBlockId){
			// This block matches one we already have
			blocks[blockIndex] = oldBlocks[j];
			blocks[blockIndex].psId = probes[i].id;
		    
			// remove it from the old blocks
			oldBlocks[j] = null;
			blockIndex++;
			break;
		    }
		}
	    }

	    if(oldBlocks == null ||
	       j == oldBlocks.length){
		// this is new block that isn't added
		block = new BlockObject(mw.defaultFont,1,14,21);
		block.psId = probes[i].id;
		block.blockNum = realBlockId;
		blocks[blockIndex] = block;
		blockIndex++;

	    }		
	}
	 
	if(oldBlocks != null){
	    // Ok we should have add all the new blocks at this point
	    // Delete the non-existant blocks
	    for(j=0; j<oldBlocks.length; j++){
		if(oldBlocks[j] != null){
		    // This block doesn't exist any more
		    oldBlocks[j].canvas.removeObject(oldBlocks[j]);
		    oldBlocks[j] = null;
		}
	    }
	    oldBlocks = null;
	}

	// make sure all the blocks are started
	for(j=0; j<blocks.length; j++){
	    pm.readInfo(blocks[j].psId);
	    
	    // don't start till we have all the info
	    pm.start(blocks[j].psId);
	}

	if(probes.length > 0){
	    pos = 0;
	    count = 0;
	    timer = addTimer(100);
	    if(pm != null){
		pm.requestAck();
	    }
	    
	} else if(timer != null){
	    // There are no blocks so stop the timer
	    removeTimer(timer);
	    timer = null;
	}
		
		
	return probes.length;
    }

    boolean step(){
	BlockObject block;
	int i;

	if(pm == null){
	    printStatus("step error");
	    return false;
	}

	// There is a bit of a bug here because
	// if an  ack is requested an the while exits
	// before getting the ack we will fall behind.
	while(true){
	    if(!pm.getPackage()){
		// we haven't recieved the ack back
		// but we exited?
		// this should be an error
		return false;
	    }

	    if(pm.response == pm.ACK){
		if(count > MAX_COUNT){
		    // we haven't received data in count steps
		    return false;
		} 
		count++;
		break;
	    }

	    if(pm.response == ProbeManager.NEW_PROBE){
		// make a new probe

	    } else {
		// find probe
		for(i=0; i< blocks.length; i++){
		    if(blocks[i].psId == pm.probeId){
			break;
		    }
		}
		
		if(i == blocks.length){
		    // no probe?
		    return false;
		}
		block = blocks[i];

		// act appropriatly function in probeState
		// info, data, stop
		switch(pm.response){
		case ProbeManager.PROBE_INFO :
		    // Find the probe
		    // This should tell us when a probe is detached
		    

		    break;
		case ProbeManager.PROBE_DATA :
		    if(block.canvas == null){
			// this is an uninitialized block
			block.setLen(pm.curData.length);
			block.dragAction = block.EXT_DRAG_MOVE;
			block.rotate(-1);
			block.move(-1,-1);
			newCanvas.addObject(block, newCanvas.layers[0]);
		    }
		    int length = pm.curData.length;
		    if(length > block.temps.length){
			length = block.temps.length;
		    }

		    for(i = 0; i < length; i++){
			block.temps[i] = pm.curData[i];
		    }
		    block.redraw();
		    if(block.canvas != null){
			block.canvas.interactAll(block, CanvasObject.UPDATE);
		    }
		    count = -1;
		    break;
		}
	    }
	}  

	if(count == 0 && 
	   graph != null){
	    graph.plot();
	}

	return true;
    }
       
}











