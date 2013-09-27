
MODULE = BlockModel
DEP_NAMES = wgraph ProbeSystem

PM_CLASSES= JavaProbeManager.class ProbeManager.class ProbeInfo.class
EXT_CLASSES= $(PM_CLASSES:%=$(PROBESYS_DIR)/%)
EXT_FILES= source/trash.bmp
MW_CLASS= BlockModel

include ../ReleaseSystem/waba.inc

WGRAPH_DIR:= $(CURDIR)/../wgraph/classes
PROBESYS_DIR:= $(CURDIR)/../ProbeSystem/classes
JAR_EXTRAS= -C $(WGRAPH_DIR) graph -C $(WEXTRAS_DIR) extra/util/Maths.class

CLASSPATH += ${CLASSPATH}:${WGRAPH_DIR}:.


all: dependencies main warp jar
