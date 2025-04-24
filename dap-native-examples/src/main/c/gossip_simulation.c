#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#include "gossip_model.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

void on_state_change(const struct DAPState *state);

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count);

int main(int argc, char *argv[])
{
    if (argc < 2)
    {
        perror(
            "Usage: gossip <port> <neighbor> [<neighbor>...]\n"
            "\n"
            "\t <port>: the port the node will listen on for incoming neighbors connections\n"
            "\t <neighbor>: endpoint in the form of <hostname>:<port> of the neighbor node");
        return 1;
    }
    int port = atoi(argv[1]);
    printf("===================================\n");
    printf("== C Program - Gossip simulation ==\n");
    printf("===================================\n");
    /* The tokens used in the simulation */
    Token a = createToken("a", port);
    Token b = createToken("b", port);
    /* 1) a|a --1_000--> a */
    Token in_tokens1[] = {a, a};
    MSet_Token *preconditions1 = MSet_Token_of(in_tokens1, ARRAY_LEN(in_tokens1));
    Token out_places1[] = {a};
    MSet_Token *effects1 = MSet_Token_of(out_places1, ARRAY_LEN(out_places1));
    Rule rule = {
        .preconditions = preconditions1,
        .rate = 1000,
        .effects = effects1,
        .msg = NULL};
    /* 2) a --1--> a|^a */
    Token in_tokens2[] = {a};
    MSet_Token *preconditions2 = MSet_Token_of(in_tokens2, ARRAY_LEN(in_tokens2));
    Token out_places2[] = {a};
    MSet_Token *effects2 = MSet_Token_of(out_places2, ARRAY_LEN(out_places2));
    Rule rule2 = {
        .preconditions = preconditions2,
        .rate = 1,
        .effects = effects2,
        .msg = a};
    /* 3) a|b --2--> a|b|^b */
    Token in_tokens3[] = {a, b};
    MSet_Token *preconditions3 = MSet_Token_of(in_tokens3, ARRAY_LEN(in_tokens3));
    Token out_places3[] = {a, b};
    MSet_Token *effects3 = MSet_Token_of(out_places3, ARRAY_LEN(out_places3));
    Rule rule3 = {
        .preconditions = preconditions3,
        .rate = 1,
        .effects = effects3,
        .msg = b};
    /* 4) b|b --1000--> b */
    Token in_tokens4[] = {b, b};
    MSet_Token *preconditions4 = MSet_Token_of(in_tokens4, ARRAY_LEN(in_tokens4));
    Token out_places4[] = {b};
    MSet_Token *effects4 = MSet_Token_of(out_places4, ARRAY_LEN(out_places4));
    Rule rule4 = {
        .preconditions = preconditions4,
        .rate = 1000,
        .effects = effects4,
        .msg = NULL};
    Rule rules[] = {rule, rule2, rule3, rule4};
    /* State */
    struct DAPState *initial_state;
    if (port == 2550)
    {
        Token initial_tokens[] = {a};
        initial_state = createDAPState(initial_tokens, ARRAY_LEN(initial_tokens));
    }
    else if (port == 2553)
    {
        Token initial_tokens[] = {b};
        initial_state = createDAPState(initial_tokens, ARRAY_LEN(initial_tokens));
    }
    else
    {
        initial_state = createDAPState(NULL, 0);
    }
    /* Neighborhood. */
    int neighbours_size = argc - 2;
    Neighbour neighbours[neighbours_size];
    for (int i = 0; i < neighbours_size; i++)
    {
        Neighbour n = {.name = argv[i + 2]};
        neighbours[i] = n;
    }
    /* Launch simulation. */
    launch_simulation(rules, ARRAY_LEN(rules), initial_state, port, neighbours, ARRAY_LEN(neighbours), &on_state_change, are_equals);
    sleep(30);
    free(initial_state);
    return 0;
}

struct DAPState *createDAPState(Token *initial_tokens, size_t token_count)
{
    MSet_Token *initial_with_token = malloc(sizeof(MSet_Token));
    if (initial_with_token == NULL)
    {
        perror("Allocation memory error on initial_with_token");
        exit(EXIT_FAILURE);
    }
    initial_with_token->elements = initial_tokens;
    initial_with_token->size = token_count;
    struct DAPState *state = malloc(sizeof(struct DAPState));
    if (state == NULL)
    {
        perror("Allocation memory error on state");
        exit(EXIT_FAILURE);
    }
    state->tokens = initial_with_token;
    state->msg = NULL;
    return state;
}

void on_state_change(const struct DAPState *state)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    const struct tm *lt = localtime(&tv.tv_sec);
    printf("\n----------------------------------------\n");
    printf("[C] %02d:%02d:%02d.%03d \n", lt->tm_hour, lt->tm_min, lt->tm_sec, tv.tv_usec / 1000);
    printf("[C] State Tokens: ");
    printf("{ ");
    for (size_t i = 0; i < state->tokens->size; i++)
    {
        print_token(state->tokens->elements[i]);
        if (i < state->tokens->size - 1)
        {
            printf(" | ");
        }
    }
    printf("}\n");
    printf("[C] Message: ");
    print_token(state->msg);
    printf("\n----------------------------------------\n");
}
