/*
 * A simple program simulating a continuous-time Markov chain (CTMC) created
 * extensionally from a set of transitions.
 * PRO: The actual semantics is programmed in a declarative and functional way
 *      in scala code thanks to scala native adapter interoperable layer.
 * CONS: The state is untyped and, therefore, unsafe (no type checking can prevent
 * |     the user from using a state that is not part of the CTMC set of states).
 * |---> Solution (to be implemented): use macros to generate what is the definition
 *       of the state type and the set of states
 *       (see https://itnext.io/tutorial-generics-in-c-b3362b3376a3).
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "ctmc.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

typedef enum {
    IDLE,
    SEND,
    FAIL,
    DONE
} MyState;

MyState idle = IDLE;
MyState send = SEND;
MyState fail = FAIL;
MyState done = DONE;

const char* toString(MyState s) {
    switch (s) {
        case IDLE: return "IDLE";
        case SEND: return "SEND";
        case FAIL: return "FAIL";
        case DONE: return "DONE";
        default: return "UNKNOWN";
    }
}

int main(int argc, char *argv[]) {
    Transition transitions[6];
    /* 1) Transition(IDLE, 1.0 --> SEND) */
    transitions[0].state = (State) idle;
    transitions[0].action.rate = 1.0;
    transitions[0].action.state = (State) send;
    /* 2) Transition(SEND, 100_000.0 --> SEND) */
    transitions[1].state = (State) send;
    transitions[1].action.rate = 100000.0;
    transitions[1].action.state = (State) send;
    /* 3) Transition(SEND, 200_000.0 --> DONE) */
    transitions[2].state = (State) send;
    transitions[2].action.rate = 200000.0;
    transitions[2].action.state = (State) done;
    /* 4) Transition(SEND, 100_000.0 --> FAIL) */
    transitions[3].state = (State) send;
    transitions[3].action.rate = 100000.0;
    transitions[3].action.state = (State) fail;
    /* 5) Transition(FAIL, 100_000.0 --> IDLE) */
    transitions[4].state = (State) fail;
    transitions[4].action.rate = 100000.0;
    transitions[4].action.state = (State) idle;
    /* 6) Transition(DONE, 1.0 --> DONE) */
    transitions[5].state = (State) done;
    transitions[5].action.rate = 1.0;
    transitions[5].action.state = (State) done;
    /* Actual semantics */
    CTMC* ctmc = create_ctmc_from_transitions(transitions, 6);
    Trace* trace = simulate(
        ctmc,
        (State) idle, /* initial state */
        20            /* number of steps */
    );
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %zu: time=%f, state=%s\n", i, trace->events[i].time, toString((MyState) trace->events[i].state));
    }
    return 0;
}
