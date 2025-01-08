#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "libtest.h"

struct State {
    char* value;
};

#define DEFINE_STATE(name) \
    static struct State name##_state = {#name}; \
    const State* name = &name##_state;

DEFINE_STATE(OK)

int main(int argc, char** argv) {
    assert(ScalaNativeInit() == 0);
    Trace* trace = test(OK, 10);
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %zu: time=%f, state=%s\n", i, trace->events[i].time, trace->events[i].state->value);
    }
    return 0;
}
