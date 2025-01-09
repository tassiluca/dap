/*
 * A simple program simulating a continuous-time Markov chain (CTMC) created
 * extensionally from a set of transitions.
 * The actual semantics is programmed in a declarative and functional way
 * in scala code thanks to scala native adapter interoperable layer.
 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "ctmc.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

struct State {
    const char* name;
};

#define DEFINE_STATE(name) \
    static struct State name##_state = {#name}; \
    State name = &name##_state;

DEFINE_STATE(IDLE)
DEFINE_STATE(SEND)
DEFINE_STATE(DONE)
DEFINE_STATE(FAIL)

const char* toString(const State s) {
    return s ? s->name : "UNKNOWN";
}

int main(void) {
    assert(ScalaNativeInit() == 0);
    printf("Simulation of a simple Continuous-Time Markov Chain (CTMC)\n");
    Transition transitions[6];
    /* 1) Transition(IDLE, 1.0 --> SEND) */
    transitions[0].state = IDLE;
    transitions[0].action.rate = 1.0;
    transitions[0].action.state = SEND;
    /* 2) Transition(SEND, 100_000.0 --> SEND) */
    transitions[1].state = SEND;
    transitions[1].action.rate = 100000.0;
    transitions[1].action.state = SEND;
    /* 3) Transition(SEND, 200_000.0 --> DONE) */
    transitions[2].state = SEND;
    transitions[2].action.rate = 200000.0;
    transitions[2].action.state = DONE;
    /* 4) Transition(SEND, 100_000.0 --> FAIL) */
    transitions[3].state = SEND;
    transitions[3].action.rate = 100000.0;
    transitions[3].action.state = FAIL;
    /* 5) Transition(FAIL, 100_000.0 --> IDLE) */
    transitions[4].state = FAIL;
    transitions[4].action.rate = 100000.0;
    transitions[4].action.state = IDLE;
    /* 6) Transition(DONE, 1.0 --> DONE) */
    transitions[5].state = DONE;
    transitions[5].action.rate = 1.0;
    transitions[5].action.state = DONE;
    /* Actual semantics */
    CTMC* ctmc = create_ctmc_from_transitions(transitions, ARRAY_LEN(transitions));
    Trace* trace = simulate(
        ctmc,
        IDLE,   /* initial state */
        20      /* number of steps */
    );
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %zu: time=%f, state=%s\n", i, trace->events[i].time, toString(trace->events[i].state));
    }
    printf("Simulation completed successfully!\n");
    return 0;
}
