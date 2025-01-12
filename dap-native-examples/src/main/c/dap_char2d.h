#ifndef DAP_CHAR2D_H
#define DAP_CHAR2D_H

#include "dap.h"

/* A place in a DAP mode as just a character */
struct Place {
    char p;
};

/* An ID placed in a 2-dimensional space */
struct Id {
    int x;
    int y;
};

Id create_id(int x, int y) {
    Id id = malloc(sizeof(struct Id));
    id->x = x;
    id->y = y;
    return id;
}

#endif
