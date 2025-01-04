ROOT_SCALA_PROJECT=../../../../
PROJECT=dap
CFLAGS=-Wpedantic -Wall -g

all: 	main

main: 	ctmc.o gossip.o libdap
	gcc ${CFLAGS} -o dap gossip.o -L. -ldap && gcc ${CFLAGS} -o ctmc ctmc.o -L. -ldap

ctmc.o: ctmc.c ctmc.h
	gcc ${CFLAGS} -c ctmc.c

gossip.o: gossip.c dap.h ctmc.h
	gcc ${CFLAGS} -c gossip.c

.PHONY: clean libdap

libdap:
	@if [ ! -f "libdap.dylib" ]; then \
  		echo "Building libdap.dylib"; \
  		cd ${ROOT_SCALA_PROJECT} && sbt ${PROJECT}Native/nativeLink && \
        		cp ${PROJECT}/native/target/scala-3.4.2/libdap.dylib dap-native-examples/src/main/c/libdap.dylib; \
	fi

clean:
	-rm -f dap ctmc *.o *.dylib *.so
