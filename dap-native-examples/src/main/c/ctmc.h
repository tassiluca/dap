#ifndef LIBCTMC_H
#define LIBCTMC_H

int ScalaNativeInit(void);

typedef struct State* State;

typedef struct {
    double rate;
    State state;
} Action;

typedef struct {
    State state;
    Action action;
} Transition;

typedef struct {
    double time;
    State state;
} Event;

typedef struct {
    const Event* events;
    size_t len;
} Trace;

typedef struct CTMC CTMC;

CTMC* create_ctmc_from_transitions(const Transition* rel, size_t rel_size);

Trace* simulate(const CTMC* ctmc, State s0, int steps);

//typedef struct {
//    Transition* transitions;
//    size_t len;
//} Transitions;
// from scala: type Transitions = CStruct2[Ptr[Transition], CSize]
//
//typedef Transitions (*CTMC_TransitionsFunction)(State);
// from scala: type CTMC_TransitionsFunction = CFuncPtr1[State, Transitions]

#endif
