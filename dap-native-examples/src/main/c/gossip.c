#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap.h"
#include "style.h"

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

void printId(const Id id) {
    printf("Id={(%d, %d)}\n", id->x, id->y);
}

void printPlace(const Place place) {
    printf("Place={%c}\n", place->p);
}

void printGridState(const State state, int grid_rows, int grid_cols) {
    char*** grid = malloc(grid_rows * sizeof(char**));
    char*** messageGrid = malloc(grid_rows * sizeof(char**));
    for (int i = 0; i < grid_rows; i++) {
        grid[i] = malloc(grid_cols * sizeof(char*));
        messageGrid[i] = malloc(grid_cols * sizeof(char*));
        for (int j = 0; j < grid_cols; j++) {
            grid[i][j] = NULL;
            messageGrid[i][j] = NULL;
        }
    }
    // Process tokens
    for (size_t i = 0; i < state->tokens->size; i++) {
        Token token = state->tokens->elements[i];
        int row = token.id->x;
        int col = token.id->y;
        if (row >= grid_rows || col >= grid_cols) {
            continue; // Skip tokens out of bounds
        }
        // Concatenate token places into grid[row][col]
        if (grid[row][col] == NULL) {
            grid[row][col] = malloc(2);
            grid[row][col][0] = token.place->p;
            grid[row][col][1] = '\0';
        } else {
            size_t len = strlen(grid[row][col]);
            grid[row][col] = realloc(grid[row][col], len + 2);
            grid[row][col][len] = token.place->p;
            grid[row][col][len + 1] = '\0';
        }
    }
    // Process messages
    for (size_t i = 0; i < state->messages->size; i++) {
        const Token token = state->messages->elements[i];
        int row = token.id->x;
        int col = token.id->y;
        if (row >= grid_rows || col >= grid_cols) {
            continue; // Skip messages out of bounds
        }
        // Concatenate message places into messageGrid[row][col]
        if (messageGrid[row][col] == NULL) {
            messageGrid[row][col] = malloc(2);
            messageGrid[row][col][0] = token.place->p;
            messageGrid[row][col][1] = '\0';
        } else {
            size_t len = strlen(messageGrid[row][col]);
            messageGrid[row][col] = realloc(messageGrid[row][col], len + 2);
            messageGrid[row][col][len] = token.place->p;
            messageGrid[row][col][len + 1] = '\0';
        }
    }
    // Determine the maximum width required for alignment
    int max_token_width = 1; // Minimum width for a single character token
    int max_message_width = 1; // Minimum width for a single character message
    for (int i = 0; i < grid_rows; i++) {
        for (int j = 0; j < grid_cols; j++) {
            if (grid[i][j] && (int)strlen(grid[i][j]) > max_token_width) {
                max_token_width = strlen(grid[i][j]);
            }
            if (messageGrid[i][j] && (int)strlen(messageGrid[i][j]) > max_message_width) {
                max_message_width = strlen(messageGrid[i][j]);
            }
        }
    }
    // Print the grid with dynamic padding and colors
    printf("Grid:\n");
    for (int i = 0; i < grid_rows; i++) {
        for (int j = 0; j < grid_cols; j++) {
            const char* places = grid[i][j] ? grid[i][j] : "·"; // Replace empty tokens with "·"
            const char* messages = messageGrid[i][j] ? messageGrid[i][j] : ""; // Empty if no messages
            // Print tokens in green, messages in blue
            printf(GREEN"%-*s"RESET"("BLUE"%-*s"RESET") ", max_token_width, places, max_message_width, messages);
        }
        printf("\n");
    }
    for (int i = 0; i < grid_rows; i++) {
        for (int j = 0; j < grid_cols; j++) {
            free(grid[i][j]);
            free(messageGrid[i][j]);
        }
        free(grid[i]);
        free(messageGrid[i]);
    }
    free(grid);
    free(messageGrid);
}

