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
  const char *json_template = "{\"name\":\"%s\",\"device_id\":%d}";
  int json_length = snprintf(NULL, 0, json_template, token_impl->name, token_impl->device_id);
  if (json_length < 0) {
    return NULL;
  }
  int json_lenght_with_terminator = json_length + 1;
  char *json = malloc(json_lenght_with_terminator);
  if (!json) {
    return NULL;
  }
  snprintf(json, json_lenght_with_terminator, json_template, token_impl->name, token_impl->device_id);
  return json;
}

Token deserialize(const char *buffer) {
  if (!buffer) {
    return NULL;
  }
  TokenImpl *token = malloc(sizeof(TokenImpl));
  if (!token) {
    return NULL;
  }
  char name[256];
  int device_id;
  int matched = sscanf(buffer, "{\"name\":\"%255[^\"]\",\"device_id\":%d}", name, &device_id);
  if (matched != 2) { // Check if both name and device_id were successfully parsed
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
  printf("  Token { name: %s, device ID: %d }", token->name, token->device_id);
}
