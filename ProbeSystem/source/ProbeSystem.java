import java.util.*;
import java.io.*;
import java.net.*;

class ProbeSystem extends Thread
{
    static boolean logging = false;

    static String usageString = 
	"Usage: java ProbeSystem [-v] [-no1W] [IPaddress]\n" +
	"   -v        Verbose output (default: no output)\n" +
	"   -no1W     Don't look for one1W devices (default: look for 1wire)\n" +
	"  If the [IPaddress] argument exists then the probe server will reflect\n" +
	"  the probe server running at that address.  (no output, no one wire)\n" +
	"  **NOTE** if one wire is not used and an IPaddress is not specified then\n" +
	"     the probe server will essentially not do anything (for now).";


    public static void logPrintln(String str)
    {
	if(logging){
	    System.out.println(str);
	}
    }

    public static void logPrint(String str)
    {
	if(logging){
	    System.out.print(str);
	}
    }

    public static void main(String args[]) throws IOException
    {
	ManagerWorker mw = null;
	ProbeClient pc;
	Vector probes;
	Thread mwThread;
	ServerSocket ss;
	OutputStream out;
	InputStream in;
	Socket server;
	int port = 1234;
	int secondServer = 0;
	JavaProbeManager jpm;
	TBProbeManager tbpm;
	boolean oneWire = true;

	if(args.length > 0){
	    for(int i= 0; i < args.length; i++){
		if(args[i].equals("-v")){
		    logging = true;
		} else if(args[i].equals("-no1W")){ 
		    oneWire = false;
		} else if(args[i].equals("-h") ||
		     args[i].equals("-?")){
		    System.out.println(usageString);
		    System.exit(0);
		} else {
		    oneWire = false;
		    jpm = new JavaProbeManager(args[i], 1234, 1, "Remote");
		    mw = new ManagerWorker(jpm);
		    (new Thread(mw)).start();
		}
	    }
	}

	if(oneWire) {
	    tbpm = new TBProbeManager(((InetAddress.getLocalHost().getAddress()[3]) & 0xFF) + "");
	    mw = new ManagerWorker(tbpm);
	    (new Thread(mw)).start();
	}

	Vector managerWorkers = new Vector();
	Enumeration mw_enum;
	if(mw != null){
	    managerWorkers.addElement(mw);
	}

	logPrintln(InetAddress.getLocalHost().toString());
	ss  = new ServerSocket(port);
	
	// new ProbeSystem("228.5.6.7", 1235);

	Vector probeClients = new Vector();
	Enumeration pc_enum;
	mw = null;
	ProbeClient curPC;

	while(true){
	    server = ss.accept();
	    out = server.getOutputStream();
	    in = server.getInputStream();
	    logPrintln("Adding client: " + server.getInetAddress());
	    pc = new ProbeClient(out, in);
	    mw_enum = ((Vector)(managerWorkers.clone())).elements();
	    for(;mw_enum.hasMoreElements();){
		mw = (ManagerWorker)mw_enum.nextElement();
		if(mw.connected){
		    pc.addMW(mw);
		} else {
		    managerWorkers.removeElement(mw);
		}
	    }
	    pc_enum = ((Vector)(probeClients.clone())).elements();
	    for(;pc_enum.hasMoreElements();){
		curPC = (ProbeClient)pc_enum.nextElement();
		if(curPC.connected){
		    curPC.addMW(pc);
		} else {
		    probeClients.removeElement(curPC);
		}
	    }
	    probeClients.addElement(pc);
	    managerWorkers.addElement(pc);
	    (new Thread(pc)).start();
	}
    }
	
    MulticastSocket multiS = null;
    InetAddress group = null;
    int port;

    public ProbeSystem(String addr, int port)
    {
	
	this.port = port;
	try{
	    group = InetAddress.getByName(addr);
	    multiS = new MulticastSocket(port);
	    multiS.setTTL((byte)0x00);
	    multiS.joinGroup(group);
	    logPrintln("Created multicast");
	    start();
	} catch (Exception e){
	    logPrintln("Problem joining group");
	}
    }

    public void run()
    {
	byte [] buf = new byte [1000];
	DatagramPacket recv;
	byte [] yepMsg = {(byte)'y', (byte)'e', (byte)'p'};
	DatagramPacket yep = new DatagramPacket(yepMsg, yepMsg.length, group, port);
	String recvStr;

	try{
	    while(true){
		recv = new DatagramPacket(buf, buf.length);
		multiS.receive(recv);
		recvStr = new String(recv.getData(),  0, recv.getLength());
		logPrintln("Got multicast: " + recvStr);
		if(recvStr.startsWith("yo")){
		    logPrintln("Sending: yep");
		    multiS.send(yep);
		}
	    }
	} catch (Exception e){
	    logPrintln("Problem in Multicast listener");
	}
    }

}












