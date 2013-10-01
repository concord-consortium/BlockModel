
/**
 * Title:        SerialToEthernet<p>
 * Description:  <p>
 * Copyright:    Copyright (c) cw<p>
 * Company:      Dallas Semiconductor<p>
 * @author cw
 * @version 1.0
 */
package serialtoethernet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import javax.comm.*;
import java.util.*;

public class SerialFrame extends JFrame
{


  public static final int BUFFER_SIZE = 4000;
  public static final int DEFAULT_SOCKET_PORT = 4000;

  static CommPortIdentifier portId;
  static Enumeration        portList;

  CheckData cd;
  int numSent = 0;

  boolean wait = true;
  InputStream      inputStream;
  public DataOutputStream out;
  SerialPort       serialPort;


  PipedOutputStream pipedout;

  byte[] gData;
  boolean value = false;
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  JTextArea jTextArea1 = new JTextArea();
  Component component1;
  JPanel jPanel2 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  Box box1;
  JPanel jPanel1 = new JPanel();
  JPanel jPanel3 = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel jPanel4 = new JPanel();
  JPanel jPanel6 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  String devPortName = "COM1";

  //Construct the frame
  public SerialFrame(String portName)
  {
    if (portName != null)
    {
      devPortName = portName;
    }
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }



  //Component initialization
  private void jbInit() throws Exception
  {
    contentPane = (JPanel) this.getContentPane();
    component1 = Box.createGlue();
    box1 = Box.createVerticalBox();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 300));
    this.setTitle("Serial To Ethernet Tester");
    jTextArea1.setAlignmentY((float) 5.0);
    jTextArea1.setAlignmentX((float) 1.0);
    jTextArea1.setBorder(BorderFactory.createEtchedBorder());
    jTextArea1.setText("jTextArea1");
    jPanel2.setLayout(borderLayout2);
    jPanel1.setLayout(borderLayout3);
    jPanel6.setLayout(flowLayout1);



      jPanel7.setLayout(borderLayout4);
      jPanel7.setMinimumSize(new Dimension(100, 50));
      jPanel7.setPreferredSize(new Dimension(100, 50));
      jPanel8.setMinimumSize(new Dimension(100, 50));
      jPanel8.setPreferredSize(new Dimension(100, 50));
      jPanel8.setLayout(borderLayout5);
      jLabel1.setText("Num Sent");
      jLabel2.setText("Num recv");
      contentPane.setMinimumSize(new Dimension(337, 300));
      contentPane.setPreferredSize(new Dimension(337, 300));
    jAutoSendButton.setText("Auto Send");
      jAutoSendButton.addActionListener(new java.awt.event.ActionListener()
      {

         public void actionPerformed(ActionEvent e)
         {
            jAutoSendButton_actionPerformed(e);
         }
      });
    jPanel9.setMinimumSize(new Dimension(100, 20));
    jPanel9.setPreferredSize(new Dimension(100, 20));
    jScrollPane1.setAutoscrolls(true);
    jTextArea.setToolTipText("");
    contentPane.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jPanel4, BorderLayout.EAST);
      jPanel1.add(jPanel3, BorderLayout.NORTH);
      jPanel3.add(jAutoSendButton, null);
    jPanel1.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea, null);
      contentPane.add(jPanel5, BorderLayout.WEST);
      contentPane.add(jPanel6, BorderLayout.SOUTH);
      jPanel6.add(jPanel8, null);
      jPanel8.add(jNumSent, BorderLayout.CENTER);
      jPanel8.add(jLabel1, BorderLayout.NORTH);
    jPanel6.add(jPanel9, null);
      jPanel6.add(jPanel7, null);
      jPanel7.add(jNumRecv, BorderLayout.CENTER);
      jPanel7.add(jLabel2, BorderLayout.NORTH);

    jNumSent.setText("0");
    jNumRecv.setText("0");
    jTextArea.setText("Waiting for connection\n");
    setSerial();
    jAutoSendButton.setEnabled(false);
    waitForConnection();
  }

  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      System.exit(0);
    }
  }

  Thread thread;
  boolean testRunning = false;
   JScrollPane jScrollPane2 = new JScrollPane();
   JScrollPane jScrollPane3 = new JScrollPane();
   JPanel jPanel5 = new JPanel();
   JPanel jPanel7 = new JPanel();
   JPanel jPanel8 = new JPanel();
   JTextField jNumRecv = new JTextField();
   BorderLayout borderLayout4 = new BorderLayout();
   JTextField jNumSent = new JTextField();
   BorderLayout borderLayout5 = new BorderLayout();
   JLabel jLabel1 = new JLabel();
   JLabel jLabel2 = new JLabel();
   JToggleButton jAutoSendButton = new JToggleButton();

  public void appendText(String temp)
  {
     jTextArea.append(temp);
  }

  public void waitForConnection()
  {
    // start the thread that waits for the network connection.
    thread = new Thread(new MyTestThread());
    thread.start();
  }
  public void setSerial()
  {
      portList = CommPortIdentifier.getPortIdentifiers();
      while (portList.hasMoreElements())
      {
         portId = (CommPortIdentifier) portList.nextElement();
         if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
         {
            if (portId.getName().equals(devPortName))
            {
               break;
            }
         }
      }

      try
      {
         serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
      }
      catch (PortInUseException e)
      {
         System.out.println("GOT a portinuseexception");

      }
      try
      {
         out = new DataOutputStream(serialPort.getOutputStream());
      }
      catch (IOException e) {
         System.out.println("GOT an IOException + " + e);
      }


      try
      {
         serialPort.setSerialPortParams(115200,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);
         serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
         serialPort.setDTR(false);
      }
      catch (UnsupportedCommOperationException e) {}


  }

  class MyTestThread implements Runnable
  {
    boolean doRun = true;
    ServerSocket serverSocket;
    Socket clientSocket;

    int index = 0;
    int count = 0;
    int numRecv = 0;
    public void run()
    {
         try
         {

            serverSocket = new ServerSocket(DEFAULT_SOCKET_PORT);
            clientSocket = serverSocket.accept();
            appendText("Got the Connection");

            // Turn on the auto send button.
            jAutoSendButton.setEnabled(true);

            InputStream is = clientSocket.getInputStream();
            byte[] b = new byte[BUFFER_SIZE];
            int numBytes = 0;
            pipedout = new PipedOutputStream();
            cd = new CheckData(pipedout);
            cd.setTextArea(jTextArea);
            cd.start();
            while (true)
            {
               numBytes = is.read(b);
               index += numBytes;
               numRecv += numBytes;
               jNumRecv.setText(new Integer(numRecv).toString());
               pipedout.write(b,0,numBytes);
            }
         }
         catch (Exception e)
         {
            System.out.println("Exception " + e.toString());
            e.printStackTrace();
         }
    }

    public void stop()
    {
      System.out.println("Stopping the test");
      doRun = false;
    }
  }


  Thread t = null;
  JPanel jPanel9 = new JPanel();
  JScrollPane jScrollPane1 = new JScrollPane();
  JTextArea jTextArea = new JTextArea();
  DoSend doSend;

   void jAutoSendButton_actionPerformed(ActionEvent e)
   {
      value = !value;
      if (value)
      {
        doSend = new DoSend();
        t = new Thread(doSend);
        cd.setIndex(0);
        t.start();
      }
      else
      {
        doSend.stopTest();
        try
        {
          Thread.sleep(1100);
        }
        catch (InterruptedException ex)
        {}
        jAutoSendButton.setText("Auto Send");
      }
   }

   class DoSend implements Runnable
   {
      boolean doTest = true;

      public void run()
      {

        jAutoSendButton.setText("Stop Send");
        Random rand = new Random();
        byte[] temp = new byte[BUFFER_SIZE];
        gData = new byte[temp.length];

        for (int i = 0; i < temp.length; i++)
        {
           temp[i] = (byte)rand.nextInt(255);
        }
        wait = true;
        System.arraycopy(temp, 0, gData, 0, temp.length);
        cd.setCompareData(gData);


        String tempStr = new String("");
        for (int i = 0; i < gData.length; i++)
        {
           tempStr += Integer.toHexString(gData[i] & 0xFF);
        }

        while (doTest)
        {
            try
            {
               out.write(temp);
               numSent += temp.length;
               jNumSent.setText(new Integer(numSent).toString());
            }
            catch (Exception ex)
            {
               System.out.println("GOT AN EXCEPTION IN AUTOSEND");
            }
        }

      }

      public void stopTest()
      {
        doTest =false;
      }
   }

}