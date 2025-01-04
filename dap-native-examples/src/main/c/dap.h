#ifndef LIBDAP_H
#define LIBDAP_H

typedef struct {
    const char* const* elements;
    size_t len;
} MultiSet;

typedef struct Rule Rule;

Rule* stringed_rule(MultiSet *preconditions, double (*rate)(MultiSet*), MultiSet *effects, MultiSet *messages);

#endif
