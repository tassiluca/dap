#ifndef FOO_H
#define FOO_H

typedef struct Point {
    int x;
    int y;
} Point;
typedef struct Foo Foo;

void print_point(Point*);

Foo* create_foo(int);

void print_foo(Foo*);

#endif