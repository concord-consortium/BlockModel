Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------------------------
| CAN Bus Transmit/Receive Example  |
-------------------------------------

I. Description
=------------=
A simple test of CAN Bus transmit/receive with two TINI's.

II. Files
=------=
buildcantest.bat - Script to build the applications.
src\cantransmit.java - Source for the transmit application.
src\canreceive.java - Source for the receive application.

III. Instructions
=--------------=
1. Load cantransmit.tini from the examples\cantest\bin
   directory using FTP. First open FTP and execute the following commands
   (for the sake of this discussion assume you chose 180.0.42.43 for your
   IP address):

       open 180.0.42.43
       root
       tini
       bin
       put cantransmit.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

  Do the above with canreceive.tini on the second TINI board.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.0 Beta 2.2)

    TINI login: root
    TINI password:
    TINI />   

3. Make sure both TINI boards are on the CAN bus.
   Execute the application ON TINI using the following command
   'java cantransmit.tini' or 'java canreceive.tini'.
    
IV. Revision History
=-------------------=
1.0 - First release.
