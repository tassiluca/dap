CC := clang
CURRENT_DIR := $(shell pwd)
BINARIES_DIR := ${CURRENT_DIR}
ROOT_SCALA_PROJECT := ${CURRENT_DIR}/../../../../
LIBRARY := dap
ifeq ($(OS), Windows_NT)
	LIBRARY_NAME := dap.dll
else ifeq ($(shell uname), Darwin)
	LIBRARY_NAME := libdap.dylib
else
	LIBRARY_NAME := libdap.so
endif
# C compiler flags: https://clang.llvm.org/docs/DiagnosticsReference.html
CFLAGS += -Wpedantic        	# Enforce strict ISO compliance
CFLAGS += -Wall             	# Enable all common warnings
CFLAGS += -Wextra           	# Enable extra warnings
CFLAGS += -Werror           	# Treat warnings as errors
CFLAGS += -fsanitize=address,undefined,null  # Enable address/undefined/null sanitizers
ifeq ($(OS), Linux)
	CFLAGS += -fsanitize=leak	# Enable leak sanitizer (not supported on macOS arm chips)
endif
CFLAGS += -g                	# Include debug symbols

all:	mains

mains: 	libdap ctmc.o gossip.o
	${CC} ${CFLAGS} -o ${BINARIES_DIR}/ctmc.exe   ctmc.o   -L${BINARIES_DIR} -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY}
	${CC} ${CFLAGS} -o ${BINARIES_DIR}/gossip.exe gossip.o -L${BINARIES_DIR} -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY}

ctmc.o: ctmc.c ctmc.h
	${CC} ${CFLAGS} -c ctmc.c

gossip.o: gossip.c dap.h ctmc.h dap_char2d.h dap_char2d_utils.c
	${CC} ${CFLAGS} -c gossip.c

.PHONY: clean libdap

libdap:
	@if [ ! -e ${BINARIES_DIR}/${LIBRARY_NAME} ]; then \
  		mkdir -p ${BINARIES_DIR}; \
  		echo "Building DAP library..."; \
  		cd ${ROOT_SCALA_PROJECT} && \
  			sbt ${LIBRARY}Native/nativeLink && \
  			ls -alh ${LIBRARY}/native/target/scala-3.* && \
  			cp ${LIBRARY}/native/target/scala-3.*/${LIBRARY_NAME} ${BINARIES_DIR}; \
	fi

clean:
	-rm -f ${BINARIES_DIR}/*.o ${BINARIES_DIR}*.dylib ${BINARIES_DIR}/*.dylib \
		${BINARIES_DIR}/*.so ${BINARIES_DIR}/*.exe *.o *.dylib *.so *.exe
