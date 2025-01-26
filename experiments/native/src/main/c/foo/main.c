#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "foo.h"

struct State {
    int x;
    int y;
};

int main() {
    struct State foo = {
        .x = 1,
        .y = 2,
    };
    printf("foo.x = %d\n", foo.x);
    printf("foo.y = %d\n", foo.y);
    printf("The address of foo is %p\n", (void *)&foo);
    pass_around(&foo);
    return 0;
}
