#include <stdio.h>
#include <stdlib.h>
#include "foo.h"

void print_point(Point* p) {
    printf("Point2d = (%d, %d)\n", p->x, p->y);
}

struct Foo {
    int value;
};

Foo* create_foo(int x) {
    Foo* foo = (Foo*)malloc(sizeof(Foo));
    foo->value = x;
    return foo;
}

void print_foo(Foo* foo) {
    printf("Foo is just the number %d\n", foo->value);
}
