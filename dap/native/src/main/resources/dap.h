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

  #include <stdlib.h>
  #include <stdbool.h>
  #include <stddef.h>
  #include <stdint.h>
  #include <stdio.h>

  /*
  * A multi-set of elements of type `Type`. Elements can be repeated and
  * unordered. The programmer can define a multi-set of any type by using the
  * macro `DEFINE_MSET(Type)`.
  */
  #define DEFINE_ARRAY(Type)                                                     \
    typedef struct {                                                             \
      Type *elements;                                                            \
      size_t size;                                                               \
    } Array_##Type;                                                              \
                                                                                 \
    static inline Array_##Type *Array_##Type##_of(Type *elements, size_t size) { \
      Array_##Type *array = (Array_##Type *) malloc(sizeof(Array_##Type));       \
      if (array == NULL) {                                                       \
        perror("Allocation memory error on array");                              \
        exit(EXIT_FAILURE);                                                      \
      }                                                                          \
      array->elements = elements;                                                \
      array->size = size;                                                        \
      return array;                                                              \
    }

  /**
   * A generic structure representing a token in the DAP model, i.e.,
   * the data exchanged between places (nodes) in the Distributed Petri Net.
   */
  typedef const void *Token;

  DEFINE_ARRAY(Token)

  /** A snapshot of the state of the current place (node) in the Distributed Petri Net. */
  struct DAPState {
    const Array_Token *tokens;
    Token msg;
  };

  /**
   * The rule guiding the evolution of the Distributed Petri Net.
   * A rule is defined by its `preconditions`, `rate`, `effects`, and `messages` and
   * is fired whenever its preconditions are satisfied, producing the effects
   * in the same place (node) and sending messages to the neighbors, according to
   * its rate.
   */
  typedef struct {
    const Array_Token *preconditions;
    double rate;
    const Array_Token *effects;
    Token msg;
  } Rule;

  DEFINE_ARRAY(Rule)

  /**
   * A neighbor place (node) in the Distributed Petri Net.
   * It is represented as string in the form of <hostname>:<port>.
   */
  typedef struct {
    const char *name;
  } Neighbor;

  DEFINE_ARRAY(Neighbor)

  /** An opaque type representing a DAP simulation based on sockets. */
  typedef const void *DASPSimulation;

  /** 
   * Creates a DAP simulation based on sockets with statically-encoded set of neighbors.
   * @param rules the rules to be applied to the simulation.
   * @param initial_state the initial state of the simulation.
   * @param neighborhood the neighborhood of this node, i.e., the set of
   *                      neighbors to which this node is connected.
   * @param serializer a function to serialize the token into a string.
   * @param deserializer a function to deserialize the string into a token.
   * @param equalizer a function to compare two tokens for equality.
   * @return a Simulation object representing the DAP simulation.
   */
  DASPSimulation simulation(
    Array_Rule *rules, 
    const struct DAPState *initial_state,
    Array_Neighbor *neighborhood,
    const char *(*serializer)(Token),
    Token (*deserializer)(const char *str),
    bool (*equalizer)(Token, Token)
  );

  /**
   * Launches the DAP simulation. Side effects happens.
   * @param simulation the simulation to be launched.
   * @param port the port the node will listen on for incoming neighbors connections.
   * @param on_state_change a callback function to be called when the state of the 
   *                        simulation changes.
   */
  void launch(DASPSimulation simulation, int port, void (*on_state_change)(const struct DAPState *state));

  /**
   * Stops the DAP simulation.
   * @param simulation the simulation instance to be stopped.
   */
  void stop(DASPSimulation simulation);

#ifdef __cplusplus
}
#endif

#endif
