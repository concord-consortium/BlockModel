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
	javac -target 1.1 -source 1.3 -classpath ../owapi_1_10/lib/OneWireAPI.jar -d classes -O $(CLASSES:%=source/%)

classes:
	-mkdir $@

tini: main
	# this command was previously setup to run inside of source
	javac -target 1.1 -source 1.3 -bootclasspath tini1.02c/bin/tiniclasses.jar \
	-classpath tini1.02c/bin/owapi_dependencies_TINI.jar \
	-d tinibin $(CLASSES:%=source/%)
	# the old command had this switch too:
	# -t /home/scytacki/owire/lib/tini_dependencies/
	# -p /home/scytacki/owire/lib/
	java -cp tini1.02c/bin/tini.jar BuildDependency -f tinibin  \
	-x tini1.02c/bin/owapi_dep.txt \
	-p tini1.02c/bin/owapi_dependencies_TINI.jar \
	-o ProbeSystem.tini -d tini1.02c/bin/tini.db -add OneWireContainer28

clean:
	rm -rf classes/*
