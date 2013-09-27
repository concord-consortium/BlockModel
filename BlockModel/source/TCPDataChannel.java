import waba.io.*;
import waba.sys.*;
	
class TCPDataChannel extends Object
{
    protected int RETRY_COUNT = 20;
    String addr = "4.19.234.82";
    int port = 100;
    Socket socket = null;

    byte buf[] = new byte[64];
    int noCount = 0;
    int ovCount = 0;

    public int status;
    public String errStr = new String("");

    final static int ERROR = -1;
    final static int OK = 0;

    boolean flushed = false;

    public TCPDataChannel(String a, int p, int retry){
	addr = a;
	port = p;
	socket = null;

	RETRY_COUNT = retry;
	if(!open()){
	    status = ERROR;
	} else {
	    status = OK;
	}

    }

    public boolean open()
    {
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
		    errStr = "new Socket";
		    return false;
		}
		Vm.sleep(200);
		socket = new Socket(addr, port);
		Vm.sleep(200);
		
	    }
	
	    socket.setReadTimeout(100);

	    flushed = true;
	}

	return true;
    }

    public boolean close()
    {
	if(socket != null){
	    socket.close();

	    socket = null;
	} 

	return true;
    }


    public final static void writeInt(int val, byte buf[], int start)
    {
	int i;
	
	for(i=0; i <= 3; i++){
	    buf[start + i] = (byte)((val >> (8*(3-i))) & 0x0FF);
	}
    }

    public final static void writeFloat(float val, byte buf[], int start)
    {
	writeInt(Convert.toIntBitwise(val), buf, start);

    }

    public final static int readInt(byte buf[], int start)
    {
	int value;
	int i;

	value = 0;
	for(i=3; i >= 0; i--){
	    value |= (0x0FF & buf[3-i+start]) << (8*i);
	}

	return value;
    }

    public final static float readFloat(byte buf[], int start)
    {
	int value;
	
	value = readInt(buf, start);
	return Convert.toFloatBitwise(value);
    }

    public int bCount;
    int rLen;
    int location;
    
    final static int READ_BYTE_COUNT = 30;
    boolean readBytes(byte buf[], int len){
	bCount = 0;
	rLen = 0;
	int count = 0;

	if(!open()){
	    return false;
	}
	    
	while(rLen != len){
	    bCount = socket.readBytes(buf, rLen, len - rLen);
	    if(bCount < 0 || count > READ_BYTE_COUNT){
		if(bCount < 0){
		    errStr = "readBytes";
		    status = ERROR;
		    return false;
		} else {
		    // just a timed out read operation
		    errStr = "Timed Out";
		    status = OK;
		    return false;
		}
	    }
	    rLen += bCount;
	    bCount = 0;
	    count ++;
	}

	return true;
    }

    boolean writeBytes(byte buf[], int len)
    {
	if(!open()){
	    return false;	    
	}
	    
	// Should check how much is written
	if(socket.writeBytes(buf, 0, len) != len){
	    errStr = "writeBytes";
	    return false;
	}

	return true;
    }

    boolean writeByte(byte b)
    {
	byte [] tmpByte = new byte [1];
	tmpByte[0] = b;

	if(!open()){
	    return false;	    
	}
	    
	// Should check how much is written
	if(socket.writeBytes(tmpByte, 0, 1)  != 1){
	    errStr = "writeByte";
	    return false;
	}

	return true;
    }

    // timeout in deciseconds
    // this doesn't look good
    boolean flushInput(int timeout)
    {
	int i;

	for(i=0; i<timeout; i++){
	    Vm.sleep(100);
	    
	}

	return true;
    }

}










