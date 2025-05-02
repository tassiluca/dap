#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "gossip_model.h"

Token createToken(const char *token, const int device_id) {
  TokenImpl *t = malloc(sizeof(TokenImpl));
  if (!t) {
    return NULL;
  }
  t->name = strdup(token);
  t->device_id = device_id;
  return t;
}

void free_token(TokenImpl *t) {
  if (!t) {
    return;
  }
  free(t->name);
  free(t);
}

const char *serialize(Token token) {
  if (!token) {
    return NULL;
  }
  TokenImpl *token_impl = (TokenImpl *)token;
  // Estimate enough space: 30 for JSON syntax + strlen(name) + possible number length
  size_t buffer_size = 30 + strlen(token_impl->name) + 20;
  char *json = malloc(buffer_size);
  if (json == NULL) {
    return NULL;
  }
  snprintf(json, buffer_size, "{\"name\":\"%s\",\"device_id\":%d}", token_impl->name, token_impl->device_id);
  return json;
}

Token deserialize(const char *buffer) {
  if (buffer == NULL) {
    return NULL;
  }
  TokenImpl *token = malloc(sizeof(TokenImpl));
  if (token == NULL) {
    return NULL;
  }
  char name[256];
  int device_id;
  if (sscanf(buffer, "{\"name\":\"%255[^\"]\",\"device_id\":%d}", name, &device_id) != 2) {
    free(token);
    return NULL;
  }
  token->name = strdup(name);
  token->device_id = device_id;
  return token;
}

bool equals(Token t1, Token t2) {
  if (!t1 || !t2) {
    return false;
  }
  TokenImpl *real_token_1 = (TokenImpl *)t1;
  TokenImpl *real_token_2 = (TokenImpl *)t2;
  if (!real_token_1 || !real_token_2 || !real_token_1->name || !real_token_2->name) {
    return false;
  }
  return strcmp(real_token_1->name, real_token_2->name) == 0;
}

void print_token(Token t) {
  if (!t) {
    return;
  }
  TokenImpl *token = (TokenImpl *)t;
  if (!token) {
    return;
  }
  printf("%s - %d", token->name, token->device_id);
}
