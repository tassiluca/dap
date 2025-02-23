#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

struct Token {
  char token;
};

static struct Token a = { 'a' };

double fixed_rate_1000(const MSet_Token set) {
  return 1000.0;
}

double fixed_rate_1(const MSet_Token set) {
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
    .msg = &messages1
  };

  Token in_tokens2[] = { &a };
  MSet_Token preconditions2 = { in_tokens2, ARRAY_LEN(in_tokens2) };
  Token out_places2[] = { &a };
  MSet_Token effects2 = { out_places2, ARRAY_LEN(out_places2) };
  Token messages2 = { &a };
  Rule rule2 = {
    .preconditions = preconditions2,
    .rate = &fixed_rate_1,
    .effects = effects2,
    .msg = &in_tokens2[0]
  };
  printf("Rule: %p\n", &rule2);
  Rule rules[] = { rule, rule2 };
  /* State */
  Token initial_tokens[] = { &a };
  MSet_Token initials = { initial_tokens, ARRAY_LEN(initial_tokens) };
  struct DAPState state = { &initials, NULL };
  /* Neighborhood */
  Neighborhood nb;
  nb.my_port = atoi(argv[1]);
  nb.size = 1;
  nb.address = malloc(nb.size * sizeof(char*));
  if (!nb.address) {
    perror("Failed to allocate memory for address");
    return 1;
  }
  nb.port = malloc(nb.size * sizeof(int));
  if (!nb.port) {
    perror("Failed to allocate memory for port");
    free(nb.address);
    return 1;
  }
  nb.address[0] = strdup("127.0.0.1"); // strdup allocates memory and copies string
  if (!nb.address[0]) {
    perror("Failed to allocate memory for address string");
    free(nb.port);
    free(nb.address);
    return 1;
  }
  nb.port[0] = atoi(argv[2]);
  /* Simulation */
  printf("Neighborhood: %p\n", &nb);
  launch_simulation(rules, ARRAY_LEN(rules), &state, &nb);
  printf("COMPLETE!\n");
  /* Free allocated memory. */
  free(nb.address[0]);
  free(nb.address);
  free(nb.port);
  return 0;
}
