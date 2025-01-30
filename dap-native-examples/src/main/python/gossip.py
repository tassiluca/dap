from dsl import dap
from utils import grids
from lib import dap_cffi

ffi = dap_cffi.ffi

sep = "-" * 50

test_id = dap.Id.of(1, 2)
print(test_id.c_struct)
print(test_id)
print(sep)

test_mset = dap.MSet([dap.Id.of(0, 2), dap.Id.of(2, 2), dap.Id.of(1, 1), dap.Id.of(1, 3)])
print(test_mset.c_struct)
print(test_mset)
print(sep)

neighbors = grids.grid_of_ids(5, 5)
for neighbor in neighbors:
    print(neighbor)

print(neighbors[0].point)
print(neighbors[1].neighbors.elements[1])

assert int(ffi.cast("uintptr_t", neighbors[0].point.c_struct)) == int(ffi.cast("uintptr_t", neighbors[1].neighbors.elements[1].c_struct))

token = dap.Place.of("a")

# 1) a|a --1_000--> a
# 2) a --1--> a|^a
rule1 = dap.Rule(preconditions = dap.MSet([token, token]), effects=dap.MSet([token]), messages=dap.MSet([]))
rule2 = dap.Rule(preconditions = dap.MSet([token]), effects=dap.MSet([token]), messages=dap.MSet([token]))
