#ifndef LIBCOUNTER_H
#define LIBCOUNTER_H

int ScalaNativeInit(void);
long addLongs(long, long);
int libcounter_addInts(int, int);
int libcounter_current_count();
void libcounter_set_counter(int);

#endif
