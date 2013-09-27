import java.io.*;
import java.util.*;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import com.dalsemi.onewire.utils.*;

class BlockTypeRange
{
    int minId;
    int maxId;
    int type;
}

class TempBlock
{
    final static int MAX_NUM_TEMP = 8;
    final static int ALUM = 1;
    final static int ACTUATOR = 2;
    final static int STAINLESS = 3;
    final static int PLASTIC = 4;

    float [] temps = new float [MAX_NUM_TEMP];
    int numTemps;
    boolean started;
    int id;
    int usrId;
    int type = 1;

    int hwToUsr(int hwId){
	if(hwId < 140){
	    type = ALUM;
	    return hwId;
	} else if(hwId < 180){
	    type = STAINLESS;
	    return hwId-140 + 201;
	} else if(hwId < 220){
	    type = PLASTIC;
	    return hwId-180 + 301;
	} else if(hwId < 230){
	    type = PLASTIC;
	    return hwId-220 + 401;
	} else {
	    type = ACTUATOR;
	    return hwId-230 + 1;	    
	}
    }

    public TempBlock(int id){
	this.id = id;
	usrId = hwToUsr(id);
	started = false;
	numTemps = 0;
    }
}

/* 
 * Main ideas:
 *  Need dynamic lists of TempBlocks
 *  
 *  Every time we search the 1-wire
 *   we need to update our list.
 *
 *  Also when getting data we get data
 *   once and then send back a piece
 *   at a time, only if they are started
 */

class TBProbeManager extends ProbeManager
{
    // get the next adapter DSPortAdapter
    DSPortAdapter adapter; 
    BufferedReader bRead;
    String line;
    int curProbe = 0;
    byte [] state;
    float temperature;
    Hashtable blocks = new Hashtable();
    Vector infoVect = new Vector();

    public TBProbeManager(String name)
    {
	adapter = null;
	this.name = name;
    }	   
	   
    void initAdapter() throws Exception {
	adapter = OneWireAccessProvider.getDefaultAdapter();
	
	if(!adapter.adapterDetected()){
	    System.exit(0);
	}
	
	// get exclusive use of adapter
	adapter.beginExclusive(true);
	adapter.setSearchAllDevices();
	adapter.targetFamily(0x28);
	adapter.setSpeed(adapter.SPEED_REGULAR);
    }	

    ProbeInfo [] getProbes()
    {
	TempBlock block;
	int count = 0;

	try{

	    ProbeSystem.logPrintln("Init the adapter");
	    initAdapter();

	    ProbeSystem.logPrintln("get devices");
	// Get all the current devices
	Enumeration ow_enum = adapter.getAllDeviceContainers();
	if(ow_enum == null){
	    return null;
	}

	// enumerate through all the iButtons found

	// do we need to notify I don't think so
	blocks.clear();
	for(;ow_enum.hasMoreElements();) {
	    // get the next ibutton
	    OneWireContainer28 probe = (OneWireContainer28)ow_enum.nextElement();
		   		   
	    // Get info from probe need to watch for exception if probe was detached
	    try{state = probe.readScratchpad();}
	    catch(OneWireIOException e){
		// Device is not present
		continue;
	    }

	    int blockNum = (int)(state[2] & 0xFF);
	    block = (TempBlock)blocks.get(new Integer(blockNum));
	    if(block == null){
		block = new TempBlock(blockNum);		
		blocks.put(new Integer(blockNum), block);
		count++;
	    } 

	}
	adapter.endExclusive();
	adapter = null;

	} catch (Exception e){
	    ProbeSystem.logPrintln("Error TB- getProbes");
	}

	
	ProbeInfo [] probes = new ProbeInfo [count];

	block = new TempBlock(0);
	count = 0;
	Enumeration blocks_enum = blocks.keys();
	for(;blocks_enum.hasMoreElements();){
	    int blockNum = ((Integer)blocks_enum.nextElement()).intValue();

	    // Get temp from info
	    probes[count] = new ProbeInfo(ProbeInfo.NAME, "Block " + block.hwToUsr(blockNum));
	    probes[count].id = blockNum;
	    count++;
	}

	return probes;
    }

    /*
     * This is a slightly tricky funct because it
     * doesn't totally fit our setup
     * 
     * My plan is to collect all the info
     * and save it and then hand back a chunk
     * of info as requested.
     */
    Enumeration deleBlocks = null;
    Enumeration addBlocks = null;
    Enumeration infoEnum = null;
    Vector newBlocks = new Vector();
    Enumeration curBlock_enum = null;
    Hashtable curBlocks = new Hashtable();

