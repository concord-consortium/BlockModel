SRC_DIRS = source/ui source/ui/_plat_ source/util source/io source/event icons source/wababin 

# If you have the MRJClasses.zip on your classpath then you don't need to skip
# the CCMacApplHandler file
SKIP_FILES = source/ui/desktop/CCMacApplHandler.java

PLATFORMS = ce desktop palm

# This includes all the file in the ../jars directory on the classpath
CLASSPATH += :$(shell for jar in $(wildcard ../jars/*); do echo -n $$jar:; done)

main:
	$(foreach PLAT, $(PLATFORMS), $(MAKE) PLATFORM=$(PLAT) classes/$(PLAT);)
	$(MAKE) icons

ifneq ($(PLATFORM),)
classes/$(PLATFORM): $(wildcard $(subst _plat_,$(PLATFORM),$(SRC_DIRS:%=%/*.java)))
	[ -d $@ ] || mkdir -p $@
	javac -classpath $@:$(CLASSPATH):../wabasdk/classes:jars/mrjSpecific.jar -d $@ $(filter-out $(SKIP_FILES),$?)
	touch $@
endif

mrjSpecific.jar: classes/desktop jars
	jar cvf jars/mrjSpecific.jar -C classes/desktop org/concord/waba/extra/ui/CCMacApplHandler.class

jars: 
	mkdir jars

icons: $(PLATFORMS:%=classes/%/icons)

$(PLATFORMS:%=classes/%/icons): 
	mkdir -p $@
	cp icons/*.bmp $@

clean:
	rm -rf classes

include ../ReleaseSystem/Makefile.inc

