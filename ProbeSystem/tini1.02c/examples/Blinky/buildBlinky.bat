@echo off
REM Compile the classfile
javac -bootclasspath ..\..\bin\tiniclasses.jar -d bin src\*.java

REM Run it through TINIConvertor to make it ready to run on TINI
java -classpath ..\..\bin\tini.jar;%classpath% TINIConvertor -f bin\Blinky.class -o bin\Blinky.tini -d ..\..\bin\tini.db


