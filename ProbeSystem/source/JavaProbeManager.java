/*
 *  Should add in ack checking
 *  it isn't a big deal on powerfull machines
 *  but I think it will be on CE.
 */
public class JavaProbeManager extends ProbeManager
{
    TCPDataChannel dc;
    ProbeInfo probes[];
    byte buf [] = new byte [14];
    byte objectBuf [] = new byte [20];


    public JavaProbeManager(String a, int p, int retry, String name)
    {
	response = OK;
	this.name = name;
	activeUpdate = true;

	// init TCPDataChannel
	dc = new TCPDataChannel(a, p, retry);
	if(dc.status == dc.ERROR){
	    response = ERROR;
	    return;
	}

	buf[0] = (byte)'b';
	buf[1] = (byte)'\r';
	buf[2] = (byte)'\n';

	// init ProbeManager (set binary)
	if(!dc.writeBytes(buf, 3)){
	    response = ERROR;
	    return;
	}

	// should look for B
	if(!dc.readBytes(buf, 1)){
	    response = ERROR;
	    return;
	}

	if((char)buf[0] != 'B'){
	    response = ERROR;
	    return;
	}

    }

    void close()
    {
	// send a q 
	if(!dc.writeByte((byte)'q')){
	    response = ERROR;
	    return;
	}

	// should look for Q
	if(!dc.readBytes(buf, 1)){
	    response = ERROR;
	    return;
	}

	if((char)buf[0] != 'Q'){
	    response = ERROR;
	    return;
	}

	dc.close();
    }

    void requestAck()
    {
	// send a p 
	if(!dc.writeByte((byte)'a')){
	    // error
	}	
    }	


    /*
     *  This must be called first
     *  calling it in the middle of 
     *  data collection will not work
     */
    ProbeInfo [] getProbes()
    {
	char strBuf [];
	int len;
	int numProbes;
	int type;

	// send a p 
	if(!dc.writeByte((byte)'p')){
	    response = ERROR;
	    return null;
	}
	
	if(!dc.readBytes(buf, 13)){
	    response = ERROR;
	    return null;
	}

	if((char)buf[0] != 'I'){
	    // error
	    return null;
	}
	probeId = dc.readInt(buf, 1);
	type = dc.readInt(buf, 5);
	len = dc.readInt(buf, 9);
	if(!(probeId == 0 && type == ProbeInfo.NUM)){
	    // error
	    response = ERROR;
	    return null;
	}

	dc.readBytes(buf, 4);
	numProbes = dc.readInt(buf, 0);
	if(numProbes == 0){
	    // no probes
	    response = OK;
	    return null;
	}

	probes = new ProbeInfo [numProbes];

	for(int i=0;i<numProbes;i++){
	    dc.readBytes(buf, 13);
	    if((char)buf[0] != 'I'){
		// error
		return null;
	    }
	    probeId = dc.readInt(buf, 1);
	    type = dc.readInt(buf, 5);
	    len = dc.readInt(buf, 9);
	    if(type != ProbeInfo.NAME){
		// error
		return null;
	    }
	    if(objectBuf.length < len){
		objectBuf = new byte[len];
	    }
	    dc.readBytes(objectBuf, len);
	    strBuf = new char[len];
	    for(int j=0; j<len; j++){
		strBuf[j] = (char)objectBuf[j];
	    }
	    probes[i] = new ProbeInfo(type, new String(strBuf));
	    probes[i].id = probeId;
	}
	
	return probes;
    }

    boolean getPackage()
    {
	int type;
	int len;
	
	probeId = 0;

	if(!dc.readBytes(buf, 1)){
	    return false;
	}

	switch((char)buf[0]){
	case 'A':
	    response = ACK;
	    probeId = 0;
	    break;
	case 'D':
	    response = PROBE_DATA;

	    if(!dc.readBytes(buf, 4)){
		return false;
	    }
	    probeId = dc.readInt(buf, 0);

	    // read data
	    dc.readBytes(buf, 4);
	    len = dc.readInt(buf, 0);
	    curData = new float[len];
	    if(objectBuf.length < (len *4)){
		objectBuf = new byte [len * 4];
	    }
	    dc.readBytes(objectBuf, len * 4);

	    for(int i=0; i<len; i++){
		curData[i] = dc.readFloat(objectBuf, i*4);
	    }

	    break;
	case 'I':
	    response = PROBE_INFO;

	    if(!dc.readBytes(buf, 4)){
		return false;
	    }
	    probeId = dc.readInt(buf, 0);

	    // read type and length
	    dc.readBytes(buf, 8);
	    type = dc.readInt(buf, 0);
	    len = dc.readInt(buf, 4);

	    objectBuf = new byte [len];
	    dc.readBytes(objectBuf, len);
	    
	    curInfo = new ProbeInfo(type, objectBuf);
	    curInfo.id = probeId;
	    if(curInfo.type == curInfo.NEW_PROBE ||
	       curInfo.type == curInfo.DELETED){
		probeId = 0;
	    }

	    break;
	}

	return true;
    }

    boolean start(int id)
    {
	buf[0] = (byte)'s';
	dc.writeInt(id, buf, 1);
	
	dc.writeBytes(buf, 5);

	return true;
    }

    boolean stop(int id)
    {
	buf[0] = (byte)'e';
	dc.writeInt(id, buf, 1);
	
	dc.writeBytes(buf, 5);

	return true;
    }
    
    boolean readInfo(int id)
    {
	buf[0] = (byte)'i';
	dc.writeInt(id, buf, 1);
	
	dc.writeBytes(buf, 5);

	return true;
    }

}







