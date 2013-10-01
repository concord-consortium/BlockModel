README.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

Message 1 of 4:
============================================================================
IMPORTANT!!! IF YOU HAVE INFORMATION IN TINI'S FILE SYSTEM THAT YOU CANNOT
             AFFORD TO LOSE, READ THIS MESSAGE!!!
----------------------------------------------------------------------------
TINI OS now strictly checks the version bytes in the system area of the 
heap. If you do not clear the heap before performing this upgrade to 1.02,
TINI OS will detect a version mis-match and *WILL CLEAR THE HEAP FOR YOU*. 
If you have any files in TINI's file system that you cannot affort to lose,
FTP them to your host computer before performing the upgrade. You can FTP
them back to TINI after the upgrade has been completed.
============================================================================
Message 2 of 4:
============================================================================
Versions.txt is a new file in the docs directory. If you have problems
upgrading to the latest distribution check this file for versions
of files included with the distribution and compare them with the versions
you are currently using.
============================================================================
Message 3 of 4:
============================================================================
The Native API has been improved and Native Libraries can now be embedded in
application binary files. See Native_API.txt and Native_Methods.txt for 
details.
============================================================================
Message 4 of 4:
============================================================================
THE LOADER IS NO LONGER INCLUDED IN THIS DISTRIBUTION. 
You do not need to update your loader unless you purchased your TINI before
05/2000 AND you are upgrading from an early Beta or Alpha revision of
firmware. You can request a copy of the loader from
mailto:tinisales@dalsemi.com?subject=tini_loader_request
============================================================================

Start of general README document:
-------------------------------------
|  Dallas Semiconductor TINI 1.02   |
-------------------------------------

------------------------------------
-=           Contents             =-
------------------------------------

i.     Introduction
I.     Basic Setup
II.    Loading the Firmware updates
III.   Loading slush
IV.    Loading Files into the TINI Filesystem Using FTP and Telnet
V.     General Information

-=--------------------------------=-


i. Introduction
=-------------=

This download contains the latest firmware for the TINI board, various
utilities for development, and source for several example applications.

  What is TINI?

     TINI is a small, embedded system designed to run Java(tm) applications
     and interface between external hardware and a network.  A quick list
     of some of TINI's features includes:

         o DS80C390 processor
         o Ethernet controller
         o 512k Flash ROM
         o 512k battery backed RAM (expandable to 2MB)
         o Two integrated serial ports and support for two external serial ports
         o Two integerated CAN controllers
         o Internal and external 1-Wire buses
         o Real time clock
         o JDK 1.1 API and firmware providing a multi-tasking, multi-threaded
           TINI OS with a full TCP/IP stack, garbage collection, serial port
           and 1-Wire drivers, PPP support, and much more.
         o Native interface that offers developers low level access to
           hardware using assembly language.
         o Slush, a system shell giving a Unix(tm) like interface with TTY,
           Telnet, and FTP servers.
           

  Requirements:

     To develop for the TINI board, you will need to download the Java
     Development Kit (JDK) from Sun for your development platform.  Although
     the TINI board's API currently conforms to the JDK 1.1 API, we recommend
     using JDK 1.2 for development.  The JDK is freely available from
     http://java.sun.com.

     To load the included firmware updates, you will need a serial port on
     your development PC and serial cable to connect to your TINI board.
     You will also need to download and install an implementation of Sun's
     Java Communications API (a.k.a. Java Serial Port API), javax.comm.  
     RxTx, a port of this API for Linux is available from http://www.rxtx.org.
     Other platforms are available directly from Sun at 
     http://java.sun.com.


  Getting Started:

     You must first load the included firmware file into the flash
     ROM of your TINI board.  You can then choose to load either the operating
     system shell slush or your own user application also into the flash.
     Slush is a Java program providing a Unix like shell for your TINI board.
     It provides most operating system functionality as well as Telnet, FTP,
     and serial (TTY) servers.

     When you develop Java programs to run on TINI, you must run the
     resulting class files through a conversion process using the
     TINIConvertor utility.  The result is a modified class file that
     will be interpreted by TINI's VM.  For help with compiling
     programs, see the file BUILDING_APPLICATIONS.txt.

     You can also choose to write native methods for your Java programs.  These
     live in separate dynamically loaded *.tlib files.  See Native_Methods.txt
     for more information, and Native_API.txt for a list of native API
     functions available to your native libraries.

     For instructions on loading the firmware update file and setting up
     your TINI board for use, read the following sections of this document.


