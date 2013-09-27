# MW_CLASS
# EXT_CLASSES
# EXT_FILES
# JAR_EXTRAS
# JAR_NAME

WABA_DIR= /home/scytacki/waba/superwaba/vmsrc/classes_java
WEXTRAS_DIR= /home/scytacki/waba/superwaba/wextras/classes
CLASSES_DIR= classes
BIN_DIR= bin
JAR_NAME= $(MW_CLASS).jar

export CLASSPATH := ${WABA_DIR}:${WEXTRAS_DIR}:${CLASSES_DIR}

CLASSES= *.java

CLIENT_IPS= $(foreach num, $(shell cat clients), 4.19.234.$(shell echo $$(($(num) + 130)))) 
FTP_COMMAND= ncftpput -m $(IP_ADDRESS) '/program files/waba' $(BIN_DIR)/$(MW_CLASS).wrp $(BIN_DIR)/$(MW_CLASS).lnk; \
ncftpput -m $(IP_ADDRESS) '/windows/start menu' $(BIN_DIR)/$(MW_CLASS).lnk



first: all

main: $(CLASSES_DIR)
ifneq ($(EXT_CLASSES),)
	cp  $(EXT_CLASSES) $(CLASSES_DIR)
endif
	javac -d $(CLASSES_DIR) -O $(CLASSES:%=source/%)
ifneq ($(EXT_FILES),)
	cp $(EXT_FILES) $(CLASSES_DIR)
endif

$(CLASSES_DIR) $(BIN_DIR):
	-mkdir $@

warp: $(BIN_DIR) main
	cd $(CLASSES_DIR); java wababin.Warp c ../$(BIN_DIR)/$(MW_CLASS) $(MW_CLASS).class

exegen: 
	cd $(CLASSES_DIR); /home/scytacki/waba/sdk/exegen /l $(EXE_CLASS_HEAP_SIZE) /m $(EXE_OBJ_HEAP_SIZE) /s 5000 /t 50 $(MW_CLASS) $(MW_CLASS) $(MW_CLASS); mv $(MW_CLASS).lnk ../bin


jar: $(BIN_DIR) main
	cd $(CLASSES_DIR); jar c0vf ../$(BIN_DIR)/$(MW_CLASS).jar $(EXT_FILES) *.class -C $(WABA_DIR) waba $(JAR_EXTRAS)

ftp: get-clients
	@echo $(shell cat clients)
	$(foreach IP_ADDRESS, $(CLIENT_IPS), $(FTP_COMMAND);)

get-clients:
	@echo "Enter the ipaq numbers:"
	@read; echo $$REPLY > clients

run:
	cd $(CLASSES_DIR); java waba.applet.Applet $(MW_CLASS)

clean:
	rm -rf $(CLASSES_DIR)/*
	rm -rf $(BIN_DIR)/*

include ../ReleaseSystem/Makefile.inc
