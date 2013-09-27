import waba.io.*;
import waba.sys.*;
	

class TCPDataProbe 
{
    public final static int UNKNOWN = 0;
    public final static int PALM = 1;
    public final static int JAVA = 2;
    public final static int WINCE = 3;
    

    String addr = "4.19.234.82";
    int port = 100;
    int platform;
    Socket socket = null;
    boolean palm = false;

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
    A2DProbe currProbe = null;

    wDataProbe caller = null;
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


    public TCPDataProbe(String a, int p, int os, wDataProbe c)
    {
	addr = a;
	port = p;
	platform = os;
	if(os == PALM) palm = true;
	id = 0;
	numValues = 0;
	socket = null;

	caller = c;
	if(os == PALM || os == WINCE){
	  skipSpace = 1;
	  skipLength = 1;
	  optLatency = 5;
	}


	openConnect();
    }

    String readLn(Socket s)
    {
	char input;
	byte inBuf[] = new byte[1];
	int ret;

	StringBuffer out = new StringBuffer();

	input = ' ';
	while(input != '\n'){
	    ret = s.readBytes(inBuf, 0, 1);
	    if(ret != 1){
		break;
	    }

	    input = (char)inBuf[0];
	    out.append(input);
	}

	return out.toString();
    }

    final static int RETRY_COUNT = 20;

    public boolean openConnect()
    {
	String info1;
	String info2;
	int count;

	if(socket != null && !socket.isOpen()){
	    socket.close();
	    socket = null;
	}

	if(socket == null){

	    count = 0;
	    socket = new Socket(addr, port);
	    while(!socket.isOpen()){
		socket.close();
		socket = null;
		count++;
		if(count > RETRY_COUNT){
		    return false;
		}
		Vm.sleep(200);
		socket = new Socket(addr, port);
		
	    }
	
	    socket.setReadTimeout(1500);

	    flushed = true;
	}

	return true;
    }
    
    public boolean start()
    {
	// Make sure socket is open
	if(!openConnect()){
	    return false;
	}

	// need to check the flush status
	if(!flushed){
	    flushSocket(null);
	}

	buf[0] = (byte)'b';
	socket.writeBytes(buf, 0, 1);
	Vm.sleep(200);

	buf[0] = (byte)'s';
	socket.writeBytes(buf, 0, 1);
	Vm.sleep(200);

	streaming = true;

	flushed = false;

	skipCount = 0;
	sendACount = sendALength;
	sentAAt = 0;
	dataCount = 0;
	skipping = false;

	return true;
    }


    public void closeConnect()
    {
      if(socket != null){
	buf[0] = (byte)'c';
	socket.writeBytes(buf, 0, 1);
	Vm.sleep(200);

	buf[0] = (byte) 'q';
	socket.writeBytes(buf, 0, 1);
	Vm.sleep(200);

	socket.close();

	socket = null;
      }	
    }

    int readInt(byte buf[], int start)
    {
	int value;
	int i;

	value = 0;
	for(i=3; i >= 0; i--){
	    value |= (0x0FF & buf[3-i+start]) << (8*i);
	}

	return value;
    }

    public int bCount;
    int rLen;


    // true for error (easier coding)
    final static int READ_BYTE_COUNT = 30;

    boolean readBytes(byte buf[], int len){
	bCount = 0;
	rLen = 0;
	int count = 0;

	while(rLen != len){
	    bCount = socket.readBytes(buf, rLen, len - rLen);
	    if(bCount <= 0 || count > READ_BYTE_COUNT){
		errStr = new String("L" + location + "RB" +
				    bCount+ ";" + count); 
		return true;
	    }
	    rLen += bCount;
	    bCount = 0;
	    count ++;
	}
	
	return false;
    }

    int location;
    char cmd = 'Z';

    void flushSocket(String msg){
	int count;
	
	// should send msg to parent call back
	if(msg == null){
	    msg = new String("Flushing buffer");
	}
	caller.printStatus(msg);

	if(!socket.isOpen()){
	    if(!openConnect()){
		caller.printStatus("Err Opn Flush");
	    }
	    return;
	}

	if(platform != PALM){
	    closeConnect();
	    openConnect();
	    return;
	}


	buf[0] = (byte)'c';
	socket.writeBytes(buf, 0, 1);
	Vm.sleep(200);

	count = 0;
	// Read as many bytes as we can till we
	// get no more
	while(!readBytes(buf, 100)){
	    caller.printStatus(msg + ": " + count + ": " + errStr);
	    count++ ;
	    Vm.sleep(1000);
	}
	
	flushed = true;
    }	


