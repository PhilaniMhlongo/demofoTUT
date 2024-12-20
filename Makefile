SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

.PHONY: help clean test grade

# help: @ Lists available make tasks
help:
	@egrep -oh '[0-9a-zA-Z_\.\-]+:.*?@ .*' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?@ "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' | sort

# clean: @ Clean the build
clean:
	mvn clean

# test: @ Run all tests
test:
	mvn test

# grade: @ Run the grading process for this exercise.
GRADING_FILES = grading.rsync-filter.txt

grade: SUBMISSION_DIR?=${PWD}
grade: GRADER_IMAGE=wethinkcode/grader-java
grade: GRADE_CMD=mvn test
grade:
	@echo +++ Copying protected/grading files over student submission dir: $(SUBMISSION_DIR)
	rsync -av --include-from "$(GRADING_FILES)" . "$(SUBMISSION_DIR)" --ignore-times

	@echo +++ Running grading command
	pushd "$(SUBMISSION_DIR)"
	docker run -t -v $(SUBMISSION_DIR):/app $(GRADER_IMAGE):latest '$(GRADE_CMD)'
	popd

