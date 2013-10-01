@echo off
javac -bootclasspath ..\..\bin\tiniclasses.jar -d bin src\*.java
java -classpath ..\..\bin\tini.jar;%classpath% TINIConvertor -f bin\canautobaud.class -o bin\canautobaud.tini -d ..\..\bin\tini.db


