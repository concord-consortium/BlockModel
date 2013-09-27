# You must have your CVSROOT variable set
# You must pass this the TAG variable

ALL_MODULE_NAME= $(MODULE)-all
BUILD_DOC= $(ALL_MODULE_NAME)/$(MODULE)/bin/BUILD.txt
REMOTE_HOST := hub.concord.org
REMOTE_ROOT := releases
VER_MODULE = $(subst _,.,$(TAG))

release-sub:
# Check out the release to our directory
# This will ask for a password
	cvs checkout -r $(TAG) $(ALL_MODULE_NAME)
# Tar all the sources
	tar cvzf $(VER_MODULE).tar.gz $(ALL_MODULE_NAME)
# Build it:  This means it must automatically build all 
#   dependencies the return code is checked
	cd $(ALL_MODULE_NAME)/$(MODULE) && $(MAKE)
# Save tag name
	@echo "The tag of this build is:" > $(BUILD_DOC) 
	@echo "$(TAG)" >> $(BUILD_DOC) 
# ftp the sources to the correct directory
	mv $(VER_MODULE).tar.gz $(ALL_MODULE_NAME)/$(MODULE)/bin
	ncftpput -u scytacki -m $(REMOTE_HOST) \
$(REMOTE_ROOT)/$(VER_MODULE) \
$(ALL_MODULE_NAME)/$(MODULE)/bin/*

include ../ReleaseSystem/cvs.inc
