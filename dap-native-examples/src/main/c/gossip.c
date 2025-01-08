#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap.h"

#define ARRAY_LEN(arr) (sizeof(arr) / sizeof(arr[0]))

double fixed_rate_1000(const MSet_Place *set) {
    return set == NULL ? 0.0 : 1000.0;
}

double fixed_rate_1(const MSet_Place *set) {
    return set == NULL ? 0.0 : 1.0;
}

struct Place {
    char p;
};

static struct Place a = { 'a' };

struct Id {
    int x;
    int y;
};

void printId(const Id* id) {
    printf("Id={(%d, %d)} [@ %p]\n", id->x, id->y, id);
}

void printPlace(const Place* place) {
    printf("Place={%c} [@ %p]\n", place->p, place);
}

void printState(const State* state) {
    printf("State={\n");
    printf("  Tokens @ %p of size=%d\n", state->tokens, state->tokens->size);
    printf("  Messages @ %p of size=%d\n", state->messages, state->messages->size);
    printf("  Tokens={\n");
    for (size_t i = 0; i < state->tokens->size; i++) {
        const Token* token = state->tokens->elements[i];
        printf("    Token={\n");
        printf("      "); printId(token->id);
        printf("      "); printPlace(token->place);
        printf("    }\n");
    }
    printf("  }\n");
    printf("  Messages={\n");
    for (size_t i = 0; i < state->messages->size; i++) {
        const Token* token = state->messages->elements[i];
        printf("    Token={\n");
        printf("      "); printId(token->id);
        printf("      "); printPlace(token->place);
        printf("    }\n");
    }
    printf("  }\n");
//    printf("  Neighbors={\n");
//    for (size_t i = 0; i < state->neighbors_size; i++) {
//        const Neighbors* neighbors = &state->neighbors[i];
//        printf("    Neighbors={\n");
//        printf("      Id="); printId(neighbors->point);
//        printf("      Neighbors={\n");
//        for (size_t j = 0; j < neighbors->neighbors->size; j++) {
//            const Id* neighbor = neighbors->neighbors->elements[j];
//            printf("        Neighbor="); printId(neighbor);
//        }
//        printf("      }\n");
//        printf("    }\n");
//    }
    printf("}\n");
}

int main(void) {
    /************ DEVICES AND THEIR (hardcoded, at least for the moment) NEIGHBORS ************/
    const Id leftUpperCorner = { 0, 0 };
    const Id rightUpperCorner = { 0, 1 };
    const Id leftLowerCorner = { 1, 0 };
    const Id rightLowerCorner = { 1, 1 };

    printf("Left upper corner"); printId(&leftUpperCorner);
    printf("Right upper corner"); printId(&rightUpperCorner);
    printf("Left lower corner"); printId(&leftLowerCorner);
    printf("Right lower corner"); printId(&rightLowerCorner);

    const Id* neighborsLeftUpperCorner[] = { &rightUpperCorner, &leftLowerCorner };
    const Id* neighborsRightUpperCorner[] = { &leftUpperCorner, &rightLowerCorner };
    const Id* neighborsLeftLowerCorner[] = { &rightLowerCorner, &leftUpperCorner };
    const Id* neighborsRightLowerCorner[] = { &leftLowerCorner, &rightUpperCorner };

    const MSet_Id neighborsLeftUpperCornerSet = { neighborsLeftUpperCorner, 2 };
    const MSet_Id neighborsRightUpperCornerSet = { neighborsRightUpperCorner, 2 };
    const MSet_Id neighborsLeftLowerCornerSet = { neighborsLeftLowerCorner, 2 };
    const MSet_Id neighborsRightLowerCornerSet = { neighborsRightLowerCorner, 2 };

    Neighbors leftUpperCornerNeighbors = { &leftUpperCorner, &neighborsLeftUpperCornerSet };
    Neighbors rightUpperCornerNeighbors = { &rightUpperCorner, &neighborsRightUpperCornerSet };
    Neighbors leftLowerCornerNeighbors = { &leftLowerCorner, &neighborsLeftLowerCornerSet };
    Neighbors rightLowerCornerNeighbors = { &rightLowerCorner, &neighborsRightLowerCornerSet };
    Neighbors all_neighbors[] = { leftUpperCornerNeighbors, rightUpperCornerNeighbors, leftLowerCornerNeighbors, rightLowerCornerNeighbors };

    /*************************************** SIMULATION ***************************************/
    // a|a --1_000--> a
    const Place* in_places1_ptrs[] = { &a, &a };
    MSet_Place preconditions1 = { in_places1_ptrs, 2 };
    const Place* out_places1_ptrs[] = { &a };
    MSet_Place effects1 = { out_places1_ptrs, 1 };
    MSet_Place messages1 = { NULL, 0 };
    Rule rule = { &preconditions1, &fixed_rate_1000, &effects1, &messages1 };
    // a --1--> a|^a
    const Place* in_places2_ptrs[] = { &a };
    MSet_Place preconditions2 = { in_places2_ptrs, 1 };
    const Place* out_places2_ptrs[] = { &a };
    MSet_Place effects2 = { out_places2_ptrs, 1 };
    const Place* messages2_ptrs[] = { &a };
    MSet_Place messages2 = { messages2_ptrs, 1 };
    Rule rule2 = { &preconditions2, &fixed_rate_1, &effects2, &messages2 };

    Rule rules[] = { rule, rule2 };

    DAP* dap = create_dap_from_rules(rules, 2);
    CTMC* ctmc = dap_to_ctmc(dap);
    printf("DAP and CTMC created successfully!\n");

    Token initialToken = { &leftUpperCorner, &a };
    MSet_Token initial_set = {
        .elements = (const Token*[]){ &initialToken },
        .size = 1
    };
    MSet_Token initial_msgs = { NULL, 0 };
    State initialState = { &initial_set, &initial_msgs };
    printState(&initialState);

    printf("Simulating...\n");

    Trace* trace = simulate_dap(
        ctmc,
        &initialState,
        all_neighbors,
        4,
        10
    );

    if (trace == NULL) {
        printf("Error: trace is NULL\n");
        return 1;
    }

    printf("Trace @ %p\n", trace);
    printf("\n\n\n\nTrace events: %zu\n", trace->len);
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %u", i);
        printf("  time=%f, ", trace->events[i].time);
        printf("  state addr=%p, ", trace->events[i].state);
        printState(trace->events[i].state);
    }

    printf("OK!\n");
    return 0;
}
