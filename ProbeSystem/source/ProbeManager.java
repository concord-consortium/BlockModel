

public class ProbeManager extends Object
{
    public final static int ERROR = -1;
    public final static int OK = 0;
    public final static int NEW_PROBE = 1;
    public final static int PROBE_INFO = 2;
    public final static int PROBE_DATA = 3;
    public final static int ACK = 6;

    public final static int INFO_SIZE = 4;

    int response;
    int probeId;
    ProbeInfo curInfo;
    float [] curData;
    String msg;
    ProbeInfo probes[] = new ProbeInfo[1];
    String name;

    ProbeInfo [] probeInfo;
    int value;
    int posInfo;
    boolean started;
    boolean activeUpdate = true;

    public ProbeManager()
    {
    }

    public ProbeManager(String name)
    {
	probeInfo = new ProbeInfo [INFO_SIZE];
	probeInfo[0] = new ProbeInfo(ProbeInfo.SIZE, INFO_SIZE);
	probeInfo[1] = new ProbeInfo(ProbeInfo.NUM, 1);
	probeInfo[2] = new ProbeInfo(ProbeInfo.UNITS, "Fake");
	probeInfo[3] = new ProbeInfo(ProbeInfo.NAME, name);

	this.name = "LOCAL";
	posInfo = INFO_SIZE;
	curData = new float[1];
	value = 0;
	started = false;
	
	probes[0] = new ProbeInfo(ProbeInfo.NAME, name);
	probes[0].id = 1;
	probeId = 1;
    }

    void close(){}

    ProbeInfo [] getProbes()
    {
	return probes;
    }

    void requestAck()
    {
    }

    boolean getPackage()
    {
	probeId = 1;

	try{
	    Thread.sleep(500);
	} catch (InterruptedException e) {
	    return false;
	}
	if(posInfo < INFO_SIZE){
	    response = PROBE_INFO;
	    curInfo = probeInfo[posInfo];
	    posInfo++;
	    return true;
	}

	if(started){
	    response = PROBE_DATA;
	    curData[0] = (float)(value % 30);
	    value++;
	    return true;
	}
	
	probeId = 0;
	response = PROBE_INFO;
	curInfo = new ProbeInfo(ProbeInfo.STOPPED, 
				"You already stopped the probe");
	return true;
    }

    boolean start(int id)
    {
	if(id == 1){
	    started = true;
	}

	return true;
    }

    boolean stop(int id)
    {
	if(id == 1){
	    started = false;
	}

	return true;
    }
    
    boolean readInfo(int id)
    {
	if(id == 1){
	    posInfo = 0;
	}
	
	return true;
    }

}








