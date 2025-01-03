#ifndef LIBDAP_H
#define LIBDAP_H
#include <stddef.h>

typedef void* State;

typedef struct {
    double rate;
    State state;
} Action;

typedef struct {
    Action* actions;
    size_t count;
} Transitions;

typedef Transitions (*CTMC_TransitionsFunction)(State);

typedef struct {
    CTMC_TransitionsFunction transitions;
} CTMC;

CTMC CTMC_ofTransitions(const Transitions* rel, size_t rel_size);

CTMC CTMC_ofFunction(CTMC_TransitionsFunction f);

typedef struct {
    double time;
    State state;
} Event;

typedef struct EventNode {
    Event event;
    struct EventNode* next;
} Trace;

Trace* CTMC_newSimulationTrace(CTMC ctmc, State s0, unsigned int seed);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////

typedef struct {
    const char* const* elements;
    size_t len;
} MultiSet;

typedef struct Rule Rule;

Rule* stringed_rule(MultiSet *preconditions, double (*rate)(MultiSet*), MultiSet *effects, MultiSet *messages);

#endif
