ROOT_SCALA_PROJECT=../../../../
PROJECT=dap
CFLAGS=-ansi -Wpedantic -Wall
TARGET=dap

all: 	main

main: 	gossip.o libdap
	  	gcc ${CFLAGS} -o ${TARGET} gossip.o -L. -ldap

gossip.o: gossip.c dap.h
	  	gcc ${CFLAGS} -c gossip.c

.PHONY: clean libdap

libdap:
	  	cd ${ROOT_SCALA_PROJECT} && sbt ${PROJECT}Native/nativeLink && \
  			cp ${PROJECT}/native/target/scala-3.4.2/libdap.dylib dap-native-examples/src/main/c/libdap.dylib

clean:
	  	-rm -f ${TARGET} *.o *.dylib *.so
