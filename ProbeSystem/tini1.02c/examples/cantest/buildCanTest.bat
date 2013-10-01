@echo off
javac -bootclasspath ..\..\bin\tiniclasses.jar -d bin src\*.java
java -classpath ..\..\bin\tini.jar;%classpath% TINIConvertor -f bin\cantransmit.class -o bin\cantransmit.tini -d ..\..\bin\tini.db
java -classpath ..\..\bin\tini.jar;%classpath% TINIConvertor -f bin\canreceive.class -o bin\canreceive.tini -d ..\..\bin\tini.db