I. Basic Setup:
=-------------=

1.  Make sure your board is properly plugged in - power, serial I/O (attached
    to the serial port of your host computer), and ethernet.  You will use a
    straight through cable (NOT a null modem cable) to connect to the
    default serial port (serial0) on TINI. 
    
    NOTE: TINI uses DTR as a control signal for reset.
    It is known to long term TINI developers as the "Do TINI Reset" signal. 

    TINI uses DTR as follows:
      DTR high - hold TINI system in RESET
      DTR low  - allow system to run normally.

    Many software applications (terminal applications,...) will set DTR high
    which will cause TINI to hold in RESET. If you need to use an application
    that holds DTR high you must disconnect from the serial0 connector on
    your development socket board. Several socket boards have provisions for 
    disconnecting this signal. 
 
    These include:
      Dallas Semiconductor: E10, E20 and E50 have a solder bridge labelled
                            "DTR RESET" which connects DTR to TINI. This 
                            solder bridge can be removed (desoldered and 
                            scraped clean) to disable DTR Reset.
      Systronix:  TILT and STEP have a jumper labelled DTR RESET which can
                  be removed to disable DTR Reset.
      Others:  See http://www.ibutton.com/TINI/marketplace/index.html for
               links.

2.  To load the firmware update, you will use the provided Java program
    JavaKit.  You need to first download and install javax.comm for your
    computer.  See Running_JavaKit.txt for setup instructions.  Once you have
    correctly configured your system to use javax.comm, run JavaKit with:
        java -classpath <TINI Install Dir>\bin\tini.jar;%CLASSPATH% JavaKit
    in Windows, or in a Un*x environment:
        java -classpath <TINI Install Dir>/bin/tini.jar:$CLASSPATH JavaKit

3.  In JavaKit, select the serial port to which you have attached the serial 
    cable from TINI.

4.  Make sure the baud rate is set to 115200.  If you need to connect at 19200,
    edit the <TINI Install Dir>\src\slush\com\dalsemi\slush\Slush.java file,
    change the SERIAL_SPEED variable to 19200 and rebuild. For instructions
    on building an application, see Building_Applications.txt. 
    NOTE: The current loader does not support baud rates below 19200.

5.  Press the "Open Port" button.

6.  Press the "Reset" button.  This will reset the board to the TINI loader.
    NOTE: this can be done at any time to abort slush and all running
    applications and return to the TINI loader.

7.  A prompt should appear similar to:

      TINI Loader  05-15-00 17:45
      Copyright (C) 2000 Dallas Semiconductor. All rights reserved.

      >


II. Loading the Firmware Updates
=------------------------------=

1.  If you are not already at a TINI loader prompt, hit the "Reset" button in
    JavaKit to activate the loader.

2.  Go to the File menu above and select "Load File".  Choose the file
    <TINI Install Dir>\bin\tini.tbin.  JavaKit should report the load in
    progress.  This will take up to several minutes.

3.  Clear the heap.  Keep in mind that this WILL destroy any files you have
    stored on TINI.  You will have to reload them using FTP.  See section IV
    for instructions on transfering files.  To clear the heap do the
    following:
        b18 <RETURN>           //changes to bank 18
        f0  <RETURN>           //fills bank 18 with 0's, effectively erasing it


III. Loading slush:
=-----------------=

1.  If you are not already at a TINI loader prompt, hit the "Reset" button in
    JavaKit to activate the loader.

2.  Go to the File menu above and select "Load File".  Choose the file
    <TINI Install Dir>\bin\slush.tbin.  JavaKit should report the
    load in progress.  This will take several seconds. 

