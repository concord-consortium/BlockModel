Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------------------------
| CAN Bus Autobaud Example          |
-------------------------------------

I. Description
=------------=
Demonstrates how to use the CAN Bus controllers on TINI
to autobaud to CAN traffic.

II. Files
=------=
buildcanautobaud.bat - Script to build the application.
src\canautobaud.java - Source for the application.

III. Instructions
=--------------=
1. Load canautobaud.tini from the examples\canautobaud\bin
   directory using FTP. First open FTP and execute the following commands
   (for the sake of this discussion assume you chose 180.0.42.43 for your
   IP address):

       open 180.0.42.43
       root
       tini
       bin
       put canautobaud.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.0 Beta 2.2)

    TINI login: root
    TINI password:
    TINI />   

3. Execute the application ON TINI using the following command 
   'java canautobaud.tini'.
    
IV. Revision History
=-------------------=
1.0 - First release.
