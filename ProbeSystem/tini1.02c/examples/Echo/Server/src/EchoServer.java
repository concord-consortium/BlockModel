/*
 * EchoServer.java
 */
 
/* Copyright (C) 1999 Dallas Semiconductor Corporation.
 * All rights Reserved. Printed in U.S.A.
 * This software is protected by copyright laws of
 * the United States and of foreign countries.
 * This material may also be protected by patent laws of the United States
 * and of foreign countries.
 * This software is furnished under a license agreement and/or a
 * nondisclosure agreement and may only be used or copied in accordance
 * with the terms of those agreements.
 * The mere transfer of this software does not imply any licenses
 * of trade secrets, proprietary technology, copyrights, patents,
 * trademarks, maskwork rights, or any other form of intellectual
 * property whatsoever. Dallas Semiconductor retains all ownership rights.
 */
/* 
 $Workfile: EchoServer.java $
 $Revision: 5 $
 $Date: 9/13/99 2:04p $
 $Author: Tomc $
 $Modtime: 9/13/99 11:09a $
 $Log: /JavaBIGNetWidget/Applications/Echo/Server/EchoServer.java $
* 
* 5     9/13/99 2:04p Tomc
* 
* 4     9/13/99 7:33a Tomc
* 
* 2     9/13/99 7:20a Tomc
*/

import java.io.*;
import java.net.*;

/** EchoServer waits for a client connection and echoes data received on the echo port.
  */
class EchoServer 
  extends Thread
{
  static final int ECHO_PORT    = 7;
  
  Socket s;

/** Constructor 
  */
  EchoServer(Socket s)
    throws IOException
  {
    //System.out.println("New client connection");
    this.s = s; 
  }
/** Echo received data until connection is closed by client.
  */
  public void run()
  {
    //System.out.println("New thread for socket s:" + s);
    
    InputStream  in   = null;
    OutputStream out  = null;
    
    try
    {
      in  = s.getInputStream();
      out = s.getOutputStream();
  
      byte[] b = new byte[16];
   
      int c;
  
      while ((c = in.read(b)) > 0)
        out.write(b, 0, c);
  
      //System.out.println("EOF");
    }
    catch (IOException ioe)
    {
      //System.out.println(ioe);
    }
    finally
    {
      try
      {
        //System.out.println("Closing");
        in.close();
        out.close();
        s.close();
      }
      catch (IOException i)
      {
        //System.out.println(i);
      }
    }
  }
/** main - Opens a server socket and spins off a new thread each time 
  *        a new client connection is accepted on this socket.
  */
  public static void main(String[] args)
  {    
    System.out.println("Starting EchoServer version 1.0 ...");
    
    ServerSocket serverSocket = null;
    
    try
    {
      serverSocket = new ServerSocket(ECHO_PORT);
  
      while(true)
        (new EchoServer(serverSocket.accept())).start();
    }
    catch(Exception e){}
    finally
    {
      try
      {
        serverSocket.close();
      }
      catch (IOException i)
      {
        //System.out.println(i);
      }
    }
  }
}
