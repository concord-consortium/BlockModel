Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------------------
| 1-Wire device List (ListOW) |
-------------------------------

I. Description
=------------=
Display the 1-Wire Network address's of the device connected to the
default 1-Wire Network.  This example represents a minimal example
to doing 1-Wire.  The 1-Wire Network is by default 
TINIExternalAdapter/serial1 on TINI.

II. Files
=------=
buildListOW.bat 
\src\ListOW.java 

III. Instructions
=--------------=
1. Load ListOW from the examples\ListOW\bin directory
   using FTP. First open FTP and execute the following commands (for the sake
   of this discussion assume you chose 180.0.42.43 for your IP address):

       open 180.0.42.43
       root
       tini
       bin
       put ListOW.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.0 Beta 2.2)

    TINI login: root
    TINI password:
    TINI />   

3. Execute the application with the 'java ListOW.tini'.  Here is some sample
   output:

    Adapter: TINIExternalAdapter Port: serial1

    B700000018AE3212
    B200000018BC2A12
    FC00000018BC0112
    1400000018AE2912

   Note the default 1-Wire Network is by default the TINIExternalAdapter/serial1
   on TINI.

IV. Revision History
=-------------------=
0.00 - First release.
