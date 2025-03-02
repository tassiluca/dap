/**
 * Module: dap
 * =================
 * A module for programming and simulating Distributed Asynchronous Petri Nets (DAP).
 */
#ifndef LIBDAP_H
#define LIBDAP_H

#include <stddef.h>

/*
 * A multi-set of elements of type `Type`. Elements can be repeated and unordered.
 * The programmer can define a multi-set of any type by using the macro `DEFINE_MSET(Type)`.
 */
#define DEFINE_MSET(Type)      \
    typedef struct {           \
        Type* elements;        \
        size_t size;           \
    } MSet_##Type;

typedef char* Neighbour;

/* The data structure keeping track of the neighbors of a place in a DAP model. */
DEFINE_MSET(Neighbour)

/* The data structure representing a token in a DAP model. */
typedef struct TokenImpl *Token;

DEFINE_MSET(Token)

struct TokenImpl {
  char* token;
};

/* The overall state of a DAP model. */
struct DAPState {
    MSet_Token tokens;
    Token msg;
};

/*
 * The rule guiding the evolution of a DAP model.
 * A rule is defined by its preconditions, rate, effects, and messages and
 * is fired whenever its preconditions are satisfied, producing the effects
 * in the same place and sending messages to the neighbors, according to its rate.
 */
typedef struct {
    const MSet_Token preconditions;
    double (*rate)(MSet_Token);
    const MSet_Token effects;
    const Token msg;
} Rule;

void launch_simulation(
    const Rule* rules,
    size_t rules_size,
    struct DAPState *s0,
    int port,
    MSet_Neighbour *neighborhood
);

#endif
