import java.util.*;

class ManagerWorker extends ProbeState 
    implements Runnable
{
    Vector probes;
    Vector cmdQueue;
    ProbeManager pm;
    Hashtable idTable;

    boolean connected = true;

    public ManagerWorker(){
	super(0, "Manager", null);
    }

    public ManagerWorker(ProbeManager _pm)
    {
	super(0, "Manager", null);

	ProbeInfo probeNames [];
	int i;
	ProbeInfo pi;
	ProbeState probeState;

	pm = _pm;
	// Need to initialize all Vectors and Hashtables
	probes = new Vector();
	cmdQueue = new Vector();
	idTable = new Hashtable();
	// Add ourselves to the idTable 
	// for simple command dispatch
	idTable.put(new Integer(0), this);

	// get list of probes from ProbeManager
	probeNames = pm.getProbes();
	if(probeNames == null){
	    // no probes attached
	    ProbeSystem.logPrintln("No probes attached");
	    return;
	}

	for(i = 0; i < probeNames.length; i++){
	    probeState = new ProbeState(probeNames[i], this);
	    probes.addElement(probeState);
	    idTable.put(new Integer(probeNames[i].id), probeState);

	    // Ask ProbeManager for information	    
	    doCmd(new ProbeCmd(ProbeCmd.READ_INFO, null, probeState), false);
	    pm.start(probeState.id);	    
	    probeState.started = true;

	}
	    
    }

    boolean gotInfo(ProbeInfo pi)
    {
	ProbeState ps = null;

	if(pi.type == ProbeInfo.NEW_PROBE){
	    // make a new probe
	    ProbeInfo newPi = new ProbeInfo(ProbeInfo.NAME, pi.strVal);
	    newPi.id = pi.id;
	    ps = new ProbeState(newPi, this);
		    
	    // add it to the probe list
	    synchronized(probes){
		probes.addElement(ps);
	    }

	    // add it to the id list
	    idTable.put(new Integer(pi.id), ps);

	    // Ask ProbeManager for information	    
	    doCmd(new ProbeCmd(ProbeCmd.READ_INFO, null, ps), false);

	    // start the probe
	    pm.start(ps.id);	    
	    ps.started = true;

	} else if(pi.type == ProbeInfo.DELETED){	    
	    // find probeState
	    ps = (ProbeState)idTable.get(new Integer(pi.id));
	    
	    synchronized(probes){
		probes.removeElement(ps);
		idTable.remove(new Integer(pi.id));
	    }
	    ps.available = false;

	} else {		    
	    return false; 
	}
	
	Enumeration clients;
	ProbeClient client;

	// Pass this info on like a good probeState :)
	synchronized(probeClients){
	    for(clients = probeClients.elements();
		clients.hasMoreElements();){
		client = (ProbeClient)clients.nextElement();
		client.gotInfo(ps, pi);
	    }
	}

	return true;

    }

    boolean gotData(float [] data)
    {

	return false;
    }

    public void doCmd(ProbeCmd cmd, boolean block)
    {
	synchronized(this){

	    ProbeSystem.logPrintln("Added command");
	    cmdQueue.addElement(cmd);
	    notifyAll();
	}

	if(block){
	    synchronized(cmd){
		// Wait for command to complete
		if(!cmd.completed)
		    try{cmd.wait();} catch (InterruptedException e){};
	    }
	}
    }

    public ProbeState findProbe(int id)
    {
	return (ProbeState)idTable.get(new Integer(pm.probeId));

    }

    // Return a list of currently attached probes
    public Vector getProbes()
    {
	if(!pm.activeUpdate){ 
	    // The Probe manager isn't sending us info about
	    // probe removal and adds
	    // So.. we must ask it when we are asked.
	    doCmd(new ProbeCmd(ProbeCmd.GET_PROBES, null, null), true);
	}

	synchronized(probes){
	    return (Vector)probes.clone();
	}
    }

    public void run(){
	stream();
    }

    boolean send(ProbeCmd cmd)
    {
	int psId = -1;
	if(cmd.ps != null){
	    psId = cmd.ps.id;
	}

	ProbeSystem.logPrintln("Sending command for " + psId + " probe");

	switch(cmd.cmd){
	case ProbeCmd.START :
	    // Check to see if we already started this probe
	    if(!cmd.ps.started){
		pm.start(cmd.ps.id);
		cmd.ps.started = true;
	    }
	    
	    ProbeSystem.logPrintln("Sent start command");
	    break;

	case ProbeCmd.STOP :
	    cmd.ps.probeClients.removeElement(cmd.pc);
	    break;

	case ProbeCmd.READ_INFO :
	    pm.readInfo(cmd.ps.id);
	    ProbeSystem.logPrintln("Sent readInfo command");
	    break;

	case ProbeCmd.GET_PROBES :
	    // ignoring update probes
	    if(!pm.activeUpdate){
		ProbeInfo [] probeNames = pm.getProbes();
		if(probeNames == null){
		    // no probes attached
		    ProbeSystem.logPrintln("No probes attached");
		    break;
		}

		for(int i = 0; i < probeNames.length; i++){
		    // see if we already have this probe
		    ProbeState ps = (ProbeState)idTable.get(new Integer(probeNames[i].id));
		    if(ps == null){
			ps = new ProbeState(probeNames[i], this);
			synchronized(probes){
			    probes.addElement(ps);
			}
			idTable.put(new Integer(probeNames[i].id), ps);
			
			pm.readInfo(ps.id);
		    }
		}
	    }
	    break;
	default :
	}

	synchronized(cmd){
	    cmd.completed = true;
	    cmd.notifyAll();
	}

	return true;
    }

    void stream()
    {
	Enumeration cmds;
	ProbeCmd cmd;
	ProbeState ps;

	while(true){
	    synchronized(this){
		for(cmds = cmdQueue.elements();
		    cmds.hasMoreElements();){
		    cmd = (ProbeCmd)cmds.nextElement();
		    if(send(cmd)){
			cmdQueue.removeElement(cmd);
		    }
		}
	    }

	    if(pm.getPackage()){
		// find probeState
		ps = (ProbeState)idTable.get(new Integer(pm.probeId));
		// call appropriate function in probeState
		// info, data, stop
		switch(pm.response){
		case ProbeManager.PROBE_INFO :
		    ProbeSystem.logPrintln("Got info from pm");
		    ps.gotInfo(pm.curInfo);
		    break;
		    
		case ProbeManager.PROBE_DATA :
		    ps.gotData(pm.curData);
		    break;
		}
	    }  else {
		// there is no data right now
	    }    
	}
    }

    public String printStatus()
    {
	String outputStr = "";
	// ps
	Enumeration prb_enum = probes.elements();
	for(;prb_enum.hasMoreElements();){
	    ProbeState ps = (ProbeState)prb_enum.nextElement();
	    outputStr = outputStr + ps.printStatus();
	}
	
	return outputStr;	
    }
}







