BuildDependency_README.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

-----------------------------------------------
Dallas Semiconductor TINI Program Building Tool
BuildDependency 1.00, December 13, 2000
-----------------------------------------------

I. Using BuildDependency
------------------------

BuildDependency replaces and extends the functionality of the 
BuildOneWireProgram.  It now allows for all kinds of programs to be built, 
with any user-specified list of dependent files built into the 
TINI application.  An old command line of:

  java BuildOneWireProgram -f ReadClock.class -o clock.tini -d tini.db 04 21

Is replaced with a command line of:

  java BuildDependency -f ReadClock.class -o clock.tini -d tini.db -add OneWireContainer04;OneWireContainer21

'OneWireContainer04' and 'OneWireContainer21' are dependency groups--the 
class files they depend on to run are stored in the program BuildDependency.  
They can also be stored in a dependency file (see the section Dependency File 
Creation).  The same command line using a stand-alone dependency file is:

  java BuildDependency -f ReadClock.class -o clock.tini -d tini.db -add OneWireContainer04;OneWireContainer21 -x owapi_dep.txt

The classes referred to in the dependency groups 'OneWireContainer04' and 
'OneWireContainer21' can be loaded from multiple locations.  The can be 
loaded from jar files, other directories, or the current directory.  Note 
that where the dependency group for 'OneWireContainer04' contains an entry 
'com.dalsemi.onewire.container.ClockContainer', BuildDependency will look for 
'com/dalsemi/onewire/container/ClockContainer.class'.  If the class is not 
found in the provided locations, BuildDependency will look in current 
directory. To tell BuildDependency to look in other places, use the 
command line:

  java BuildDependency -f ReadClock.class -o clock.tini -d tini.db -add OneWireContainer04;OneWireContainer21 -p onewire.jar;c:\containers

In this case, BuildDependency will look for dependency classes first in 
onewire.jar, then in the directory c:\containers, then in the current 
directory.  Therefore, if it is looking for 
com.dalsemi.onewire.container.ClockContainer first, it is looking for:

  1. Entry com/dalsemi/onewire/container/ClockContainer.class stored in 
     onewire.jar.
  2. The file c:\containers\com\dalsemi\onewire\container\ClockContainer.class.
  3. The file .\com\dalsemi\onewire\container\ClockContainer.class.

BuildDependency uses the first instance it finds.

If the build fails or to see more of what BuildDependency is doing, add the 
'-debug' switch.

  java BuildDependency -f ReadClock.class -o clock.tini -d tini.db -add OneWireContainer04;OneWireContainer21 -debug

To see a list of all the dependencies, use:

  (to use defaults)
  java BuildDependency -dep

  (to use from a dependency file)
  java BuildDependency -adp -x owapi_dep.txt

To see a list of the dependencies for one dependency group, use (note there is 
no space between -dep and OneWireContainer04):

  java BuildDependency -depOneWireContainer04

BuildDependency will pass any unrecognized arguments to TINIConvertor.  The 
only arguments recognized by BuildDependency are:

  -add NAMES  -- The names of the dependencies to add to this
                 project. This is a semi-colon separated list
                 of dependency names.
  -p PATH     -- Path to your dependency classes.  This is a
                 semi-colon separated list that can include
                 multiple jar files and directories.
  -x DEP_FILE -- Filename of the dependency text database file.
                 BuildDependency has a default set of dependencies
                 used for 1-Wire programs.  See the readme for more
                 on this file's format.
  -debug      -- See the entire output of TINIConvertor
  -dep        -- See the entire dependency list using the specified
                 depdendency file or the default.
  -depNAME    -- See the depdendency list for dependency named NAME.

You can therefore use -f, -o, and -d just as they are used with TINIConvertor.
Note that using '-f onewire.jar' will include the entire jar file in the 
application, while using '-p onewire.jar -add OneWireContainer04' will only 
add the classes necessary for OneWireContainer04 from the jar file.

II. Dependency File Creation
----------------------------

The basic format for defining a dependency group is:

GROUPNAME=DEP_CLASS_1;DEP_CLASS_2;DEP_CLASS_3;DEP_CLASS_4;DEP_CLASS_5;DEP_CLASS_6

So to define 'OneWireContainer04', the full line would be:

OneWireContainer04=com.dalsemi.onewire.container.OneWireContainer04;com.dalsemi.onewire.utils.Bit;com.dalsemi.onewire.container.ClockContainer;com.dalsemi.onewire.container.MemoryBank;com.dalsemi.onewire.container.MemoryBankNV;com.dalsemi.onewire.container.MemoryBankScratch;com.dalsemi.onewire.container.OneWireSensor;com.dalsemi.onewire.container.PagedMemoryBank;com.dalsemi.onewire.container.ScratchPad

However, groups can also act like properties.

CONT_ROOT=com.dalsemi.onewire.container
UTILS_ROOT=com.dalsemi.onewire.utils
OneWireContainer04=%CONT_ROOT%.OneWireContainer04;%UTILS_ROOT%.Bit;%CONT_ROOT%.ClockContainer;%CONT_ROOT%.MemoryBank;%CONT_ROOT%.MemoryBankNV;%CONT_ROOT%.MemoryBankScratch;%CONT_ROOT%.OneWireSensor;%CONT_ROOT%.PagedMemoryBank;%CONT_ROOT%.ScratchPad

These can also be nested.  Say we want to create a group that will build in 
containers for all the parts that implement the ClockContainer interface 
(family codes 04, 21, and 26):

Clocks=%OneWireContainer04%;%OneWireContainer21%;%OneWireContainer26%

However, this leaves open the possibility for an infinite loop to occur.  As a 
trivial example:

Timepieces=%Watches%
Watches=%Timepieces%

To guard against this, BuildDependency has a watchdog timer that kills the 
program if a dependency line cannot be resolved in 5 seconds.

Note that this tool is not limited to 1-Wire use only.  It can be used for any 
kind of dependency-based building of applications.
