Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

---------------------------------------
| 1-Wire Memory Dump Utility (OWDump) |
---------------------------------------

I. Description
=------------=
Dump the memory contents of a all of the of 1-Wire devices on the
default 1-Wire network in three selectable formats (raw, pages, packets).
The 1-Wire Network is by default TINIExternalAdapter/serial1
on TINI.


II. Files
=------=
buildOWDump.bat 
\src\OWDump.java 

III. Instructions
=--------------=
1. Load OWDump from the examples\OWDump\bin directory
   using FTP. First open FTP and execute the following commands (for the sake
   of this discussion assume you chose 180.0.42.43 for your IP address):

       open 180.0.42.43
       root
       tini
       bin
       put OWDump.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.02 Pre-Release 2)

    TINI login: root
    TINI password:
    TINI />   

3. To get command line systax execute OWDump without any parameter
   'java OWDump.tini'.  You will see the following output:

     OneWire Memory Dump console application: Version 0.01
     Arch: TINI,  OS Name: slush,  OS Version: TINI OS (pre-release 2) 1.02

     syntax: OWDump ('r' 'p' 'k') <TIME_TEST>
        Dump an iButton/1-Wire Device contents
        'r' 'p' 'k'  - required flag: (Raw,Page,pacKet) type dump
        <TIME_TEST>  - optional flag if present will time each read
                      of memory banks and not display the contents

   Raw 'r' mode will display the contents of all memory banks
   present.  Page 'p' and packet 'k' will display attempt to
   read only the memory banks that are read/write and general purpose.

   Here is some sample output.  The Adapter information is displayed
   with "==", the device information with "*" and the memory bank 
   information with "|".  For example 'java OWDump.tini r':

     OneWire Memory Dump console application: Version 0.00
     Arch: TINI,  OS Name: slush,  OS Version:  TINI OS (pre-release 2) 1.02

     =========================================================================
     == Adapter Name: TINIExternalAdapter
     == Adapter Port description: <na>
     == Adapter Version: <na>
     == Adapter support overdrive: true
     == Adapter support hyperdrive: false
     == Adapter support EPROM programming: false
     == Adapter support power: true
     == Adapter support smart power: false
     == Adapter Class Version: 1.01

     *************************************************************************
     * 1-Wire Device Name: DS1996
     * 1-Wire Device Other Names:
     * 1-Wire Device Address: 150000000140D60C
     * 1-Wire Device Max speed: Overdrive
     * 1-Wire Device Description: 65536 bit read/write nonvolatile memory partitioned
      into two-hundred fifty-six pages of 256 bits each.
     ...

IV. Revision History
=-------------------=
0.00 - First release.
0.01 - Changed to non-deprecated PagedMemoryBank.hasExtraInfo()
