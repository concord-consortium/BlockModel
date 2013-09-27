# to include this you need to define:
# MODULE
# DEP_NAMES
#

#should check for already built dependencies
#need a list somehow
#export EXP_DEP_NAME = $(EXP_DEP_NAME) $(foreach DEP,$(DEP_NAMES), $(if $(findstring $DEP, $EXP_DEP_NAME), , $(DEP))) 

DEP_TAGS = $(DEP_NAMES:%=../%/tag)

.PHONY: dependencies release $(DEP_NAMES) $(DEP_NAMES:%=../%)

dependencies: $(DEP_NAMES:%=../%)
	
$(DEP_NAMES:%=../%):
	cd $@ && $(MAKE)

release: tag $(DEP_TAGS) $(DEP_NAMES) ReleaseSystem
	# supposably this means we have taged everything
	# so now we can call the realease makefile which will checkout
	# all these files using our tag and try to build it and publish it
	cd ../ReleaseSystem/ && $(MAKE) release-sub TAG=$(shell cat tag) MODULE=$(MODULE)

tag:
	@echo "You must make a version or branch first for " $(MOD_NAME)
	@exit 1

$(DEP_TAGS) ../ReleaseSystem/tag : ../%/tag:
	@echo You must make a version or branch first for **$(*)**
	@exit 1
	
$(DEP_NAMES) ReleaseSystem : % : tag ../%/tag
	#Tag the dependencies with our tag so we can get them back
	cvs rtag -r $(shell cat ../$@/tag) $(shell cat tag) $@

include ../ReleaseSystem/cvs.inc
