#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap_char2d.h"
#include "dap_char2d_utils.c"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

double fixed_rate_1000(const MSet_Place *set) {
    return set == NULL ? 0.0 : 1000.0;
}

double fixed_rate_1(const MSet_Place *set) {
    return set == NULL ? 0.0 : 1.0;
}

static struct Place a = { 'a' };

int main(void) {
    int rows = 5, cols = 6, simulation_steps = 100;
    MSet_Id* ids = create_grid_of_ids(rows, cols);
    Neighbors *all_neighbors = grid_neighbors(ids, rows, cols);
    /* Create a DAP model with two rules */
    /* 1) a|a --1_000--> a */
    Place in_places1[] = { &a, &a };
    MSet_Place preconditions1 = { in_places1, ARRAY_LEN(in_places1) };
    Place out_places1[] = { &a };
    MSet_Place effects1 = { out_places1, ARRAY_LEN(out_places1) };
    MSet_Place messages1 = { NULL, 0 };
    Rule rule = {
        .preconditions = &preconditions1,
        .rate = &fixed_rate_1000,
        .effects = &effects1,
        .messages = &messages1
    };
    /* 2) a --1--> a|^a */
    Place in_places2[] = { &a };
    MSet_Place preconditions2 = { in_places2, ARRAY_LEN(in_places2) };
    Place out_places2[] = { &a };
    MSet_Place effects2 = { out_places2, ARRAY_LEN(out_places2) };
    Place sent_places[] = { &a };
    MSet_Place messages2 = { sent_places, ARRAY_LEN(sent_places) };
    Rule rule2 = {
        .preconditions = &preconditions2,
        .rate = &fixed_rate_1,
        .effects = &effects2,
        .messages = &messages2
    };
    Rule rules[] = { rule, rule2 };
    DAP dap = create_dap_from_rules(rules, ARRAY_LEN(rules));
    CTMC ctmc = dap_to_ctmc(&dap);
    printf("DAP and CTMC created successfully!\n");
    /* Actual semantics */
    Id leftUpperCorner = all_neighbors[0].point;
    Token initialToken = { leftUpperCorner, &a };
    MSet_Token initial_set = { &initialToken, 1 };
    MSet_Token initial_msgs = { NULL, 0 };
    struct State initialState = { &initial_set, &initial_msgs };
    printf("Initial state ");
    print_grid_state(&initialState, rows, cols);
    printf("\n\nSimulating...\n");
    Trace* trace = simulate_dap(
        &ctmc,
        &initialState,
        all_neighbors,
        rows * cols,
        simulation_steps
    );
    if (trace == NULL) {
        printf("Error: trace is NULL\n");
        return 1;
    }
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %zu", i);
        printf("  time=%f, ", trace->events[i].time);
        print_grid_state(trace->events[i].state, rows, cols);
        printf("\n\n");
    }
    printf("Simulation completed successfully!\n");
    free_ids(ids);
    free_neighbors(all_neighbors, rows, cols);
    free((void*)trace->events);
    free(trace);
    return 0;
}
