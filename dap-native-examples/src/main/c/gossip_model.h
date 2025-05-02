#ifndef GOSSIP_MODEL_H
#define GOSSIP_MODEL_H

#ifdef __cplusplus
extern "C" {
#endif

  #include "../../../lib/dap.h"

  typedef struct {
    char *name;
    int device_id;
  } TokenImpl;

  Token createToken(const char *token, int device_id);

  const char *serialize(Token token);

  Token deserialize(const char *buffer);

  bool equals(Token t1, Token t2);

  void print_token(Token t);

#ifdef __cplusplus
}
#endif

#endif
