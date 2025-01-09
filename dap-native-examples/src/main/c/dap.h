#ifndef LIBDAP_H
#define LIBDAP_H

typedef struct State* State;

#define DEFINE_MSET(Type)      \
    typedef struct {           \
        const Type* elements; \
        size_t size;           \
    } MSet_##Type;

typedef struct Place* Place;
typedef struct Id* Id;

DEFINE_MSET(Place)
DEFINE_MSET(Id)

typedef struct {
    Id point;
    const MSet_Id* neighbors;
} Neighbors;

typedef struct {
    Id id;
    Place place;
} Token;

DEFINE_MSET(Token)

typedef struct {
    double time;
    State state;
} Event;

typedef struct {
    const Event* events;
    size_t len;
} Trace;

struct State {
    const MSet_Token* tokens;
    const MSet_Token* messages;
};

typedef struct {
    const MSet_Place* preconditions;
    double (*rate)(const MSet_Place*);
    const MSet_Place* effects;
    const MSet_Place* messages;
} Rule;

typedef struct DAP DAP;
typedef struct CTMC CTMC;

DAP* create_dap_from_rules(const Rule* rules, size_t rules_size);

CTMC* dap_to_ctmc(const DAP* dap);

Trace* simulate_dap(const CTMC* ctmc, const State s0, const Neighbors* neighbors, int neighbors_size, int steps);

#endif
