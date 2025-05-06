#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include <string.h>
#include <unistd.h>

#include "gossip_model.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

void on_state_change(const struct DAPState *state);

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count);

int main(int argc, char *argv[]) {
  if (argc < 2) {
    perror("Usage: gossip <port> <neighbor> [<neighbor>...]\n"
           "\n"
           "\t <port>: the port the node will listen on for incoming neighbors connections\n"
           "\t <neighbor>: endpoint in the form of <hostname>:<port> of the "
           "neighbor node");
    return 1;
  }
  int port = atoi(argv[1]);
  printf("===================================\n");
  printf("== C Program - Gossip simulation ==\n");
  printf("===================================\n");
  /* The tokens used in the simulation */
  Token a = createToken("a", port);
  // Token b = createToken("b", port);
  /* 1) a|a --1_000--> a */
  Token in_tokens1[] = {a, a};
  Array_Token *preconditions1 = Array_Token_of(in_tokens1, ARRAY_LEN(in_tokens1));
  Token out_places1[] = {a};
  Array_Token *effects1 = Array_Token_of(out_places1, ARRAY_LEN(out_places1));
  Rule rule = {
    .preconditions = preconditions1,
    .rate = 1000,
    .effects = effects1,
    .msg = NULL
  };
  /* 2) a --1--> a|^a */
  Token in_tokens2[] = {a};
  Array_Token *preconditions2 = Array_Token_of(in_tokens2, ARRAY_LEN(in_tokens2));
  Token out_places2[] = {a};
  Array_Token *effects2 = Array_Token_of(out_places2, ARRAY_LEN(out_places2));
  Rule rule2 = {
    .preconditions = preconditions2,
    .rate = 1,
    .effects = effects2,
    .msg = a
  };
  Rule rules[] = {rule, rule2};
  Array_Rule *all_rules = Array_Rule_of(rules, ARRAY_LEN(rules));
  /* State */
  struct DAPState *initial_state = malloc(sizeof(struct DAPState));
  initial_state->msg = NULL;
  if (port == 2550) {
    Token initial_tokens_arr[] = {a};
    Array_Token *initial_tokens = Array_Token_of(initial_tokens_arr, ARRAY_LEN(initial_tokens_arr));
    initial_state->tokens = initial_tokens;
  //   } else if (port == 2553) {
  //     Token initial_tokens[] = {b};
  //     initial_state = createDAPState(initial_tokens,
  //     ARRAY_LEN(initial_tokens));
  } else {
    Array_Token *initial_tokens = Array_Token_of(NULL, 0);
    initial_state->tokens = initial_tokens;
  }
  /* Neighborhood. */
  int neighbors_size = argc - 2;
  Neighbor neighbors[neighbors_size];
  for (int i = 0; i < neighbors_size; i++) {
    char *arg = argv[i + 2];
    char *sep = strchr(arg, ':');
    if (!sep) {
      fprintf(stderr, "Invalid format: %s\n", arg);
      continue;
    }
    *sep = '\0';
    neighbors[i].name = strdup(arg);
    neighbors[i].port = atoi(sep + 1);
  }
  printf("\n");
  Array_Neighbor *neighborhood = Array_Neighbor_of(neighbors, neighbors_size);
  /* Launch simulation. */
  DASPSimulation sim = simulation(all_rules, initial_state, neighborhood, &serialize, &deserialize, &equals);
  launch(sim, port, &on_state_change);
  sleep(30);
  stop(sim);
  sleep(3);
  return 0;
}

void on_state_change(const struct DAPState *state) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  const struct tm *lt = localtime(&tv.tv_sec);
  printf("\n--------------------------------------------------\n");
  printf("[C] %02d:%02d:%02d.%03d \n", lt->tm_hour, lt->tm_min, lt->tm_sec, tv.tv_usec / 1000);
  printf("[C] State Tokens: ");
  printf("{ ");
  for (size_t i = 0; i < state->tokens->size; i++) {
    print_token(state->tokens->elements[i]);
    if (i < state->tokens->size - 1) {
      printf(" | ");
    }
  }
  printf(" }\n");
  printf("[C] Message: ");
  print_token(state->msg);
  printf("\n--------------------------------------------------\n");
}
