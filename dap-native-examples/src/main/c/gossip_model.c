#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "gossip_model.h"

Token createToken(const char* token, const int device_id) {
    TokenImpl *t = malloc(sizeof(TokenImpl));
    if (!t) return NULL;
    t->name = strdup(token);
    t->device_id = device_id;
    size_t out_size = 0;
    uint8_t *buffer = serialize(t, &out_size);
    if (!buffer) return NULL;
    SerializedData *sd = malloc(sizeof(SerializedData));
    if (!sd) return NULL;
    sd->data = buffer;
    sd->size = out_size;
    return sd;
}

void free_token(TokenImpl* t) {
    if (!t) return;
    free(t->name);
    free(t);
}

uint8_t* serialize(TokenImpl *token, size_t *out_size) {
    if (!token) return NULL;
    size_t name_len = strlen(token->name) + 1;
    *out_size = name_len + sizeof(int);
    uint8_t *buffer = malloc(*out_size);
    if (!buffer) return NULL;
    memcpy(buffer, &token->device_id, sizeof(int));
    memcpy(buffer + sizeof(int), token->name, name_len);
    return buffer;
}

TokenImpl* deserialize(const uint8_t* buffer) {
    if (!buffer) return NULL;
    TokenImpl* token = malloc(sizeof(TokenImpl));
    if (!token) return NULL;
    memcpy(&token->device_id, buffer, sizeof(int));
    token->name = strdup((char*)(buffer + sizeof(int)));
    if (!token->name) {
        free(token);
        return NULL;
    }
    return token;
}

int are_equals(Token t1, Token t2) {
    if (t1->size != t2->size) {
        return 0;
    }
    TokenImpl* real_token_1 = deserialize(t1->data);
    TokenImpl* real_token_2 = deserialize(t2->data);
    if (!real_token_1 || !real_token_2) return 0;
    int result = strcmp(real_token_1->name, real_token_2->name) == 0;
    free(real_token_1->name);
    free(real_token_1);
    free(real_token_2->name);
    free(real_token_2);
    return result;
}

void print_token(Token t) {
    if (!t) {
        printf("No tokens");
        return;
    }
    TokenImpl *token = deserialize(t->data);
    if (!token) {
        printf("No token");
        return;
    }
    printf("%s - %d", token->name, token->device_id);
    free_token(token);
}