3.  Hit enter to get a prompt.  To begin execution and bring up slush, type:
        e <RETURN>

    NOTE:  You may see a couple of garbage characters printed as control of
           the port is transferred from the loader to the application.
           Finally, the slush boot messages should begin printing, and you
           will be able to log in.

           At this point you might want to ensure that JavaKit is set to
           "Dumb Terminal" mode to disable echo when typing passwords.
    
    You should see output similar to:
 
    [-=        slush Version 1.02         =-]
    [          System coming up.            ]
    [      Beginning initialization...      ]
    [        Not generating log file.       ]    [Info]
    [    Initializing shell commands...     ]    [Done]

    [        Checking system files...       ]    [Done]

    [ Initializing and parsing .startup...  ]
    [        Initializing network...        ]
    [    Network configurations not set.    ]    [Skip]
    [         Network configuration         ]    [Done]
    [         System init routines          ]    [Done]

    [    slush initialization complete.     ]

    [      Bringing up Serial Server...     ]

    Hit any key to login.

    Welcome to slush.  (Version 1.02)

    TINI login: root
    TINI password:
    TINI />

    NOTE: There are two default accounts, "guest" with
          the password "guest" and "root" with the password "tini".

    Now, you can set your IP address using the ipconfig command.
    For instance, to set your IP directly:
    
        ipconfig -f -a 192.168.1.20 -m 255.255.255.0

    Note: You must specify a subnet mask when you set your IP address.
      
    Or, if you have a DHCP server on your network and want to obtain a dynamic
    IP, type:
    
        ipconfig -f -d

    Make sure to consult your network administrator before selecting your IP.

    You can set the hostname of the TINI board with the hostname command,
    and change other network settings with the ipconfig command.
    Type 'help ipconfig' for details.

    Two new switches have been added to ipconfig. 

        ipconfig -C

    will commit the current network configuration to flash memory at the start
    of bank 7. If the configuration changes after it has been committed it
    will be restored on the next bootup.

        ipconfig -D

    will disable boot time restoration.

    Type 'help' for a list of all valid slush commands.

    For more information on slush, see Slush.txt.

    For information on building and flashing another application into bank 7
    in place of slush, see Building_Applications.txt.


IV. Loading Files into the TINI Filesystem Using FTP and Telnet
=-------------------------------------------------------------=

1. Follow directions in part III to load and execute the slush executable in
   bank 7.

2. Set an IP address for slush.  Type 'help ipconfig' for details.

3. Use your usual FTP client program to connect and transfer files to TINI.

4. Use the Serial Server or Telnet to manipulate your files.
   (Copy, delete, make directories, etc.)  Type 'help' from one of these
   sessions to get a list of valid slush commands, and 'help <command>' to get
   help on a particular command.


V. General Information
=--------------------=

  390 Processor:

     The 390's earliest ancestor is an 8051. The main differences in terms of
     performance are 4 clks/machine-cycle instead of 12, faster clock rate (up
     to 40 MHz) a 32 bit math unit and 2 data pointers. The data pointers are 22
     bits as opposed to 16 bit allowing the relatively large amount of RAM we
     support without the nasty firmware overhead of bank switching.  Comparing
     the raw speed of the 390 to an 8051, at 40Mhz it looks like a 100MHz 8051.
     It would look like 120MHz given 4 clks/MC vs. 12 clks/MC but some
     instructions grew a machine cycle so the real speed multiplier is more like
     2.5. The other major differences are related to integrated I/O like the CAN
     controllers, 2 serial ports and more external interrupts.

  CAN Bus Controller:

     The 390 contains two built in CAN Controllers, capable of connecting to
     two separate busses at different bit rates.  Each has 15 message centers,
     with each message center capable of send or receive of 11 or 29 bit ID
     messages.  Masks can be individually set on a per message center basis.

  ROM Banks:

     TINI's 512k ROM is divided among 8 64k banks.  Bank 0 contains the
     bootstrap loader.  Banks 1-6 contain the Java API and TINI firmware. Bank
     7 contains the user application that TINI will execute upon powerup.
     You can choose to put the os shell slush in this bank (providing many
     system utilities and FTP and Telnet servers) or your own user application.

  Garbage Collector:

     The garbage collector runs on behalf of a specific process P if one of the 
     following conditions exist and the garbage collector is currently idle:

     1) Process P calls System.gc().
     2) The total amount of free RAM in the heap goes below 64k when process
        P attempts to allocate memory. 

     The collector frees only the garbage generated by process P. 
     - Invoking gc from the slush command line frees only garbage generated 
       by slush. 
     - Calling System.gc() from a HelloWorld application frees only garbage
       generated by the HelloWorld application.

  More Information:

     See Limitations.txt for a partial list of TINI system limitations.

     TINI Homepage
         http://www.ibutton.com/TINI

     TINI Email Forum
         To subscribe, send an email with "subscribe TINI" in the BODY of the
         message to TINI-request@iButton.com.
         To unsubscribe, send an email with "unsubscribe TINI" in the BODY of
         the message to TINI-request@iButton.com.

