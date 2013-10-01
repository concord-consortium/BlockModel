Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------
|     CommTester  |
-------------------

I. Description
=------------=

Demonstration of serial to ethernet communication.  

There are two applications necessary for this example to run.  The 
host application sends data out a PCs serial port and receives data on
a socket (4000).  The host application generates random data and sends it
out the serial port, when ethernet data is received it is verified 
against the data sent.


II. Files
=------=

CommTester.java

TINI application that reads from a serial port and sends all received data 
out a socket connection.

Serial.java

Main application file for the PC.  This file should be executed by typing
java serialtoethernet.Serial <serial port name>
serial port name defaults to COM1 if it is not given on the command
line.

SerialFrame.java

Application frame that sends data out the serial port and receives the data
on the socket.  It defaults to port 4000 for the socket connection.  

CheckData.java 

This class verifies that the data received on the socket matches the data
sent out the serial port.

SerialFrame_AboutBox

Displays an about box.



II. Instructions
=--------------=

To set up your TINI for use with this program, first set the 
IP address of your TINI with the ipconfig command if you have not 
already done so.  Now ftp the CommTester.tini file into the root
directory of TINI.  Close JavaKit.

You should now run the host application.
java serialtoethernet.Serial <port name> 

This should bring up an application and display "Waiting for Connection".
This will also reboot your TINI.  You have to wait for your TINI to respond
to pings before moving on to the next step.


Next, open a telnet session to TINI, log in with the TINI name and password, 
and type:
downserver -s

then type:
java CommTester.tini <IP ADDR OF HOST>

where IP_ADDR_OF_HOST is the IP address of the host computer you are running 
from. 

Your host application should display a message "Got a connection".  The Auto
Send button should also become active.  Click on the Auto Send button.  This
begins the process of sending data out the serial port.  All data should be
received through the socket connection.  If any data is received incorrectly,
you will see a message "dropped a byte".  The Num Sent count and the Num recv
count should increment sequentially.

In the event that a byte is dropped, verify the serial connections to TINI and
also verify network connectivity.


IV. Revision History
=-------------------=
1.0 - first release
