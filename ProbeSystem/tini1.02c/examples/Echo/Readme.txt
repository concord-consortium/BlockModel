Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

----------------------
| Echo Server/Client |
----------------------

I. Description
=------------=
A simple implementation of an echo server and client.  The server runs on
TINI and listens on port 7 for any connections.  Once a client establishes a
connection, the server echoes any data it receives back to the client.


II. Files
=------=
Client\buildEchoClient.bat - Script to build the client.
Client\src\EchoClient.java - Source for the client.
Server\buildEchoServer.bat - Script to build the server.
Server\src\EchoServer.java - Source for the server.

III. Instructions
=--------------=
1. Load EchoServer from the examples\Echo\Server\bin directory
   using FTP. First open FTP and execute the following commands (for the sake
   of this discussion assume you chose 180.0.42.43 for your IP address):

       open 180.0.42.43
       root
       tini
       bin
       put EchoServer.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.0 Beta 2.2)

    TINI login: root
    TINI password:
    TINI />   

3. Execute the EchoServer ON TINI using the following command 
    'java EchoServer.tini'. You should see the following prompt:
    
      Starting EchoServer version 1.0 ...

    Execute the EchoClient ON THE HOST from the 
    examples\Echo\Client\bin directory using the following command:

    'java EchoClient -n 128 -c a -s 180.0.42.43' 
    
    The output should look like this:
    characterToSend: 97
           numBytes: 128
         serverName: 180.0.42.43
           echoData:
    97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97
    ;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;9
    7;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;
    97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97
    ;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;97;

    EchoClient will dump a usage statement if you type invalid data on the
    command line (e.g. java EchoClient xyz). The switches are:
    -n <number of bytes to send>
    -c <character byte to send>
    -s <server name or IP address>


IV. Revision History
=-------------------=
1.0 - First release.
