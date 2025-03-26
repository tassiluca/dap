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
 * A multi-set of elements of type `Type`. Elements can be repeated and unordered.
 * The programmer can define a multi-set of any type by using the macro `DEFINE_MSET(Type)`.
 */
#define DEFINE_MSET(Type)                                                   \
typedef struct {                                                            \
    Type* elements;                                                         \
    size_t size;                                                            \
} MSet_##Type;                                                              \
                                                                            \
/* Constructor */                                                           \
static inline MSet_##Type* MSet_##Type##_create(size_t initial_size) {      \
    MSet_##Type* set = (MSet_##Type*)malloc(sizeof(MSet_##Type));           \
    set->elements = (Type*)malloc(sizeof(Type) * initial_size);             \
    set->size = initial_size;                                               \
    return set;                                                             \
}                                                                           \
                                                                            \
/* Destructor */                                                            \
static inline void MSet_##Type##_destroy(MSet_##Type* set) {                \
    if (set) {                                                              \
        if (set->elements) {                                                \
            free(set->elements);                                            \
        }                                                                   \
        free(set);                                                          \
    }                                                                       \
}                                                                           \
                                                                            \
/* Setter */                                                                \
static inline void MSet_##Type##_set(MSet_##Type* set, size_t index, Type value) { \
    if (index < set->size) {                                                \
        set->elements[index] = value;                                       \
    }                                                                       \
}                                                                           \
                                                                            \
/* Getter */                                                                \
static inline Type MSet_##Type##_get(MSet_##Type* set, size_t index) {      \
    return set->elements[index];                                            \
}

typedef char* Neighbour;

/* The data structure keeping track of the neighbors of a place in a DAP model. */
DEFINE_MSET(Neighbour)

/* An opaque data structure representing a token in a DAP model. */
typedef struct TokenImpl *Token;

/** A multi-set of tokens. */
DEFINE_MSET(Token)

/* The overall state of a DAP model. */
struct DAPState {
    MSet_Token *tokens;
    Token msg;
};

/*
 * The rule guiding the evolution of a DAP model.
 * A rule is defined by its preconditions, rate, effects, and messages and
 * is fired whenever its preconditions are satisfied, producing the effects
 * in the same place and sending messages to the neighbors, according to its rate.
 */
typedef struct {
    const MSet_Token *preconditions;
    double rate;
    const MSet_Token *effects;
    Token msg;
} Rule;

DEFINE_MSET(Rule)

/*
 * Launches the distributed simulation of a DAP model.
 * The simulation is started on the given `port` with a preconfigured `neighborhood`
 * and is guided by the given `rules`, which are applied to the initial state `s0`.
 */
void launch_simulation(
    MSet_Rule* rules,
    struct DAPState *s0,
    int port,
    MSet_Neighbour *neighborhood,
    void (*on_state_change)(struct DAPState *state)
);

/*===================================== UTILS =====================================*/

int register_serde(
    const char* name,
    uint8_t* (*serialize_fn)(void *data, size_t *out_size),
    void* (*deserialize_fn)(uint8_t *bytes, int size)
);

int register_equatable(
    const char* name,
    int (*equals_fn)(void *a, void *b)
);

#ifdef __cplusplus
}
#endif

#endif
