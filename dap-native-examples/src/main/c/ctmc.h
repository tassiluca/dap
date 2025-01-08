#ifndef LIBCTMC_H
#define LIBCTMC_H

typedef struct State State;

typedef struct {
    double rate;
    const State* state;
} Action;

typedef struct {
    const State* state;
    Action action;
} Transition;

typedef struct CTMC CTMC;

CTMC* create_ctmc_from_transitions(const Transition* rel, size_t rel_size);

typedef struct {
    double time;
    const State* state;
} Event;

typedef struct {
    const Event* events;
    size_t len;
} Trace;

Trace* simulate(const CTMC* ctmc, const State* s0, size_t steps);

//typedef struct {
//    Transition* transitions;
//    size_t len;
//} Transitions;
// from scala: type Transitions = CStruct2[Ptr[Transition], CSize]
//
//typedef Transitions (*CTMC_TransitionsFunction)(State);
// from scala: type CTMC_TransitionsFunction = CFuncPtr1[State, Transitions]

#endif
