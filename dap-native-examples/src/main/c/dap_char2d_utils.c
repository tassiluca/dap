#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dap_char2d.h"

#define GREEN "\033[1;32m" // Green for tokens
#define BLUE  "\033[1;35m"  // Blue for messages
#define RESET "\033[0m"  // Reset color

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
            int idx = i * cols + j;
            all_neighbors[idx].point = ids->elements[idx];
            all_neighbors[idx].neighbors = malloc(sizeof(MSet_Id));
            // the number of neighbors of each id depends on their position (in the corners, it will be 2)
            all_neighbors[idx].neighbors->elements = malloc(max_neighbors * sizeof(Id));
            all_neighbors[idx].neighbors->size = 0;
            for (int k = 0; k < max_neighbors; ++k) {
                int nx = i + dx[k];
                int ny = j + dy[k];
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                    all_neighbors[idx].neighbors->elements[all_neighbors[idx].neighbors->size++] =
                        ids->elements[nx * cols + ny];
                }
            }
        }
    }
    return all_neighbors;
}

void processGridElement(char*** grid, const Token token, int rows, int cols) {
    int row = token.id->x;
    int col = token.id->y;
    if (row >= rows || col >= cols) {
        return; // Skip elements out of bounds
    }
    if (grid[row][col] == NULL) {
        grid[row][col] = malloc(2);
        grid[row][col][0] = token.place->p;
        grid[row][col][1] = '\0';
    } else {
        size_t len = strlen(grid[row][col]);
        char* new_str = malloc(len + 2);
        if (new_str) {
            strcpy(new_str, grid[row][col]);
            new_str[len] = token.place->p;
            new_str[len + 1] = '\0';
            free(grid[row][col]);
            grid[row][col] = new_str;
        }
    }
}

void initializeGrid(char*** grid, int rows, int cols) {
    for (int i = 0; i < rows; i++) {
        grid[i] = malloc(cols * sizeof(char*));
        for (int j = 0; j < cols; j++) {
            grid[i][j] = NULL;
        }
    }
}

void freeGrid(char*** grid, int rows, int cols) {
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            free(grid[i][j]);
        }
        free(grid[i]);
    }
    free(grid);
}

void printGridState(const State state, int rows, int cols) {
    // Allocate and initialize grids
    char*** grid = malloc(rows * sizeof(char**));
    char*** messageGrid = malloc(rows * sizeof(char**));
    initializeGrid(grid, rows, cols);
    initializeGrid(messageGrid, rows, cols);
    // Process tokens and messages
    for (size_t i = 0; i < state->tokens->size; i++) {
        processGridElement(grid, state->tokens->elements[i], rows, cols);
    }
    for (size_t i = 0; i < state->messages->size; i++) {
        processGridElement(messageGrid, state->messages->elements[i], rows, cols);
    }
    // Print the grid with fixed-width cells and colors
    printf("Grid:\n");
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            const char* places = grid[i][j] ? grid[i][j] : " ";
            const char* messages = messageGrid[i][j] ? messageGrid[i][j] : " ";
            printf(GREEN"%-2s"RESET"("BLUE"%s"RESET") ", places, messages);
        }
        printf("\n");
    }
    // Cleanup
    freeGrid(grid, rows, cols);
    freeGrid(messageGrid, rows, cols);
}
