#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include <assert.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

typedef struct {
    char* name;
    int device_id;
} TokenImpl;

void on_state_change(struct DAPState *state);

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count);

Token createToken(const char* token, int port);

int are_equals(SerializedData* d1, SerializedData* d2);

int main(int argc, char *argv[]) {
    if (argc < 2) {
        perror(
            "Usage: gossip <port> <neighbor> [<neighbor>...]\n"
            "\n"
            "\t <port>: the port the node will listen on for incoming neighbors connections\n"
            "\t <neighbor>: endpoint in the form of <hostname>:<port> of the neighbor node"
        );
        return 1;
    }
    int port = atoi(argv[1]);
    printf("Gossip simulation\n");
    Token a = createToken("a", port);
    Token b = createToken("b", port);
    /* 1) a|a --1_000--> a */
    Token in_tokens1[] = { a, a };
    MSet_Token preconditions1 = { in_tokens1, ARRAY_LEN(in_tokens1) };
    Token out_places1[] = { a };
    MSet_Token effects1 = { out_places1, ARRAY_LEN(out_places1) };
    Rule rule = {
        .preconditions = &preconditions1,
        .rate = 1000,
        .effects = &effects1,
        .msg = NULL
    };
    /* 2) a --1--> a|^a */
    Token in_tokens2[] = { a };
    MSet_Token preconditions2 = { in_tokens2, ARRAY_LEN(in_tokens2) };
    Token out_places2[] = { a };
    MSet_Token effects2 = { out_places2, ARRAY_LEN(out_places2) };
    Rule rule2 = {
        .preconditions = &preconditions2,
        .rate = 1,
        .effects = &effects2,
        .msg = a
    };
    /* 3) a|b --2--> a|b|^b */
    Token in_tokens3[] = { a, b };
    MSet_Token preconditions3 = { in_tokens3, ARRAY_LEN(in_tokens3) };
    Token out_places3[] = { a, b };
    MSet_Token effects3 = { out_places3, ARRAY_LEN(out_places3) };
    Rule rule3 = {
        .preconditions = &preconditions3,
        .rate = 1,
        .effects = &effects3,
        .msg = b
    };
    /* 4) b|b --1000--> b */
    Token in_tokens4[] = { b, b };
    MSet_Token preconditions4 = { in_tokens4, ARRAY_LEN(in_tokens4) };
    Token out_places4[] = { b };
    MSet_Token effects4 = { out_places4, ARRAY_LEN(out_places4) };
    Rule rule4 = {
        .preconditions = &preconditions4,
        .rate = 1000,
        .effects = &effects4,
        .msg = NULL
    };
    Rule rules[] = { rule, rule2, rule3, rule4 };
    MSet_Rule all_rules = { rules, ARRAY_LEN(rules) };
    /* State */
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
    /* Capabilities */
    // register_serde("Token", serialize_fn, deserialize_fn);
    register_equatable(are_equals);
    /* Simulation */
    printf("Starting gossip simulation\n");
    launch_simulation(&all_rules, initial_state, port, &neighborhood, &on_state_change);
    free(initial_state);
    return 0;
}

uint8_t* serialize(TokenImpl *token, size_t *out_size) {
    if (!token) return NULL;
    size_t name_len = strlen(token->name) + 1;
    *out_size = name_len + sizeof(int);
    uint8_t *buffer = (uint8_t*)malloc(*out_size);
    if (!buffer) return NULL;
    memcpy(buffer, &token->device_id, sizeof(int));
    memcpy(buffer + sizeof(int), token->name, name_len);
    return buffer;
}

TokenImpl* deserialize(const uint8_t* buffer, size_t size) {
    if (!buffer) return NULL;
    TokenImpl* token = (TokenImpl*)malloc(sizeof(TokenImpl));
    if (!token) return NULL;
    memcpy(&token->device_id, buffer, sizeof(int));
    token->name = strdup((char*)(buffer + sizeof(int)));
    if (!token->name) {
        perror("strdup");
        free(token);
        return NULL;
    }
    return token;
}

int are_equals(SerializedData* d1, SerializedData* d2) {
    if (d1->size != d2->size) {
        return 0;  // False (sizes are different)
    }
    TokenImpl* t1 = deserialize(d1->data, d1->size);
    TokenImpl* t2 = deserialize(d2->data, d2->size);
    return /*t1->device_id == t2->device_id &&*/ strcmp(t1->name, t2->name) == 0;
}

Token createToken(const char* token, int id) {
    TokenImpl *t = malloc(sizeof(TokenImpl));
    if (!t) return NULL;
    t->name = strdup(token);
    t->device_id = id;
    size_t out_size = 0;
    uint8_t *buffer = serialize(t, &out_size);
    if (!buffer) return NULL;
    SerializedData *sd = malloc(sizeof(SerializedData));
    if (!sd) return NULL;
    sd->data = buffer;
    sd->size = out_size;
    return sd;
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
            TokenImpl *token = deserialize(state->tokens->elements[i]->data, state->tokens->size);
            printf("%s - %d", token->name, token->device_id);
            if (i < state->tokens->size - 1) {
                printf(COLOR_MAGENTA " | " COLOR_YELLOW);
            }
        }
        printf(" }" COLOR_RESET "\n");
    } else {
        printf(COLOR_YELLOW "{ }" COLOR_RESET "\n");
    }
    if (state->msg != NULL) {
        TokenImpl *msg = deserialize(state->msg->data, state->msg->size);
        printf(COLOR_GREEN "[C][💬] Message: " COLOR_RESET "\"%s - %d\"\n", msg->name, msg->device_id);
    } else {
        printf(COLOR_GREEN "[C][💬] Message: " COLOR_RESET "No message.\n");
    }
    printf(COLOR_CYAN "----------------------------------------" COLOR_RESET "\n");
}
