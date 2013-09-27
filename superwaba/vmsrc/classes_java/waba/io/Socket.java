package waba.io;

/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

/**
 * Socket is a TCP/IP network socket.
 * <p>
 * Under Java and Windows CE, if no network is present, the socket
 * constructor may hang for an extended period of time due to the
 * implementation of sockets in the underlying OS. This is a known
 * problem.
 * <p>
 * Here is an example showing data being written and read from a socket:
 *
 * <pre>
 * Socket socket = new Socket("www.yahoo.com", 80);
 * if (!socket.isOpen())
 *   return;
 * byte buf[] = new byte[10];
 * buf[0] = 3;
 * buf[1] = 7;
 * socket.writeBytes(buf, 0, 2);
 * int count = socket.readBytes(buf, 0, 10);
 * if (count == 10)
 *   ...
 * socket.close();
 * </pre>
 */

public class Socket extends Stream
{
boolean _isOpen;
java.net.Socket _socket;
java.io.InputStream _in;
java.io.OutputStream _out;

/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * @param host the host name or IP address to connect to
 * @param port the port number to connect to
 */

public Socket(String host, int port)
	{
	_isOpen = false;
	try
		{
		_socket = new java.net.Socket(host, port);
		_socket.setSoTimeout(1500);
		_in = _socket.getInputStream();
		_out = _socket.getOutputStream();
		_isOpen = true;
		}
	catch (Exception e) { e.printStackTrace(); }

	if (!_isOpen && _socket != null)
		{
		try { _socket.close(); }
		catch (Exception e) {}
		}
	}
/**
 * Closes the socket. Returns true if the operation is successful
 * and false otherwise.
 */

public boolean close()
	{
	if (!_isOpen)
		return false;
	_isOpen = false;
	boolean pass = true;
	try
		{
		_socket.close();
		}
	catch (Exception e) { pass = false; }
	return pass;
	}
/**
 * Returns true if the socket is open and false otherwise. This can
 * be used to check if opening the socket was successful.
 */

public boolean isOpen()
	{
	return _isOpen;
	}
/**
 * Reads bytes from the socket into a byte array. Returns the
 * number of bytes actually read or -1 if the server closed
 * the connection or an error prevented the read operation from
 * occurring.
 * @param buf the byte array to read data into
 * @param start the start position in the byte array
 * @param count the number of bytes to read
 */

public int readBytes(byte buf[], int start, int count)
	{
	if (!_isOpen)
		return -1;
	int nread = -1;
	try
		{
		nread = _in.read(buf, start, count);
		}
	catch (java.io.InterruptedIOException e) { return 0; }
	catch (Exception e) { e.printStackTrace(); }
	return nread;
	}
/**
 * Sets the timeout value for read operations. The value specifies
 * the number of milliseconds to wait from the time of last activity
 * before timing out a read operation. Passing a value of 0 sets
 * no timeout causing any read operation to return immediately with
 * or without data. The default timeout is 1500 milliseconds. This
 * method returns true if successful and false if the value passed
 * is negative or the socket is not open. Calling this method
 * currently has no effect under Win32 or WindowsCE. The
 * read timeout under those platforms will remain the system default.
 * @param millis timeout in milliseconds
 */

public boolean setReadTimeout(int millis)
	{
	if (millis < 0)
		return false;
	if (millis == 0)
		millis = 1; // 0 means infinity in setSoTimeout()
	try
		{
		_socket.setSoTimeout(millis);
		return true;
		}
	catch (Exception e) { e.printStackTrace(); }
	return false;
	}
/**
 * Writes to the socket. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring. If data
 * can't be written to the socket for approximately 2 seconds, the
 * write operation will time out.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */

public int writeBytes(byte buf[], int start, int count)
	{
	if (!_isOpen)
		return -1;
	int nwrite = -1;
	try
		{
		_out.write(buf, start, count);
		nwrite = count;
		}
	catch (Exception e) { e.printStackTrace(); }
	return nwrite;
	}
}