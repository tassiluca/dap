#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

struct TokenImpl {
    char *token;
};

unsigned char* serialize_fn(void *data, size_t *out_size) {
    struct TokenImpl *token = data;
    int str_len = strlen(token->token) + 1;
    *out_size = str_len;
    unsigned char *bytes = malloc(str_len);
    if (bytes == NULL) {
        perror("Allocation memory error on bytes");
        return NULL;
    }
    memcpy(bytes, token->token, str_len);
    return bytes;
}

void* deserialize_fn(const unsigned char *bytes, int size) {
    if (size <= 0) {
        fprintf(stderr, "Invalid deserialize size\n");
        return NULL;
    }
    Token token = malloc(sizeof(struct TokenImpl));
    if (token == NULL) {
        perror("Allocation memory error on token");
        return NULL;
    }
    token->token = (char*)malloc(size);
    if (token->token == NULL) {
        perror("Allocation memory error on token");
        free(token);
        return NULL;
    }
    memcpy(token->token, bytes, size);
    return token;
}

double fixed_rate_1000(MSet_Token *set) {
  return set == NULL ? 0.0 : 1000.0;
}

double fixed_rate_1(MSet_Token *set) {
  return set == NULL ? 0.0 : 1.0;
}

void on_state_change(struct DAPState *state);

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count);

Token createToken(const char* token);

int main(void) {
    printf("Gossip simulation\n");
    Token a = createToken("a");
    int res = register_codec("Token", serialize_fn, deserialize_fn);
    printf("Registered tokens: %s\n", !res ? "yes" : "no");
    Token newA = use_just_for_fun(a);
    printf("Real token %s\n", a->token);
    printf("Using token: %s\n", newA->token);
    free(a);
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
  MSet_Token *initial_with_token = malloc(sizeof(MSet_Token));
  if (initial_with_token == NULL) {
      perror("Allocation memory error on initial_with_token");
      exit(EXIT_FAILURE);
  }
  initial_with_token->elements = initial_tokens;
  initial_with_token->size = token_count;
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
    if (state->tokens->size > 0) {
        printf(COLOR_YELLOW "{ ");
        for (size_t i = 0; i < state->tokens->size; i++) {
            printf("%s", state->tokens->elements[i]->token);
            if (i < state->tokens->size - 1) {
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
