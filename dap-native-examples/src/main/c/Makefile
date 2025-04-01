OS := $(shell uname -s)
ifeq ($(OS), Darwin)
    OS := macOS $(shell sw_vers -productVersion | cut -d. -f1)
endif
CC := clang
CURRENT_DIR := $(shell pwd)
BINARIES_DIR := ${CURRENT_DIR}
ROOT_SCALA_PROJECT := ${CURRENT_DIR}/../../../../
LIBRARY := dap
# C compiler flags: https://clang.llvm.org/docs/DiagnosticsReference.html
CFLAGS += -Wpedantic        	# Enforce strict ISO compliance
CFLAGS += -Wall             	# Enable all common warnings
CFLAGS += -Wextra           	# Enable extra warnings
CFLAGS += -Werror           	# Treat warnings as errors
CFLAGS += -fsanitize=address,undefined,null  # Enable address/undefined/null sanitizers
ifneq ($(OS), macOS 15)
	CFLAGS += -fsanitize=leak  	# Enable leak sanitizer (not supported on macOS >= 15.x)
endif
CFLAGS += -g                	# Include debug symbols

all:	mains
	@echo "Detected OS: ${OS}"

mains: 	libdap gossip_simulation.o gossip_model.o
	${CC} ${CFLAGS} -o ${BINARIES_DIR}/gossip_simulation.exe gossip_simulation.o gossip_model.o -L${BINARIES_DIR} -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY}

gossip.o: gossip_simulation.c dap.h
	${CC} ${CFLAGS} -c gossip_simulation.c gossip_model.c

.PHONY: clean libdap

libdap:
	@if [ ! -e ${BINARIES_DIR}/libdap* ]; then \
  		mkdir -p ${BINARIES_DIR}; \
  		echo "Building DAP library..."; \
  		cd ${ROOT_SCALA_PROJECT} && \
  			sbt ${LIBRARY}Native/nativeLink && \
  			cp ${LIBRARY}/native/target/scala-3.*/libdap.* ${BINARIES_DIR}; \
	fi

clean:
	-rm -f ${BINARIES_DIR}/*.o ${BINARIES_DIR}*.dylib ${BINARIES_DIR}/*.dylib \
		${BINARIES_DIR}/*.so ${BINARIES_DIR}/*.exe *.o *.dylib *.so *.exe