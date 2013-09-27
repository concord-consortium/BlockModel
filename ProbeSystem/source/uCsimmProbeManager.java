
class uCsimmProbeManager extends ProbeManager
{
    TCPDataChannel dc;
    uCsimmDataProbe probes[];
    public void uCsimmDataProbe currProbe;

    byte buf[] = new byte[64];
    int noCount = 0;
    int ovCount = 0;
    StringBuffer dataValue = new StringBuffer();

    public int id;
    public int numValues;    
    public int status;
    public String errStr = new String("");

    boolean flushed = false;
    boolean streaming = true;

    int sendACount = 0;
    int sendALength = 20;
    int sentAAt = 0;
    int dataCount = 0;
    int skipCount = 0;
    public int skipSpace = sendALength;
    public int skipLength = 0;
    boolean skipping = false;
    public int dataLatency;
    int optLatency = 5;
    int probeCount = 0;

    public uCsimmProbeManager(TCPDataChannel myDc, int num)
    {
	dc = myDc;
	probes = new uCsimmDataProbe [num];
	probeCount = 0;
    }

    public boolean start(int id)
    {
	return start();
    }

    public boolean stop(int id)
    {
	return closeConnect();
    }

    public boolean start()
    {
	if(!flushChannel(null) ||
	   !dc.writeByte((byte)'b') ||
	   !dc.writeByte((byte)'s')){
	    return false;
	}

	streaming = true;
	flushed = false;
	skipCount = 0;
	sendACount = sendALength;
	sentAAt = 0;
	dataCount = 0;
	skipping = false;

	return true;
    }


    public boolean closeConnect()
    {
	if(!dc.writeByte((byte)'c') ||
	   !dc.writeByte((byte)'q') ||
	   !dc.close()){
	    return false;
	} 
	dc = null;
	return true;
    }

    public int bCount;
    int rLen;


    int location;
    char cmd = 'Z';

    boolean flushChannel(String msg){	
	if(!flushed){
	    // should send msg to parent call back
	    if(msg == null){
		msg = new String("Flushing buffer");
	    }
	    System.out.println(msg);

	    if(!dc.writeByte((byte)'c') ||
	       !dc.flushInput(15)){
		return false;
	    }

	    flushed = true;
	}

	return true;
    }

    public boolean initProbe(uCsimmDataProbe probe)
    {
	int i = 0;

	if(probe == null){
	    // eventually we would like to be able
	    // to create probes on the fly
	    errStr = new String("No known probes");
	    return false;
	}

	//  First get probe info from server
	if(!start()){
	    return false;
	}
	
	location=8;
	if(!dc.readBytes(buf, 1)){
	    return false;
	}

	if(buf[0] == (byte)'n'){
	    // Probe Server is giving probe info
	    
	    location = 9;
	    if(!dc.readBytes(buf, 8)){
		return false;
	    }

	    id = dc.readInt(buf, 0);
	    numValues = dc.readInt(buf, 4);
	    if(id == 0){
		if(numValues == 1){
		    id = 1;
		} else {
		    id = 2;
		}
	    }

	    flushChannel("IDing probe");
	} else {
	    flushChannel("Error in getProbe");
	    return false;
	}
	
	probe.init(id, numValues);
	probes[probeCount] = probe;
	currProbe = probe;

	// Should check bounds here
	probeCount++;

	return true;
    }

    void restart(String msg)
    {
	status = 2;
	flushChannel(msg);
	if(!start()){
	    status = -1;
	}
    }


    String msgStr = new String();
    /*
     * status 
     *  -1 fatal error stop!!: errStr
     *  0  got Data
     *  1  stopped
     */

    public boolean getPackage()
    {
	int value;
	int i;
	int strLen;
	float [] output = null;

	status = -1;

	if(currProbe == null){
	    return null;
	}


	while(true){
	    if(sendACount == sendALength){
		// Send an A
		sentAAt = dataCount;
		if(!dc.writeByte((byte)'a')){
		    //Hmmm
		    status = -1;
		    return null;
		}
	    } 
	    sendACount++;

	    location = 0;
	    if(!dc.readBytes(buf, 1)){
		restart(errStr);
		return null;
	    }

	    if(buf[0] == (byte)'A'){
		// Probe Server responding to our request
		System.out.println("Got an A");
		
		// How many points have arrived since
		// we requested the A
		dataLatency = dataCount - sentAAt;
		skipCount = 0;
		skipping = false;
		if(dataLatency > sendALength){
		  if(skipSpace != 1){
		    skipSpace = 1;
		    skipLength = 1;
		  } else {
		    skipLength++;
		  }
		  restart("Large Latency");
		  return null;
		} else if(dataLatency > 5){
		  if(skipSpace > 1){
		    skipSpace = skipSpace / 2;
		    skipLength = 1;
		  } else {
		    skipLength++;
		  }
		}

		// start counting till the next A
		sendACount = 0;
		continue;
	    } else if(buf[0] == (byte)'n'){
		// Probe Server is giving probe info
		
		location = 1;
		if(!dc.readBytes(buf, 8)){
		    restart(errStr);
		    return null;
		}

		id = dc.readInt(buf, 0);
		numValues = dc.readInt(buf, 4);

		// Verify that the probe is the same.
		// only restart if the probe changes
		if(!currProbe.matches(numValues)){
		    // Stop the streaming and flush the buffer
		    flushChannel("New Probe Attached");

		    msgStr = new String("New Probe Attached");
		    status = 1;
		    return null;
		} else {
		    // The probe hasn't changed keep going
		    continue;
		}
	    }	else if(buf[0] == (byte)'d'){
		// Probe Server is sending a data point
	      dataCount++;

	      // Figure out skipping
	      if(skipping){
		if(skipCount >= skipLength){
		  skipping = false;
		  skipCount = 0;
		}
	      } else {
		if(skipCount >= skipSpace && skipLength > 0){
		  skipping = true;
		  skipCount = 0;
		}
	      }

	      skipCount++;
	      
	      // Is this necessary????????
		if(numValues <= 0){
		    // we should try to restart the stream
		    restart(null);
		    return null;
		}

		location = 2;
		if(!dc.readBytes(buf, 4*numValues)){
		    restart(errStr);
		    return null;
		}

		if(!skipping){
		  
		  output = new float[numValues];
		  for(i=0;i<numValues;i++){
		    output[i] = (float)dc.readInt(buf, i*4);
		  }

		  status = 0;
		  return output;
		} else {
		  // skip this point because we fell behind
		  continue;
		}
	    } else if(buf[0] == (byte)'p'){
		// Probe Server is sending a pause msg
		StringBuffer strBuf = new StringBuffer();

		location = 3;
		if(!dc.readBytes(buf, 4)){
		    restart(errStr);
		    return null;
		}
		strLen = dc.readInt(buf, 0);

		location = 4;
		if(!dc.readBytes(buf, strLen)){
		    restart(errStr);
		    return null;
		}

		for(i=0; i<strLen; i++){
		    strBuf.append((char)buf[i]);
		}

		// Stop stream and flush buffer
		flushChannel("Paused");
		status = 1;
		msgStr = new String("Paused: " + strBuf);
		return null;
	    } 
	
	    return null;

	}


    }

}







