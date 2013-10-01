@echo off
javac -bootclasspath ..\..\bin\tiniclasses.jar -classpath ..\..\bin\owapi_dependencies_TINI.jar;%classpath% -d bin src\*.java
java -classpath ..\..\bin\tini.jar;%classpath% BuildDependency -x ..\..\bin\owapi_dep.txt -p ..\..\bin\owapi_dependencies_TINI.jar -f bin -o bin\OWDump.tini -d ..\..\bin\tini.db -add AllMemoryDevices


