/**
 * Module: dap
 * =================
 * A module for programming and simulating Distributed Asynchronous Petri
 * Nets (DAP) like models as Continuous-Time Markov Chains (CTMC) processes.
 */
#ifndef LIBDAP_H
#define LIBDAP_H

#include "ctmc.h"

/*
 * A multi-set of elements of type `Type`. Elements can be repeated and unordered.
 * The programmer can define a multi-set of any type by using the macro `DEFINE_MSET(Type)`.
 */
#define DEFINE_MSET(Type)      \
    typedef struct {           \
        Type* elements;        \
        size_t size;           \
    } MSet_##Type;

typedef struct Place *Place;
typedef struct Id *Id;

DEFINE_MSET(Place)

DEFINE_MSET(Id)

/* The data structure keeping track of the neighbors of a place in a DAP model. */
typedef struct {
    Id point;
    MSet_Id* neighbors;
} Neighbors;

/* The data structure representing a token in a DAP model. */
typedef struct {
    Id id;
    Place place;
} Token;

DEFINE_MSET(Token)

/* The overall state of a DAP model. */
struct State {
    const MSet_Token* tokens;
    const MSet_Token* messages;
};

/*
 * The rule guiding the evolution of a DAP model.
 * A rule is defined by its preconditions, rate, effects, and messages and
 * is fired whenever its preconditions are satisfied, producing the effects
 * in the same place and sending messages to the neighbors, according to its rate.
 */
typedef struct {
    const MSet_Place* preconditions;
    double (*rate)(const MSet_Place*);
    const MSet_Place* effects;
    const MSet_Place* messages;
} Rule;

typedef void* DAP;

DAP create_dap_from_rules(const Rule* rules, size_t rules_size);

CTMC dap_to_ctmc(const DAP* dap);

Trace* simulate_dap(const CTMC* ctmc, const State s0, const Neighbors* neighbors, int neighbors_size, int steps);

#endif