Id create_id(int x, int y) {
    Id id = malloc(sizeof(Id));
    id->x = x;
    id->y = y;
    return id;
}

MSet_Id *create_grid_of_ids(int rows, int cols) {
    MSet_Id *ids = malloc(sizeof(MSet_Id));
    ids->elements = malloc(rows * cols * sizeof(Id));
    ids->size = rows * cols;
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            ids->elements[i * cols + j] = create_id(i, j);
        }
    }
    return ids;
}

Neighbors *grid_neighbors(MSet_Id* ids, int rows, int cols) {
    int max_neighbors = 4;
    int dx[] = { -1, 1, 0, 0 };
    int dy[] = { 0, 0, -1, 1 };
    Neighbors *all_neighbors = malloc(rows * cols * sizeof(Neighbors));
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            all_neighbors[i * cols + j].point = ids->elements[i * cols + j];
            all_neighbors[i * cols + j].neighbors = malloc(sizeof(MSet_Id));
            // the number of neighbors of each id depends on their position (in the corners, it will be 2)
            all_neighbors[i * cols + j].neighbors->elements = malloc(max_neighbors * sizeof(Id));
            all_neighbors[i * cols + j].neighbors->size = 0;
            for (int k = 0; k < max_neighbors; ++k) {
                int nx = i + dx[k];
                int ny = j + dy[k];
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                    all_neighbors[i * cols + j].neighbors->elements[all_neighbors[i * cols + j].neighbors->size++] = ids->elements[nx * cols + ny];
                }
            }
        }
    }
    return all_neighbors;
}

int main(void) {
    int rows = 5, cols = 5;
    MSet_Id* ids = create_grid_of_ids(rows, cols);
    Neighbors *all_neighbors = grid_neighbors(ids, rows, cols);

    // a|a --1_000--> a
    Place in_places1_ptrs[] = { &a, &a };
    MSet_Place preconditions1 = { in_places1_ptrs, 2 };
    Place out_places1_ptrs[] = { &a };
    MSet_Place effects1 = { out_places1_ptrs, 1 };
    MSet_Place messages1 = { NULL, 0 };
    Rule rule = {
        .preconditions = &preconditions1,
        .rate = &fixed_rate_1000,
        .effects = &effects1,
        .messages = &messages1
    };
    // a --1--> a|^a
    Place in_places2_ptrs[] = { &a };
    MSet_Place preconditions2 = { in_places2_ptrs, 1 };
    Place out_places2_ptrs[] = { &a };
    MSet_Place effects2 = { out_places2_ptrs, 1 };
    Place messages2_ptrs[] = { &a };
    MSet_Place messages2 = { messages2_ptrs, 1 };
    Rule rule2 = { &preconditions2, &fixed_rate_1, &effects2, &messages2 };

    Rule rules[] = { rule, rule2 };

    DAP dap = create_dap_from_rules(rules, 2);
    CTMC ctmc = dap_to_ctmc(&dap);
    printf("DAP and CTMC created successfully!\n\n");

    Id leftUpperCorner = all_neighbors[0].point;
    Token initialToken = { leftUpperCorner, &a };
    MSet_Token initial_set = { &initialToken, 1 };
    MSet_Token initial_msgs = { NULL, 0 };
    struct State initialState = { &initial_set, &initial_msgs };

    printf("Initial state ");
    printGridState(&initialState, rows, cols);

    printf("Simulating...\n");
    Trace* trace = simulate_dap(
        &ctmc,
        &initialState,
        all_neighbors,
        rows * cols,
        250
    );

    if (trace == NULL) {
        printf("Error: trace is NULL\n");
        return 1;
    }

    printf("\n\n\n\nTrace events: %zu\n", trace->len);
    for (size_t i = 0; i < trace->len; i++) {
        printf("Event %zu", i);
        printf("  time=%f, ", trace->events[i].time);
        printGridState(trace->events[i].state, rows, cols);
        printf("\n\n");
    }

    printf("OK!\n");
    return 0;
}