    boolean getPackage()
    {
	TempBlock block;

	/*
	 *  Have we sent all the information that 
	 * we know to the caller?
	 * Yes - get more info
	 * No - continue sending.
	 */
	if((infoEnum == null || !infoEnum.hasMoreElements()) &&
	   (deleBlocks == null || !deleBlocks.hasMoreElements()) &&
	   (addBlocks == null || !addBlocks.hasMoreElements()) &&
	   (curBlock_enum == null || !curBlock_enum.hasMoreElements())){
	    
	    synchronized(infoVect){
		if(infoVect.size() > 0){
		    infoEnum = ((Vector)infoVect.clone()).elements();
		    infoVect.removeAllElements();
		} else {
		    infoEnum = null;
		}

	    try{

		if(adapter == null){
		    initAdapter();
		}

		// Get all the current devices
		Enumeration ow_enum = adapter.getAllDeviceContainers();
		
		// send 1-Wire Reset
		int rslt = adapter.reset();
		
		if(rslt != adapter.RESET_PRESENCE){
		    // "No devices found on reset"
		    
		}
		
		// broadcast the SKIP ROM 
		adapter.putByte(0xCC);
		
		// Tell them all to collect temperature
		adapter.putByte(0x44);
		
		// Sleep until they are done collecting temp
		Thread.sleep(750);
		
		newBlocks.removeAllElements();
		curBlocks.clear();
		for(;ow_enum.hasMoreElements();) {
		    // get the next ibutton
		    OneWireContainer28 probe = (OneWireContainer28)ow_enum.nextElement();
		    
		    // Get info from probe need to watch for exception if probe was detached
		    try{state = probe.readScratchpad();}
		    catch(OneWireIOException e){
			// Device is not present
			// need to do something special here I think
			ProbeSystem.logPrintln("Lost block in middle of poll");
			continue;
		    }

		    int blockNum = (int)(state[2] & 0xFF);

		    // First check our new set of blocks
		    block = (TempBlock)curBlocks.get(new Integer(blockNum));
		    if(block == null){
			// This block hasn't been seen yet
			// Is it new?
			block = (TempBlock)blocks.get(new Integer(blockNum));
			if(block == null){
			    // it's new
			    block = new TempBlock(blockNum);		
			    curBlocks.put(new Integer(blockNum), block);
			    
			    // Add it to the new list
			    newBlocks.addElement(block);
			} else {
			    // it is already in our old list remove it
			    blocks.remove(new Integer(blockNum));
			    
			    // add it to the new list
			    curBlocks.put(new Integer(blockNum), block);
			    block.numTemps = 0;
			}
		    }
		    
		    // ok we have an updated current block
		    int probeNum = (int)((state[3] >> 5) & 0x07);
		    
		    // This is a HACK because the blocks aren't 
		    // numbered right. They should have a block number
		    // and a probe number within the block
		    block.temps[probeNum] = (float)probe.getTemperature(state);
		    if(probeNum+1 > block.numTemps){
			block.numTemps = probeNum +1;
		    }
		    
		}

		/*
		 * so at this point we have our curBlocks in "curBlocks"
		 * any deleted blocks will be in "blocks"
		 * any new blocks will be in "newBlocks"
		 */
		// save the deleted blocks
		deleBlocks = blocks.elements();
		
		// make a enum of new blocks
		addBlocks = newBlocks.elements();
	    
		// update the blocks for the next call
		Hashtable tmp = blocks;
		blocks = curBlocks;
		curBlocks = tmp;

		// make a list of data(Temps) to be sent out
		curBlock_enum = blocks.elements();
		
		if((deleBlocks == null ||
		    !deleBlocks.hasMoreElements()) &&
		   blocks.size() == 0){
		    //there are no blocks attached
		    return false;
		}

	    } catch(Exception e){
		ProbeSystem.logPrintln("Error in getData 1W");
		return false;
	    }

	    }
	}

	/*
	 * If we are here then there is info to be sent to the
	 * caller
	 */
	if(infoEnum != null &&
	   infoEnum.hasMoreElements()){
	    response = PROBE_INFO;
	    curInfo = (ProbeInfo)infoEnum.nextElement();
	    probeId = curInfo.id;
	    return true;
	}


	if(deleBlocks != null &&
	   deleBlocks.hasMoreElements()){
	    //send delete info
	    block = (TempBlock)deleBlocks.nextElement();
	    probeId = 0;
	    response = PROBE_INFO;
	    msg = "Probe not attached";
	    curInfo = new ProbeInfo(ProbeInfo.DELETED, msg);
	    curInfo.id = block.id;	    
	    ProbeSystem.logPrintln("dele block " + block.usrId);
	    return true;
	}

	if(addBlocks != null && 
	   addBlocks.hasMoreElements()){
	    // send added info
	    block = (TempBlock)addBlocks.nextElement();
	    probeId = 0;
	    response = PROBE_INFO;
	    curInfo = new ProbeInfo(ProbeInfo.NEW_PROBE, "Block " + block.usrId);
	    curInfo.id = block.id;
	    ProbeSystem.logPrintln("add block " + block.usrId + "(" + block.id + ")");
	    return true;
	}

	if(curBlock_enum != null &&
	   curBlock_enum.hasMoreElements()){
	    // send the temps for these probes
	    block = (TempBlock)curBlock_enum.nextElement();
	    
	    // skip stopped probes
	    while(!block.started &&
		  curBlock_enum.hasMoreElements()){
		block = (TempBlock)curBlock_enum.nextElement();
	    }
	    
	    // if block is still stop the there are no started
	    // blocks
	    if(!block.started){
		return false;
	    }

	    probeId = block.id;
	    response = PROBE_DATA;
	    curData = new float [block.numTemps];
	    System.arraycopy(block.temps, 0, curData, 0, block.numTemps);

	    return true;
	}
    
	// We should also check for an info enum
  
	// we shouldn't get here
	ProbeSystem.logPrintln("Unknown state!");
	return false;
    }

    boolean start(int id)
    {
	TempBlock block;

	block = (TempBlock)blocks.get(new Integer(id));
	if(block != null){
	    block.started = true;
	    return true;
	}
	
	return false;
    }

    boolean stop(int id)
    {
	TempBlock block;

	block = (TempBlock)blocks.get(new Integer(id));
	if(block != null){
	    block.started = false;
	    return true;
	}
	
	return false;
    }
    
    boolean readInfo(int id)
    {
	TempBlock block;
	ProbeInfo info;

	ProbeSystem.logPrint("Reading info... ");

	synchronized(infoVect){
	    
	    block = (TempBlock)blocks.get(new Integer(id));
	    if(block != null){
		info = new ProbeInfo(ProbeInfo.MSG, ("Type " + block.type));
		info.id = block.id;
		infoVect.addElement(info);
		ProbeSystem.logPrintln("Done.");
	    }
	}
	return true;
    }

}




