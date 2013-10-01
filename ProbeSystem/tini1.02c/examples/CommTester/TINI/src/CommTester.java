/*
 * Copyright (C) 1996, 97, 98 Dallas Semiconductor Corporation.
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


package CommTester;

import javax.comm.*;
import java.io.*;
import java.net.*;
import com.dalsemi.tininet.*;



public class CommTester
{
   // IP Address of machine that TINI is going to send data to.
   // THIS SHOULD BE CHANGED TO MATCH YOUR TEST MACHINE IP
   String hostIPAddress;

   // Default port number for Socket connection.
   int portNumber = 4000;


   public CommTester(String ip)
   {
      hostIPAddress = ip;
   }

   public static void main(String[] args)
   {
      if (args.length == 0)
      {
         System.out.println("USUAGE: java CommTester <IP ADDR OF HOST>");
         System.exit(0);
      }


      CommTester ct = new CommTester(args[0]);
      ct.run();
   }

   public void run()
   {
      try
      {
         /*
         // -- COMMENT OUT THIS SECTION IF RUNNING FROM SLUSH
         // My ip address
         byte[] ip = {(byte)180, (byte)0, (byte)54, (byte)84};
         // My subnet mask
         byte[] subnet = {(byte)255, (byte)255, (byte)0, (byte)0};

         // Initialize the network on TINI
         TININet.setSubnetMask(subnet);
         TININet.setIPAddress(ip);

         // -- END COMMENT SECTION
         */

         // Begin code for SLUSH

         // In the event that you want to run on different serial ports, use the following lines
         // as needed.
         /*
         com.dalsemi.system.TINIOS.enableSerialPort1();
         com.dalsemi.system.TINIOS.setExternalSerialPortEnable(2, true);
         com.dalsemi.system.TINIOS.setExternalSerialPortEnable(3, true);
         */

         CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier("serial0");
         SerialPort sp = (SerialPort)portId.open("testApp", 0);

         sp.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);


         /* this helps TINI service heavy data better.  These numbers can be optimized to
            a specific application.  The use of a receive threshold is very useful when receiving
            large amounts of data */
         sp.enableReceiveThreshold(1024);
         sp.enableReceiveTimeout(1000);

         // Get our input stream for the serial port
         InputStream in = sp.getInputStream();


         // total number of bytes read.
         int bytesRead = 0;
         int num = 0;


         // Connect to the host machine for testing.
         Socket client = new Socket(hostIPAddress, portNumber);
         // Get an output stream for writing the received serial data.
         OutputStream out = client.getOutputStream();

         // Array to be used for data read.
         byte[] data = new byte[1024];

         while (true)
         {
            // read from the serial port.
            num = in.read(data);
            // if we got data then write it out the socket.
            if (num > 0)
            {
               out.write(data, 0, num);

            }
        }
    }
    catch(Exception e)
    {
         System.out.println("GOT AN EXCEPTION");
         System.out.println(e.toString());

         com.dalsemi.system.Debug.debugDump("Exception");
         com.dalsemi.system.Debug.debugDump(e.toString());
    }

  }
}
