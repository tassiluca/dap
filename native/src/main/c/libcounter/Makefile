CFLAGS=-ansi -Wpedantic -Wall
TARGET=main

all: 	main

main: 	main.o libcounter
	  	gcc ${CFLAGS} -o ${TARGET} main.o -L. -lproof-of-concept

main.o: main.c libcounter.h
	  	gcc ${CFLAGS} -c main.c

.PHONY: clean libcounter

libcounter:
	  	cd ../../../../.. && sbt proofNative/nativeLink && cp native/target/scala-3.4.2/libproof-of-concept.dylib native/src/main/c/libcounter

clean:
	  	-rm -f ${TARGET} *.o *.dylib *.so
