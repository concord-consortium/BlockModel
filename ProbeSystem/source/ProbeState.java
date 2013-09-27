import java.util.*;

public class ProbeState extends Object
{
    Vector probeClients;
    int id;
    Vector probeInfo;
    boolean init;
    int infoCount = 100;
    boolean started;
    String name;
    ManagerWorker mw;
    ProbeInfo sizeInfo;
    boolean available;
    float curData [] = null;

    public ProbeState()
    {
	mw = null;
	id = 0;
	started = false;
	probeInfo = null;
    }

    public ProbeState(ProbeInfo pi, ManagerWorker m)
    {
	this(pi.id, pi.strVal, m);
    }

    public ProbeState(int id, String name, ManagerWorker m)
    {
	this.id = id;
	mw = m;
	probeClients = new Vector();
	probeInfo = new Vector();
	started = false;
	this.name = name;
	if(mw != null){
	    probeInfo.addElement(new ProbeInfo(id, ProbeInfo.MANAGER, mw.pm.name));
	    sizeInfo = new ProbeInfo(id, ProbeInfo.SIZE, 1);
	}
	sizeInfo = new ProbeInfo(id, ProbeInfo.SIZE, 2);
	infoCount = Integer.MAX_VALUE;
	probeInfo.addElement(sizeInfo);
	available = true;
    }

    public boolean addClient(ProbeClient pc)
    {
	synchronized(probeClients){
	    if(id != 0)
		pc.gotData(this, curData);
	    
	    probeClients.addElement(pc);
	}

	return true;
    }

    public boolean removeClient(ProbeClient pc)
    {
	synchronized(probeClients){	    
	    probeClients.removeElement(pc);
	}

	return true;
    }

    boolean gotInfo(ProbeInfo pi)
    {
	int size;
	Enumeration clients;
	ProbeClient client;

	ProbeSystem.logPrintln("PS Got Info: " + pi.toString());

	if(pi.type == ProbeInfo.SIZE){
	    infoCount = pi.intVal - 1;
	    return true;
	} 
	    
	probeInfo.addElement(pi);
	size = sizeInfo.intVal +1;
	sizeInfo.intVal = size; 
	
	if(size - 2 >= infoCount){
	    return false;
	}

	// for every client call its gotInfo function
	synchronized(probeClients){
	    for(clients = probeClients.elements();
		clients.hasMoreElements();){
		client = (ProbeClient)clients.nextElement();
		client.gotInfo(this, pi);
	    }
	}

	return true;
	
    }

    boolean gotData(float []data)
    {
	Enumeration clients;
	ProbeClient client;

	// for every client call its gotData function
	synchronized(probeClients){
	    curData = data;
	    for(clients = probeClients.elements();
		clients.hasMoreElements();){
		client = (ProbeClient)clients.nextElement();
		client.gotData(this, data);
	    }
	}

	return true;
    }
	
    public String printStatus()
    {
	
	return id + ": Num Probe Clients: " + probeClients.size() + "\n\r";

    }

}












