#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

struct Token {
  char* token;
};

static struct Token a = { "a" };

double fixed_rate_1000(const MSet_Token set __attribute__((unused))) {
  return 1000.0;
}

double fixed_rate_1(const MSet_Token set __attribute__((unused))) {
  return 1.0;
}

int main(int argc, char *argv[]) {
  printf("Gossip simulation\n");
  /* 1) a|a --1_000--> a */
  Token in_tokens1[] = { &a, &a };
  MSet_Token preconditions1 = { in_tokens1, ARRAY_LEN(in_tokens1) };
  Token out_places1[] = { &a };
  MSet_Token effects1 = { out_places1, ARRAY_LEN(out_places1) };
  Token messages1 = NULL;
  Rule rule = {
    .preconditions = preconditions1,
    .rate = &fixed_rate_1000,
    .effects = effects1,
    .msg = messages1
  };
  /* 2) a --1--> a | ^a */
  Token in_tokens2[] = { &a };
  MSet_Token preconditions2 = { in_tokens2, ARRAY_LEN(in_tokens2) };
  Token out_places2[] = { &a };
  MSet_Token effects2 = { out_places2, ARRAY_LEN(out_places2) };
  Rule rule2 = {
    .preconditions = preconditions2,
    .rate = &fixed_rate_1,
    .effects = effects2,
    .msg = in_tokens2[0]
  };
  Rule rules[] = { rule, rule2 };
  /* State */
  Token initial_tokens[] = { &a };
  MSet_Token initial_with_token = { initial_tokens, ARRAY_LEN(initial_tokens) };
  MSet_Token initial_empty = { NULL, 0 };
  int port = atoi(argv[1]);
  struct DAPState *state = malloc(sizeof(struct DAPState));
  if (!state) {
    perror("Memory allocation failed");
    return 1;
  }
  if (port == 2550) {
    state->tokens = initial_with_token;
    state->msg = NULL;
  } else {
    state->tokens = initial_empty;
    state->msg = NULL;
  }
  /* Neighborhood */
  int neighbours_size = argc - 2;
  Neighbour neighbours[neighbours_size];
  for (int i = 0; i < neighbours_size; i++) {
    neighbours[i] = argv[i + 2];
  }
  MSet_Neighbour neighborhood = { neighbours, ARRAY_LEN(neighbours) };
  /* Simulation */
  launch_simulation(rules, ARRAY_LEN(rules), state, atoi(argv[1]), &neighborhood);
  printf("COMPLETE!\n");
  free(state);
  return 0;
}
