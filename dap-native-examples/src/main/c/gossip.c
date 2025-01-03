#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

double rate(MultiSet *set) {
    if (set == NULL) {
        return 0.0;
    }
    size_t total = 0;
    for (size_t i = 0; i < set->len; i++) {
        total += strlen(set->elements[i]);
    }
    return (double) total;
}

int main(int argc, char *argv[]) {
    printf("Hello world from grossip source!\n");
    MultiSet preconditions = {(const char*[]){"a", "b"}, 2};
    MultiSet effects = {(const char*[]){"c", "d"}, 2};
    MultiSet messages = {(const char*[]){"e", "f"}, 2};
    Rule* rule = stringed_rule(&preconditions, rate, &effects, &messages);
    printf("Rule created successfully!\n");
    return 0;
}
