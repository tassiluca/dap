ROOT_SCALA_PROJECT=../../../../../../
PROJECT=experiments
CC:=clang
CFLAGS=-ansi -Wpedantic -Wall -fsanitize=address,undefined
TARGET=main

all: 	main

main: 	main.o lib
	  	${CC} ${CFLAGS} -o ${TARGET} main.o -L. -lexperiments

main.o: main.c foo.h
	  	${CC} ${CFLAGS} -c main.c

.PHONY: clean lib

lib:
	  	cd ${ROOT_SCALA_PROJECT} && sbt experimentsNative/nativeLink && \
  			cp ${PROJECT}/native/target/scala-3.4.2/libexperiments.dylib ${PROJECT}/native/src/main/c/foo

clean:
	  	-rm -f ${TARGET} *.o *.dylib *.so
