#ifndef LIBADVCOUNTER_H
#define LIBADVCOUNTER_H

typedef struct Counter Counter;

int ScalaNativeInit(void);
Counter* create_counter();
Counter* inc_counter(Counter*, int);
Counter* dec_counter(Counter*, int);
int counter_value(Counter*);

#endif
