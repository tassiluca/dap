from typing import List
from dsl import dap

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
            mset = dap.MSet("Id", neighbor_ids)
            neighbors.append(dap.Neighbors(id_obj, mset))
    return neighbors

