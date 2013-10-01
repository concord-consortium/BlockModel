@echo off
cd lib

copy ..\..\..\native\lib\apiequ.inc .
copy ..\..\..\native\lib\tini.inc .
copy ..\..\..\native\lib\tinimacro.inc .
copy ..\..\..\native\lib\ds80c390.inc .

..\..\..\native\bin\win32\macro example1.a51
..\..\..\native\bin\win32\a390 -l example1.mpp
del *.inc
copy example1.tlib ..\bin
del example1.tlib
cd ..
javac -bootclasspath ..\..\bin\tiniclasses.jar -d bin src\*.java
java -classpath ..\..\bin\tini.jar;%classpath% TINIConvertor -n bin\example1.tlib -f bin -o bin\Example1.tini -d ..\..\bin\tini.db

