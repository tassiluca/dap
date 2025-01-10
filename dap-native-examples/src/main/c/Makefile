CC := clang
CURRENT_DIR := $(shell pwd)
BINARIES_DIR := ${CURRENT_DIR}/bin
ROOT_SCALA_PROJECT := ${CURRENT_DIR}/../../../../
LIBRARY := dap
# C compiler flags: https://clang.llvm.org/docs/DiagnosticsReference.html
CFLAGS += -Wpedantic        	# Enforce strict ISO compliance
CFLAGS += -Wall             	# Enable all common warnings
CFLAGS += -Wextra           	# Enable extra warnings
CFLAGS += -Werror           	# Treat warnings as errors
CFLAGS += -fsanitize=address,undefined,null  # Enable address/undefined/null sanitizers
#CFLAGS += -fsanitize=leak   	# Enable leak sanitizer (not supported on macOS arm chips)
CFLAGS += -g                	# Include debug symbols

all:	mains

mains: 	libdap ctmc.o gossip.o
	mkdir -p bin
	${CC} ${CFLAGS} -o bin/ctmc 	ctmc.o 		-L${BINARIES_DIR} -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY}
	${CC} ${CFLAGS} -o bin/gossip 	gossip.o 	-L${BINARIES_DIR} -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY}

ctmc.o: ctmc.c ctmc.h
	${CC} ${CFLAGS} -c ctmc.c

gossip.o: gossip.c dap.h ctmc.h
	${CC} ${CFLAGS} -c gossip.c

.PHONY: clean libdap

libdap:
	@if [ ! -e bin/libdap* ]; then \
  		mkdir -p bin; \
  		echo "Building DAP library..."; \
  		cd ${ROOT_SCALA_PROJECT} && \
  			sbt ${LIBRARY}Native/nativeLink && \
  			cp ${LIBRARY}/native/target/scala-3.*/libdap.* dap-native-examples/src/main/c/bin/; \
	fi

clean:
	-rm -f ${BINARIES_DIR}/* *.o *.dylib *.so *.exe
