Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

------------------
| NativeExample1 |
------------------

NOTE: The batch file now embeds example1.tlib into Example1.tini. Only
      Example1.tini needs to be FTP-ed to TINI. Alternately the batch
      file could be modified slightly to generate a .tbin file and the
      application, with embedded tlib, could be loaded into bank 7 using 
      JavaKit. The application would need to be modified to call 
      Debug.setDefaultStreams() in order to view the output (bank 7 
      application only).

I. Description
=------------=
  This example demonstrates the usage of native libraries. Specifically
  how to manage Java parameters and return values and how to manage
  state buffers. This example has four native methods "method1",
  "method2", "method3" and "method4". method1 fills the byte array parameter 
  with 0x55, stores the long parameter in its state buffer and returns the 
  int parameter. method2 reads the long parameter, stored in method1, from 
  its state buffer and returns it. method3 reads a number stored in indirects, 
  increments it, stores the new value and returns the incremented number.
  method4 sleeps for the requested number of milliseconds and returns the
  requested sleep time. The native initialization routine, called on 
  loadLibrary, detects if state buffers exist. If not it mallocs new buffers 
  and inserts them into system state tables. It also acquires and inits 
  "indirects" and registers a process destroy function. The process destroy 
  function unregisters itself and releases "indirects".

II. Files
=------=
  -Java-
  Example1.java
  -Native-
  example1.a51
  tini.inc
  tinimacro.inc
  ds80c390.inc
  apiequ.inc

III. Instructions
=--------------=
  FTP Example1.tini to TINI.
  Execute "java Example1.tini" at the slush prompt.

IV. Revision History
=-------------------=
  beta3.0 - Created.
  1.02    - Added indirect support, process destroy support, changed  
            NatLib_LoadPointer calls to NatLib_LoadJavaByteArray calls and
            added native sleep example.
            Batch file now embeds the .tlib file in the application.
  1.02c   - Fixed native method1 bug. Loop register fixups were calculated
            incorrectly. Changed "orl a, R1(2)" to "mov a, R1(2)".

