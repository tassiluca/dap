/**
 * Module: dap
 * =================
 * A module for programming and simulating Distributed Asynchronous Petri Nets (DAP).
 */
#ifndef LIBDAP_H
#define LIBDAP_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>
#include <stdint.h>

/*
 * RawData is a structure that holds raw data and its size.
 * It is used to represent a block of serialized data.
 */
typedef struct {
    uint8_t* data;
    size_t size;
} RawData;

RawData *pack(uint8_t* data, size_t size) {
    RawData *rd = (RawData*)malloc(sizeof(RawData));
    if (!rd) return NULL;
    rd->data = (uint8_t*)malloc(size);
    if (!rd->data) {
        free(rd);
        return NULL;
    }
    memcpy(rd->data, data, size);
    rd->size = size;
    return rd;
}

/*
 * A multi-set of elements of type `Type`. Elements can be repeated and unordered.
 * The programmer can define a multi-set of any type by using the macro `DEFINE_MSET(Type)`.
 */
#define DEFINE_MSET(Type)                                                   \
typedef struct {                                                            \
    Type* elements;                                                         \
    size_t size;                                                            \
} MSet_##Type;                                                              \
                                                                            \
/*
 * Creates a new multi-set with the given elements and size.
 */                                                                         \
MSet_##Type* MSet_##Type##_of(Type* elements, size_t size);                 \
                                                                            \
/*
 * Frees the memory allocated for the multi-set.
 */                                                                         \
void MSet_##Type##_free(MSet_##Type* set);                                  \
                                                                            \
Type MSet_##Type##_get(MSet_##Type* set, size_t index) {                    \
    if (index < set->size) {                                                \
        return set->elements[index];                                        \
    }                                                                       \
    return NULL;                                                            \
}

/*
 * An opaque data structure representing a token in a DAP model, i.e.,
 * the data exchanged between places (nodes) in the Distributed Petri Net.
 */
typedef const RawData *Token;

/*
 * A multi-set of tokens.
 */
DEFINE_MSET(Token)

/*
 * A snapshot of the state of the current place (node).
 */
struct DAPState {
    const MSet_Token *tokens;
    Token msg;
};

/*
 * The rule guiding the evolution of the Distributed Petri Net.
 * A rule is defined by its preconditions, rate, effects, and messages and
 * is fired whenever its preconditions are satisfied, producing the effects
 * in the same place (node) and sending messages to the neighbors, according to its rate.
 */
typedef struct {
    const MSet_Token *preconditions;
    double rate;
    const MSet_Token *effects;
    Token msg;
} Rule;

/*
 * A neighbor place (node) in the Distributed Petri Net.
 * It is represented as string in the form of <hostname>:<port>.
 */
typedef const char* Neighbour;

/*
 * Launches the distributed simulation of a DAP model.
 * The simulation is started on the given `port` with a preconfigured `neighborhood`
 * and is guided by the given `rules`, which are applied to the initial state `s0`.
 */
void launch_simulation(
    const Rule* rules, size_t rules_size,
    const struct DAPState *s0,
    int port,
    const Neighbour *neighbors, size_t neighbors_size,
    void (*on_state_change)(const struct DAPState *state),
    int (*equals_fn)(const RawData *a, const RawData *b)
);

#ifdef __cplusplus
}
#endif

#endif
