Readme.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-------------------
| Memory Reporter |
-------------------

I. Description
=------------=
Shows the memory statistics of TINI in a graphical way.

Note:  To use this application the optional PollMemory command must be built
into Slush.  See Slush.txt in the docs directory for details on adding commands
and rebuilding Slush.

II. Files
=------=
buildMemDisplay.bat - Builds the application.
src\MemDisplay.java - Sets up the window, and establishes the connection to TINI.
src\ScrollingGraph.java	- A generic graph, that gives a real-time display of data.

III. Instructions
=--------------=
On TINI, run the pollmemory command.  The launch MemDisplay from the bin directory
with the following command line (where xx.xx.xx.xx is the ip or hostname of your TINI).

java MemDisplay -a xx.xx.xx.xx

IV. Revision History
=-------------------=
2.0 - Repaint/Flicker problem fixed.
1.0 - First release.
