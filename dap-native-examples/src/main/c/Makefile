ROOT_SCALA_PROJECT=../../../../
PROJECT=dap
# C compiler flags: https://gcc.gnu.org/onlinedocs/gcc/Warning-Options.html
CFLAGS += -Wpedantic        	# Enforce strict ISO compliance
CFLAGS += -Wall             	# Enable all common warnings
CFLAGS += -Wextra           	# Enable extra warnings
CFLAGS += -Werror           	# Treat warnings as errors
CFLAGS += -fsanitize=address,undefined,null  # Enable address/undefined/null sanitizers
#CFLAGS += -fsanitize=leak   	# Enable leak sanitizer (not supported on macos arm chips)
CFLAGS += -g                	# Include debug symbols

all:	mains

mains: 	libdap ctmc.o gossip.o
	mkdir -p bin
	gcc ${CFLAGS} -o bin/ctmc ctmc.o \
		-L/Users/lucatassi/Projects/master-thesis/proof-of-concept/dap-native-examples/src/main/c/bin \
		-Wl,-rpath,/Users/lucatassi/Projects/master-thesis/proof-of-concept/dap-native-examples/src/main/c/bin \
		-ldap
	gcc ${CFLAGS} -o bin/gossip gossip.o \
		-L/Users/lucatassi/Projects/master-thesis/proof-of-concept/dap-native-examples/src/main/c/bin \
		-Wl,-rpath,/Users/lucatassi/Projects/master-thesis/proof-of-concept/dap-native-examples/src/main/c/bin \
		-ldap

ctmc.o: ctmc.c ctmc.h
	gcc ${CFLAGS} -c ctmc.c

gossip.o: gossip.c dap.h ctmc.h
	gcc ${CFLAGS} -c gossip.c

.PHONY: clean libdap

libdap:
	@if [ ! -f "bin/libdap.dylib" ]; then \
  		mkdir -p bin; \
  		echo "Building libdap library"; \
  		cd ${ROOT_SCALA_PROJECT} && \
  			sbt ${PROJECT}Native/nativeLink && \
  			cp ${PROJECT}/native/target/scala-3.4.2/libdap.dylib dap-native-examples/src/main/c/bin/libdap.dylib; \
	fi

clean:
	-rm -f bin/* *.o *.dylib *.so
