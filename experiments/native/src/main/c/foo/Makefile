CC = gcc
CFLAGS =
TARGET = main
OBJS = main.o foo.o

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJS)

main.o: main.c foo.h
	$(CC) $(CFLAGS) -c main.c

foo.o: foo.c foo.h
	$(CC) $(CFLAGS) -c foo.c

clean:
	rm -f $(TARGET) $(OBJS)

.PHONY: all clean
