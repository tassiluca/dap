/**
 * Module: ctmc
 * =================
 * A module for creating and simulating Continuous-Time Markov Chains (CTMC).
 */
#ifndef LIBCTMC_H
#define LIBCTMC_H

/* The opaque type representing a state in a CTMC process. */
typedef struct State* State;

/* An action in a CTMC process, namely a rate and a target state. */
typedef struct {
    double rate;
    State state;
} Action;

/* A transition in a CTMC process, namely a source state and an action. */
typedef struct {
    State state;
    Action action;
} Transition;

/* A simulation event, i.e. a step in the simulation.
 * It contains the time and the state of the CTMC process.
 */
typedef struct {
    double time;
    State state;
} Event;

/* A simulation trace, namely the sequence of events composing the simulation. */
typedef struct {
    const Event* events;
    size_t len;
} Trace;

/* A Continuous-Time Markov Chain (CTMC) */
typedef void* CTMC;

/* Create a CTMC from an array of transitions. */
CTMC create_ctmc_from_transitions(const Transition* rel, size_t rel_size);

/* Simulate a CTMC process starting from a given state s0 for a given number of steps. */
Trace* simulate(const CTMC* ctmc, State s0, int steps);

#endif
