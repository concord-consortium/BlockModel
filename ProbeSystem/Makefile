# To compile this you need a java compiler
# and you need to have the tini.jar archive
# and the OneWireAPI.jar achive one your
# classpath.  (or in jdk/jre/lib/ext)
#


CLASSES= ProbeSystem.java ProbeManager.java ManagerWorker.java \
	ProbeClient.java ProbeCmd.java ProbeInfo.java ProbeState.java \
	CmdErrorException.java JavaProbeManager.java TCPDataChannel.java \
	TBProbeManager.java

main: classes
	javac -d classes -O $(CLASSES:%=source/%)

classes:
	-mkdir $@

tini: main
	javac -bootclasspath /home/scytacki/tini1.01/bin/tiniclasses.jar \
-d ../classes/tinibin $(CLASSES)
	java BuildOneWireProgram tinibin/*.class -p /home/scytacki/owire/lib/ \
-o ProbeSystem.tini -d /home/scytacki/tini1.01/bin/tini.db -t /home/scytacki/owire/lib/tini_dependencies/ 28

clean:
	rm -rf classes/*
