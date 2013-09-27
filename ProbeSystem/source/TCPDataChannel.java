import java.io.*;
import java.net.*;


class TCPDataChannel extends Object
{
    String addr = "4.19.234.82";
    int port = 100;
    Socket socket = null;

    byte buf[] = new byte[64];
    int noCount = 0;
    int ovCount = 0;

    public int status;
    public String errStr = new String("");

    boolean flushed = false;
    final static int ERROR = -1;
    final static int OK = 0;

    InputStream inS;
    OutputStream out;
    DataOutputStream outS;

    public TCPDataChannel(String a, int p, int retry){
	addr = a;
	port = p;
	socket = null;

	open();

    }

    public boolean open()
    {
	int count;

	if(socket == null){

	    try{
		socket = new Socket(addr, port);

		socket.setSoTimeout(1500);

		inS = socket.getInputStream();
		out = socket.getOutputStream();
		outS = new DataOutputStream(out);

	    } catch(IOException e){
		socket = null;
		status = ERROR;
		return false;
	    } catch(SecurityException e){
		socket = null;
		status = ERROR;
		return false;
	    }

	    flushed = true;
	}

	return true;
    }

    public boolean close()
    {
	if(socket != null){
	    try{
		inS.close();
		outS.close();

		socket.close();
	    } catch(IOException e){
		socket = null;
		status = ERROR;
		return false;
	    }
	} 
	return false;
    }


    public final static void writeInt(int val, byte buf[], int start)
    {
	int i;
	
	for(i=0; i <= 3; i++){
	    buf[start + i] = (byte)((val >> (8*(3-i))) & 0x0FF);
	}
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
	return Float.intBitsToFloat(value);
    }

    public int bCount;
    int rLen;
    int location;
    
    final static int READ_BYTE_COUNT = 30;
    boolean readBytes(byte buf[], int len){
	bCount = 0;
	rLen = 0;
	int count = 0;

	try{
	    while(rLen != len){
		bCount = inS.read(buf, rLen, len - rLen);
		if(bCount <= 0 || count > READ_BYTE_COUNT){
		    errStr = new String("L" + location + "RB" +
					bCount+ ";" + count); 
		    return true;
		}
		rLen += bCount;
		bCount = 0;
		count ++;
	    }
	} catch (InterruptedIOException e){
	    // silently return false
	    // this will happen often with a short time out
	    return false;
	} catch(IOException e) {
	    ProbeSystem.logPrintln("Failed reading bytes: "+ e.toString());
	    return false;
	} 

	return true;
    }

    boolean writeBytes(byte buf[], int len)
    {
	try{
	    outS.write(buf, 0, len);
	    outS.flush();
	} catch(IOException e){
	    return false;
	}
	return true;
    }

    boolean writeByte(byte b)
    {

	try{
	    outS.writeByte((int)b);
	    outS.flush();
	} catch(IOException e){
	    return false;
	}
	
	return true;
    }

    // timeout in deciseconds
    boolean flushInput(int timeout)
    {
	int i;

	for(i=0; i<timeout; i++){
	    try{
		Thread.sleep(100);
		inS.skip(inS.available());
	    } catch (InterruptedException e){
		return false;
	    } catch (IOException e){
		return false;
	    }
	}

	return true;
    }

}










