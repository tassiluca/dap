#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>  // Necessario per ottenere i millisecondi
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

double fixed_rate_1000(const MSet_Token set __attribute__((unused))) {
  return 1000.0;
}

double fixed_rate_1(const MSet_Token set __attribute__((unused))) {
  return 1.0;
}

void on_state_change(struct DAPState *state);

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count);

Token createToken(const char* token);

int main(int argc, char *argv[]) {
  printf("Gossip simulation\n");
  Token a = createToken("a");
  Token b = createToken("b");
  if (a == NULL || b == NULL) {
      perror("Errore allocazione memoria");
      free(a);
      free(b);
      exit(EXIT_FAILURE);
  }
  /* 1) a|a --1_000--> a */
  Token in_tokens1[] = { a, a };
  MSet_Token preconditions1 = { in_tokens1, ARRAY_LEN(in_tokens1) };
  Token out_places1[] = { a };
  MSet_Token effects1 = { out_places1, ARRAY_LEN(out_places1) };
  Rule rule = {
    .preconditions = preconditions1,
    .rate = &fixed_rate_1000,
    .effects = effects1,
    .msg = NULL
  };
  /* 2) a --1--> a|^a */
  Token in_tokens2[] = { a };
  MSet_Token preconditions2 = { in_tokens2, ARRAY_LEN(in_tokens2) };
  Token out_places2[] = { a };
  MSet_Token effects2 = { out_places2, ARRAY_LEN(out_places2) };
  Rule rule2 = {
    .preconditions = preconditions2,
    .rate = &fixed_rate_1,
    .effects = effects2,
    .msg = a
  };
  /* 3) a|b --2--> a|b|^b */
  Token in_tokens3[] = { a, b };
  MSet_Token preconditions3 = { in_tokens3, ARRAY_LEN(in_tokens3) };
  Token out_places3[] = { a, b };
  MSet_Token effects3 = { out_places3, ARRAY_LEN(out_places3) };
  Rule rule3 = {
    .preconditions = preconditions3,
    .rate = &fixed_rate_1,
    .effects = effects3,
    .msg = b
  };
  /* 4) b|b --1000--> b */
  Token in_tokens4[] = { b, b };
  MSet_Token preconditions4 = { in_tokens4, ARRAY_LEN(in_tokens4) };
  Token out_places4[] = { b };
  MSet_Token effects4 = { out_places4, ARRAY_LEN(out_places4) };
  Rule rule4 = {
    .preconditions = preconditions4,
    .rate = &fixed_rate_1000,
    .effects = effects4,
    .msg = NULL
  };
  Rule rules[] = { rule, rule2, rule3, rule4 };
  /* State */
  int port = atoi(argv[1]);
  struct DAPState *initial_state;
  if (port == 2550) {
    Token initial_tokens[] = { a };
    initial_state = createDAPState(initial_tokens, ARRAY_LEN(initial_tokens));
  } else if (port == 2553) {
    Token initial_tokens[] = { b };
    initial_state = createDAPState(initial_tokens, ARRAY_LEN(initial_tokens));
  } else {
    initial_state = createDAPState(NULL, 0);
  }
  /* Neighborhood */
  int neighbours_size = argc - 2;
  Neighbour neighbours[neighbours_size];
  for (int i = 0; i < neighbours_size; i++) {
    neighbours[i] = argv[i + 2];
  }
  MSet_Neighbour neighborhood = { neighbours, ARRAY_LEN(neighbours) };
  /* Simulation */
  launch_simulation(rules, ARRAY_LEN(rules), initial_state, atoi(argv[1]), &neighborhood, &on_state_change);
  free(initial_state);
  return 0;
}

Token createToken(const char* token) {
  Token t = malloc(sizeof(struct TokenImpl));
  if (t == NULL) {
      perror("Allocation memory error on token");
      exit(EXIT_FAILURE);
  }
  t->token = strdup(token);
  return t;
}

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count) {
  MSet_Token initial_with_token = { initial_tokens, token_count };
  struct DAPState *state = malloc(sizeof(struct DAPState));
  if (state == NULL) {
      perror("Allocation memory error on state");
      exit(EXIT_FAILURE);
  }
  state->tokens = initial_with_token;
  state->msg = NULL;
  return state;
}

#define COLOR_RESET   "\033[0m"
#define COLOR_GREEN   "\033[32m"
#define COLOR_YELLOW  "\033[33m"
#define COLOR_CYAN    "\033[36m"
#define COLOR_MAGENTA "\033[35m"

void on_state_change(struct DAPState *state) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    struct tm *lt = localtime(&tv.tv_sec);
    printf(COLOR_CYAN "[C][⏰] %02d:%02d:%02d.%03d" COLOR_RESET "\n",
      lt->tm_hour, lt->tm_min, lt->tm_sec, tv.tv_usec / 1000);
    printf(COLOR_GREEN "[C][📦] State Tokens: " COLOR_RESET);
    if (state->tokens.size > 0) {
        printf(COLOR_YELLOW "{ ");
        for (size_t i = 0; i < state->tokens.size; i++) {
            printf("%s", state->tokens.elements[i]->token);
            if (i < state->tokens.size - 1) {
                printf(COLOR_MAGENTA " | " COLOR_YELLOW);
            }
        }
        printf(" }" COLOR_RESET "\n");
    } else {
        printf(COLOR_YELLOW "{ }" COLOR_RESET "\n");
    }
    if (state->msg != NULL) {
        printf(COLOR_GREEN "[C][💬] Message: " COLOR_RESET "\"%s\"\n", state->msg->token);
    } else {
        printf(COLOR_GREEN "[C][💬] Message: " COLOR_RESET "Nessun messaggio disponibile.\n");
    }
    printf(COLOR_CYAN "----------------------------------------" COLOR_RESET "\n");
}
