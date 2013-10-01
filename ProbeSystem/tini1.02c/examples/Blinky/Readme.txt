Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------------------------
| Blinky Example                    |
-------------------------------------

I. Description
=------------=
A simple program that blinks the D1 LED on the TINI Board.

II. Files
=------=
buildBlinky.bat - Script to build the applications.
src\Blinky.java - Source for the Blinky application.

III. Instructions
=--------------=
1. Load Blinky.tini from the examples\Blinky\bin
   directory using FTP. First open FTP and execute the following commands
   (for the sake of this discussion assume you chose 180.0.42.43 for your
   IP address):

       open 180.0.42.43
       root
       tini
       bin
       put Blinky.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.02B)

    TINI login: root
    TINI password:
    TINI />   

3.  Execute the application ON TINI using the following command
   'java Blinky.tini' 
    
IV. Revision History
=-------------------=
1.02b - First release.