    public A2DProbe getProbe(A2DProbe probes[])
    {
	int i = 0;

	if(probes == null){
	    // eventually we would like to be able
	    // to create probes on the fly
	    errStr = new String("No known probes");
	    return null;
	}

	//  First get probe info from server
	if(!start()){
	    return null;
	}
	
	location=8;
	if(readBytes(buf, 1)){
	    return null;
	}

	if(buf[0] == (byte)'n'){
	    // Probe Server is giving probe info
	    
	    location = 9;
	    if(readBytes(buf, 8)){
		return null;
	    }

	    id = readInt(buf, 0);
	    numValues = readInt(buf, 4);

	    flushSocket("Matching probe");
	} else {
	    flushSocket("Error in getProbe");
	    return null;
	}
	
	currProbe = null;
	for(i = 0; i<probes.length; i++){
	    if(probeMatch(probes[i])){
		currProbe = probes[i];
		return currProbe;
	    }
	}

	return null;
    }

    boolean probeMatch(A2DProbe probe){
	if(probe == null){
	    return false;
	}

	if(probe.numValues == numValues){
	    return true;
	} else {
	    return false;
	}
    }

    void readError()
    {
	flushSocket(errStr);
	start();
    }


    String msgStr = new String();
    /*
     * status 
     *  -1 fatal error stop!!: errStr
     *  0  got Data
     *  1  stopped
     */

    public float[] getData()
    {
	int value;
	int i;
	int strLen;
	float [] output = null;

	status = -1;


	while(true){
	    // Make sure we are still connected
	    if( !socket.isOpen()){
		socket.close();
		socket = null;
		if(!openConnect()){
		    errStr = new String("getD connect");
		    return null;
		}
	    }

	    if(sendACount == sendALength){
	      // Send an A
	      sentAAt = dataCount;
	      buf[0] = (byte)'a';
	      socket.writeBytes(buf, 0, 1);
	      // Do I need to do this
	      Vm.sleep(100);

	    } 
	    sendACount++;

	    location = 0;
	    if(readBytes(buf, 1)){
		readError();
		status = 2;
		return null;
	    }

	    if(buf[0] == (byte)'A'){
		// Probe Server responding to our request
		caller.printStatus("Got an A");
		
		// How many points have arrived since
		// we requested the A
		dataLatency = dataCount - sentAAt;
		skipCount = 0;
		skipping = false;
		if(dataLatency > sendALength){
		  flushSocket("Large Latency");
		  start();
		  if(skipSpace != 1){
		    skipSpace = 1;
		    skipLength = 1;
		  } else {
		    skipLength++;
		  }
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
		if(readBytes(buf, 8)){
		    readError();
		    status = 2;
		    return null;
		}

		id = readInt(buf, 0);
		numValues = readInt(buf, 4);

		// Verify that the probe is the same.
		// only restart if the probe changes
		if(!probeMatch(currProbe)){
		    // Stop the streaming and flush the buffer
		    flushSocket("New Probe Attached");

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
	      
		if(numValues <= 0){
		    // we should try to restart the stream
		    flushSocket(null);

		    if(!start()){
			// couldn't start
			errStr = new String("Couldn't restart");
			return null;
		    }
		    status = 2;
		    return null;
		}

		location = 2;
		if(readBytes(buf, 4*numValues)){
		    readError();
		    status = 2;
		    return null;
		}

		if(!skipping){
		  
		  output = new float[numValues];
		  for(i=0;i<numValues;i++){
		    output[i] = (float)readInt(buf, i*4);
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
		if(readBytes(buf, 4)){
		    readError();
		    status = 2;
		    return null;
		}
		strLen = readInt(buf, 0);

		location = 4;
		if(readBytes(buf, strLen)){
		    readError();
		    status = 2;
		    return null;
		}

		for(i=0; i<strLen; i++){
		    strBuf.append((char)buf[i]);
		}

		// Stop stream and flush buffer
		flushSocket("Paused");

		status = 1;
		msgStr = new String("Paused: " + strBuf);
		return null;
	    } 
	
	    return null;

	}


    }

}







