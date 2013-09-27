class uCsimmManagerWorker extends uCsimmProbeManager implements Runnable{
    LinDataProbe curProbe;
    boolean collecting;
    ProbeSystem ps;
    Vector probes;
    Vector cmdQueue;

    public uCsimmManagerWorker(TCPDataChannel dc, ProbeSystem _ps)
    {
	super(dc);
	curProbe = new LinDataProbe();
	ps = _ps;
    }

    public void doCmd(ProbeCmd cmd, boolean block){
	syncronized(this){

	    cmdQueue.add(cmd);

	    notifyAll();
	}

	if(block){
	    syncronized(cmd){
		// Wait for command to complete
		wait();
	    }
	}
    }

    // Return a list of currently attached probes
    public Vector getProbes()
    {

    }

    public boolean addClient(ProbeClient pc, ProbeState ps)
    {
	ProbeCmd cmd;
	// Figure out which probe this client wants
	// Return true if the probe exists and can be queryed
	// Add to command list
	cmd = new CmdStart(ps);

	doCmd(cmd, true);

	// Act on command result
	ps.probeClients.add(pc);
    }

    public void run(){
	/*
	  Handle the probe manager
	  The probe manager should maintain 
	  a current list of active probes
	  If it is not streaming it will request this list
	  If it is streaming it aggregate the addition and
	  removal of probes so it's list is current
	  
	  The probe manager need not be syncronized so
	  the same manager can run in Waba.

	  So I guess the manager worker has two states:
	  Streaming and NotStreaming. 

	  When a client has joined and is requesting data
	  then we are streaming otherwise we are waiting
	  for clients to join.
	*/

	while(true){

	    // Wait for a client to join
	    syncronized(this){
		wait();
	    }

	    // So some client woke us up 
	    // Start streaming
	    stream();
	}
    }

    void stream()
    {
	Enumeration cmds;
	ProbeCmd = cmd;

	while(true){
	    syncronized(this){
		for(cmds = cmdQueue.elements();
		    cmds.hasMoreElements();){
		    cmd = (ProbeCmd)cmds.nextElement();
		    if(cmd.send()){
			cmdQueue.remove(cmd);
		    }
		}

		if(cmdQueue.size() <= 0 && waitingForResponse <= 0){
		    break;
		}
	    }

	    if(pm.getPackage()){
		if(pm.response == ProbeManager.NEW_PROBE){
		    // make a new probe
		    // add it to the id list
		    idTable.put(new Integer(pm.probeId), ps);

		    // send a non-blocking get info command
		    doCmd(ps, false);
		} else {
		    // find probeState
		    ps = idTable.get(new Integer(pm.probeId));
		    // call appropriate function in probeState
		    // info, data, stop
		    switch(pm.response){
		    case ProbeManager.GOT_INFO :
			ps.gotInfo(pm.curInfo);
			break;
		    case ProbeManager.GOT_DATA :
			ps.gotData(pm.curData);
			break;
		    case ProbeManager.STOP_PROBE :
			ps.stopProbe(pm.msg);
			break;
		    }
		}
	    }  else {
		// failed get package????
	    }
	}
    }






