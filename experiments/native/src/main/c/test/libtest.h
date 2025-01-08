#ifndef LIBADVCOUNTER_H
#define LIBADVCOUNTER_H

int ScalaNativeInit(void);

typedef struct State* State;

typedef struct {
    double time;
    const State state;
} Event;

typedef struct {
    const Event* events;
    size_t len;
} Trace;

Trace* test(const State s0, int steps);

#endif
