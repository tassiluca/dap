from typing import List
from collections import defaultdict
from dsl import dap
from dsl.dap import DAPState

def grid_of_ids(rows: int, cols: int) -> List[dap.Neighbors]:
    ids = [[dap.Id.of(i, j) for j in range(cols)] for i in range(rows)]
    neighbors = []
    for i in range(rows):
        for j in range(cols):
            id_obj = ids[i][j]
            neighbor_ids = []
            if i > 0:
                neighbor_ids.append(ids[i - 1][j])
            if i < rows - 1:
                neighbor_ids.append(ids[i + 1][j])
            if j > 0:
                neighbor_ids.append(ids[i][j - 1])
            if j < cols - 1:
                neighbor_ids.append(ids[i][j + 1])
            mset = dap.MSet(neighbor_ids)
            neighbors.append(dap.Neighbors(id_obj, mset))
    return neighbors

def print_grid(width: int, height: int, state: DAPState):
    grid = defaultdict(lambda: defaultdict(lambda: ("", "")))
    for token in state.tokens:
        x, y = token.id.coordinates
        grid[x][y] = (grid[x][y][0] + token.place.p, grid[x][y][1])
    for message in state.messages:
        x, y = message.id.coordinates
        grid[x][y] = (grid[x][y][0], grid[x][y][1] + message.place.p)
    def format_cell(x, y):
        token, message = grid[x][y]
        return f"{token}({message})"
    max_width = max(5, *(len(format_cell(i, j)) for i in range(height) for j in range(width)))
    for i in range(height):
        row = [f"{format_cell(i, j):<{max_width}}" for j in range(width)]
        print(" ".join(row))
