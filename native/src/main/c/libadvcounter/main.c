#include "libadvcounter.h"
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char** argv) {
    assert(ScalaNativeInit() == 0);
    Counter* counter = create_counter();
    if (counter == NULL) {
        fprintf(stderr, "Failed to create counter\n");
        return 1;
    }
    fprintf(stdout, "Incrementing counter\n");
    Counter* counter2 = inc_counter(counter, 10);
    fprintf(stdout, "Counter value: %d\n", counter_value(counter2));
    fprintf(stdout, "Decrementing counter\n");
    Counter* counter3 = dec_counter(counter2, 5);
    fprintf(stdout, "Counter value: %d\n", counter_value(counter3));
    free(counter);
    free(counter2);
    free(counter3);
    return 0;
}
