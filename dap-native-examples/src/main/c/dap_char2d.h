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

#endif
