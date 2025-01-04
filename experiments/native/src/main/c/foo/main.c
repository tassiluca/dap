#include "foo.h"
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

int main() {
    Foo* foo = create_foo(42);
    print_foo(foo);
    Point p = {1, 2};
    print_point(&p);
    print_point(&foo);
    return 0;
}
