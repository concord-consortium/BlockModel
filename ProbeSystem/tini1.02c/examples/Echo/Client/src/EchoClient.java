/*
 * EchoClient.java
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
 $Workfile: EchoClient.java $
 $Revision: 4 $
 $Date: 9/13/99 7:19a $
 $Author: Tomc $
 $Modtime: 9/13/99 7:19a $
 $Log: /JavaBIGNetWidget/Applications/Echo/Client/EchoClient.java $
* 
* 4     9/13/99 7:19a Tomc
* 
* 3     9/13/99 7:12a Tomc
* 
* 2     9/13/99 7:00a Tomc
*/

import java.io.*;
import java.net.*;

/** EchoClient sends bytes to an EchoServer one byte at a time and prints out the return byte.
  */
class EchoClient
  extends Thread
{
  static final int ECHO_PORT = 7;
  
  byte    characterToSend;
  int     numBytes;
  String  serverName;

  /** Constructor
    */
  EchoClient(byte characterToSend, int numBytes, String serverName)
  {    
    this.characterToSend  = characterToSend;
    this.serverName       = serverName;
    this.numBytes         = numBytes;
    
    System.out.println("characterToSend: " + characterToSend);
    System.out.println("       numBytes: " + numBytes);
    System.out.println("     serverName: " + serverName);
    System.out.println("       echoData: ");
  }
  /** Send out the bytes and print the return data.
    */
  public void run()
  {    
    Socket s = null;
    
    try
    {
      s = new Socket(InetAddress.getByName(serverName), ECHO_PORT);
      
      InputStream   in  = s.getInputStream();
      OutputStream  out = s.getOutputStream();
      
      byte[] b = new byte[1];
      b[0] = this.characterToSend;

      for(int i = 0; i < numBytes; i++)
      {
        out.write(b);
        in.read(b); 
        System.out.print(b[0] + ";");
      }
    }
    catch (IOException ioe)
    {
      System.out.println(ioe);
      ioe.printStackTrace();
    }
    finally
    {
      try
      {
        s.close();
      }
      catch (IOException e)
      {  
        System.out.println(e);
        e.printStackTrace();
      }
    }
  }
  /** Print usage statement and exit.
    */
  public  static  void  usage()
  {
    System.out.println("EchoClient [-n <numBytesToSend>] [-c <characterToSend>] -s <echoServerName>");
    System.exit(1);
  }
  /** main
    */
  public static void main(String[] args)
  {
    byte    characterToSend = (byte)'a';
    String  serverName      = "nbtest15";
    int     numBytes        = 128;
    
    if(args != null)
    {
      char lastSwitch = 0;
      
      if(args.length % 2 != 0)
            EchoClient.usage();
            
      for(int i = 0; i < args.length; i++)
      {
        if((i%2 == 0))
        {
          if(args[i].length() != 2)
            EchoClient.usage();
          
          if(args[i].charAt(0) != '-')
            EchoClient.usage();
          
          lastSwitch  = args[i].charAt(1);
        }
        else
        {
          switch(lastSwitch)
          {
            case 's':
              serverName = args[i];
            break;
            
            case 'n':
              numBytes  = (new Integer(args[i])).intValue();
            break;
            
            case 'c':
              characterToSend = (byte)((byte)args[i].charAt(0));
            break;
            
            default:
              EchoClient.usage();
            break;
          }
        }
      }
    }

    (new EchoClient(characterToSend, numBytes, serverName)).start();
  }
}
