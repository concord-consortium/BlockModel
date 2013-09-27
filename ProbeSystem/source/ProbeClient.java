import java.io.*;
import java.util.*;

public class ProbeClient extends ManagerWorker 
    implements Runnable
{
    DataOutputStream out;
    DataInputStream in;

    boolean text = true;
    ManagerWorker mw;
    StringTokenizer cmdST;
    Vector addedProbes;
    Hashtable availableProbes;
    Hashtable probeIds;
    Vector managerWorkers;
    int nextId;
    Vector deletedIds = new Vector();
    Enumeration deleId_enum;

    // Store probe states for client initialized probes
    Vector clientProbes = new Vector();

    final static String COMMANDS = 
	"p - list currently attached probes\n\r" +
	"  Output: [id] \"Name\" \n\r" +
	"s [id] - start sending data from [id] probe\n\r" +
	"  Output: D [id] [numPts] [pt0] [pt1] .. [ptX]\n\r" +
	"e [id] - stop sending data from [id] probe\n\r" +
	"? - print these commands\n\r" +
	"q - stop all data and close connection\n\r" +
	"i,a,b,t - undocumented\n\r";

    public ProbeClient(OutputStream o, InputStream i)
    {
	managerWorkers = new Vector();
	out = new DataOutputStream(o);
	in = new DataInputStream(i);
	addedProbes = new Vector();
	availableProbes = new Hashtable(); 
	probeIds = new Hashtable();
	nextId = 1;
	connected = true;
    }

    /*
     * ManagerWorker extensions
     * It might be better to turn the ManagerWorker
     * into an interface
     */

    // Return a list of currently attached probes
    public Vector getProbes()
    {
	synchronized(clientProbes){
	    return (Vector)clientProbes.clone();
	}
    }

    public String printStatus()
    {
	String outputStr = "";
	// ps
	synchronized(clientProbes){
	    if(clientProbes.size() == 0){
		return "No Probes setup by client\n\r";
	    } 
	    Enumeration prb_enum = clientProbes.elements();
	    for(;prb_enum.hasMoreElements();){
		ProbeState ps = (ProbeState)prb_enum.nextElement();
		outputStr = outputStr + ps.printStatus();
	    }
	}
	return outputStr;	
    }

    boolean gotInfo(ProbeInfo pi)
    {
	return true;
    }

    boolean gotData(float [] data)
    {
	return true;
    }

    void addMW(ManagerWorker m)
    {
	Enumeration probes;
	ProbeState ps;

	managerWorkers.addElement(m);
	for(probes = m.getProbes().elements();
	    probes.hasMoreElements();){
	    ps = (ProbeState)probes.nextElement();
	    availableProbes.put(new Integer(nextId), ps);
	    probeIds.put(ps, new Integer(nextId));
	    nextId++;
	}
	
	// add the managerWorker itself
	probeIds.put(m, new Integer(0));
	m.addClient(this);		   
    }


    void updateProbes()
    {
	Enumeration mw_enum;
	ManagerWorker mw;
	Enumeration probes;
	ProbeState ps;
	Enumeration oldProbesId_enum;
	Integer id;

	// Remove old probes
	oldProbesId_enum = availableProbes.keys();
	for(;oldProbesId_enum.hasMoreElements();){
	    id = (Integer)oldProbesId_enum.nextElement();
	    ps = (ProbeState)availableProbes.get(id);
	    if(ps.available  == false){
		availableProbes.remove(id);
		probeIds.remove(ps);
		deletedIds.addElement(id);
	    }
	}

	// Add new ones
	mw_enum = ((Vector)(managerWorkers.clone())).elements();
	deleId_enum = deletedIds.elements();
	for(;mw_enum.hasMoreElements();){
	    mw = (ManagerWorker)mw_enum.nextElement();
	    if(!mw.connected){
		managerWorkers.removeElement(mw);
		continue;
	    }
	    probes = mw.getProbes().elements();
	    for(;probes.hasMoreElements();){
		ps = (ProbeState)probes.nextElement();
		if(probeIds.get(ps) == null){
		    if(deleId_enum.hasMoreElements()){
			id = (Integer)deleId_enum.nextElement();
			deletedIds.removeElement(id);
		    } else {
			id = new Integer(nextId++);
		    }
		    availableProbes.put(id, ps);
		    probeIds.put(ps, id);
		}
	    }
	}
    }

    void writeInt(int i) throws IOException
    {
	if(text){
	    out.writeBytes(i + " ");
	} else {
	    out.writeInt(i);
	}
    }

    void writeFloat(float f) throws IOException
    {
	if(text){
	    out.writeBytes(f + " ");
	} else {
	    out.writeInt(Float.floatToIntBits(f));
	}
    }

    void writeCmd(char c) throws IOException
    {
	if(text){
	    out.writeBytes(c + " ");
	} else {
	    out.writeByte((int)c);
	}
    }

    void writeString(String s) throws IOException
    {
	if(s != null){
	    out.writeBytes(s);
	} else {
	    out.writeBytes("NULL");
	}		
    }

    char readCmd() throws CmdErrorException, IOException 
    {
	if(text){
	    String inLine = in.readLine();
	    if(inLine == null){
		throw new CmdErrorException();
	    }

	    cmdST = new StringTokenizer(inLine);
	    
	    try{
		return cmdST.nextToken().charAt(0);
	    } catch(NoSuchElementException e){
		throw new CmdErrorException();
	    }

	} else {
	    return (char)in.readByte();
	}
    }

    int readInt() throws CmdErrorException, IOException
    {
	if(text){
	    if( !cmdST.hasMoreTokens() ){
		throw(new CmdErrorException());
	    }
	    try{
		return Integer.valueOf(cmdST.nextToken()).intValue();
	    } catch (NumberFormatException e){
		throw(new CmdErrorException());
	    }
	} else {
	    return in.readInt();
	}
    }

    float readFloat() throws CmdErrorException, IOException
    {
	if(text){
	    if( !cmdST.hasMoreTokens() ){
		throw(new CmdErrorException());
	    }

	    return Float.valueOf(cmdST.nextToken()).floatValue();

	} else {
	    int bitVal = in.readInt();
	    return Float.intBitsToFloat(bitVal);
	}
    }

    String readString(int len) throws CmdErrorException, IOException
    {
	int readLength;
	int numBytes;

	if(text){
	    if( !cmdST.hasMoreTokens() ){
		throw new CmdErrorException();
	    }

	    // return all the rest of the chars int the string
	    return cmdST.nextToken("");

	} else {
	    byte[] buffer = new byte [len];
	    readLength = 0;
	    while(readLength < len){
		numBytes = in.read(buffer, readLength, len - readLength);
		if(numBytes == -1){
		    throw new CmdErrorException();
		}
		readLength += numBytes;
	    }
	    
	    return new String(buffer);
	}
    }

    public void gotInfo(ProbeState ps, ProbeInfo info)
    {
	byte buf [];

	if(!connected){
	    return;
	}

	Integer id = (Integer)probeIds.get(ps);
	if(id == null){
	    // We haven't seen this probe before so add it
	    // to our list
	    deleId_enum = deletedIds.elements();
	    if(deleId_enum.hasMoreElements()){
		id = (Integer)deleId_enum.nextElement();
		deletedIds.removeElement(id);
	    } else {
		id = new Integer(nextId++);
	    }
	    availableProbes.put(id, ps);
	    probeIds.put(ps, id);
	}

	synchronized(out){
	    try{
		writeCmd('I');
		writeInt(id.intValue());
		if(text){
		    writeString(info.toString() + "\n\r");
		} else {
		    buf = info.toBytes();
		    out.write(buf, 0, buf.length);
		}

	    } catch(IOException e) {
		quit = true;
		connected = false;
		ProbeSystem.logPrintln("Died in write info");
	    }
	}
    }

    public void gotData(ProbeState ps, float []data)
    {
	int i;
	int id;

	if(!connected || data == null){
	    return;
	}

	id = ((Integer)probeIds.get(ps)).intValue();

	synchronized(out){
	    //save this command and data for later execution
	    try{
		writeCmd('D');
		writeInt(id);
		writeInt(data.length);
		
		for(i = 0; i < data.length; i++){
		    writeFloat(data[i]);
		}
		
		if(text){
		    writeString("\n\r");
		}
	    } catch(IOException e) {
		quit = true;
		connected = false;
		ProbeSystem.logPrintln("Died in write data");
	    }
	}
    }

    boolean quit = false;

    public void run()
    {
	char cmd;
	int id;
	ProbeState ps;
        Enumeration probes;

	// need to sit and check for input
	// and need to safety thread the gotData
	while(true){

	    try{
		// read input command and act on it.
		// for text this should read the whole thing at once
		cmd = readCmd();
		switch(cmd){
		case 's' :
		    // start the id probe
		    id = readInt();
		    
		    // figure out which probe is this
		    ps = (ProbeState) availableProbes.get(new Integer(id));
		    
		    if(ps == null){
			if(text){
			    writeString("Unknown probe: " + id + "\n\r");
			}
			break;
		    }

		    // add ourselves to the list
		    ps.addClient(this);
		    
		    addedProbes.addElement(ps);
		    break;
		case 'i' :
		    ProbeInfo pi;
		    byte buf [];

		    // return the info on the id
		    id = readInt();
		    if(id == 0){
			// Print info on all the manager workers
			Enumeration mw_enum = managerWorkers.elements();
			for(;mw_enum.hasMoreElements();){			
			    mw = (ManagerWorker)mw_enum.nextElement();
			    writeString(mw.printStatus());
			}			    
			break;
		    }

		    // Which probe is it
		    ps = (ProbeState) availableProbes.get(new Integer(id));
		 
		    if(ps == null){
			if(text){
			    writeString("Unknown probe: " + id + "\n\r");
			}
			break;
		    }

		    synchronized(out){
			// Send info
			for(probes = ps.probeInfo.elements();
			    probes.hasMoreElements();){
			    pi = (ProbeInfo)probes.nextElement();
			    writeCmd('I');
			    writeInt(id);
			    if(text){
				writeString(pi.toString() + "\n\r");
			    } else {
				buf = pi.toBytes();
				out.write(buf, 0, buf.length);
			    }
			}		    
		    }
		    break;
		case '?' :
		    // return commands
		    if(text){
			writeString(COMMANDS);
		    }
		    break;
		case 'e' :
		    // stop the id probe
		    id = readInt();

		    // figure out which probe this is
		    ps = (ProbeState) availableProbes.get(new Integer(id));

		    if(ps == null){
			if(text){
			    writeString("Unknown probe: " + id + "\n\r");
			}
			break;
		    }

		    // Stop probe
		    ps.removeClient(this);
		    addedProbes.removeElement(ps);
		    ProbeSystem.logPrintln("Probe stopped");
		    break;
		case 'p' :
		    // list probes
		    updateProbes();
		    
		    synchronized(out){
			
			if(!text){
			    writeCmd('I');
			    writeInt(0);
			    writeInt(ProbeInfo.NUM);
			    writeInt(4);
			    writeInt(availableProbes.size());
			}
			
			for(probes = availableProbes.elements();
			    probes.hasMoreElements();){
			    ps = (ProbeState)probes.nextElement();
			    id = ((Integer)probeIds.get(ps)).intValue();
			    if(!text){
				writeCmd('I');
				writeInt(id);
				writeInt(ProbeInfo.NAME);
				writeInt(ps.name.length());
				writeString(ps.name);
			    } else {
				writeInt(id);
				writeString(ps.name);
				writeString("\n\r");
			    }
			}
		    }
		    break;
		case 'b' :
		    // switch to binary
		    text = false;
		    synchronized(out){
			writeCmd('B');
		    }
		    break;
		case 't' :
		    // switch to text
		    text = true;
		    break;
		case 'a' :
		    // send back an ack
		    synchronized(out){
			writeCmd('A');
			if(text){
			    writeString("\n\r");
			}
		    }
		    break;
		case 'q' :
		    quit = true;
		    connected = false;
		    // stop all our probes
		    removeAll();

		    synchronized(out){
			writeCmd('Q');
			if(text){
			    writeString("\n\r");
			}
		    }

		    break;
		case 'I' :
		    // This currently is a bit of a hack
		    // recieving an I just addes the probe to 
		    // the list
		    // recieving info from client its probe
		    ps = new ProbeState(1, "A2D", null);
		    if(clientProbes.size() > 0){
			clientProbes.removeAllElements();
		    }
		    clientProbes.addElement(ps);
		    break;
		case 'D' :
		    // get the data
		    // format is D [len] [val] [val] ...
		    int len = readInt();

		    float [] curData = new float[len];
		    for(int i=0; i<len; i++){
			curData[i] = readFloat();
		    }

		    if(clientProbes.size() == 1){
			ps = (ProbeState)clientProbes.firstElement();
			ps.gotData(curData);
		    }
		    break;		 		    
		}


	    } catch (CmdErrorException e){
		if(text){
		    // return commands
		} else {
		    // send an error
		    return;
		}
	    } catch (EOFException e){
		// client quit without telling us
		ProbeSystem.logPrintln("Client force quit");
		connected = false;
		quit = true;
		removeAll();
	    } catch (IOException e){
		if(text){
		    // hmm..
		    connected = false;
		    quit = true;
		} else {
		    // send error
		    connected = false;
		    quit = true;
		}
		removeAll();
		ProbeSystem.logPrintln("Bizarre client quit");

	    }   

	    if(quit){
		// What about tell the manager workers we done?
		connected = false;
		break;
	    }
	}
	connected = false;

	try{
	    out.close();
	} catch(IOException e) {
	    ProbeSystem.logPrintln("Error on out.close");
	}

	try{
	    in.close();
	} catch( IOException e) {
	    ProbeSystem.logPrintln("Error on out.close");
	}
    }

    void removeAll()
    {
	Enumeration probes;
	ProbeState ps;

	for(probes = addedProbes.elements();
	    probes.hasMoreElements();){
	    ps = (ProbeState)probes.nextElement();
	    ps.removeClient(this);
	}
	addedProbes.removeAllElements();
		  
	if(clientProbes.size() == 1){
	    ((ProbeState)clientProbes.firstElement()).available = false;
	}
    }
}











