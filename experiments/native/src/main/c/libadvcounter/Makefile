ROOT_SCALA_PROJECT=../../../../../../
PROJECT=experiments
CFLAGS=-ansi -Wpedantic -Wall -fsanitize=address,undefined
TARGET=main

all: 	main

main: 	main.o libcounter
	  	gcc ${CFLAGS} -o ${TARGET} main.o -L. -lexperiments

main.o: main.c libadvcounter.h
	  	gcc ${CFLAGS} -c main.c

.PHONY: clean libcounter

libcounter:
	  	cd ${ROOT_SCALA_PROJECT} && sbt experimentsNative/nativeLink && \
  			cp ${PROJECT}/native/target/scala-3.4.2/libexperiments.dylib ${PROJECT}/native/src/main/c/libadvcounter

clean:
	  	-rm -f ${TARGET} *.o *.dylib *.so
