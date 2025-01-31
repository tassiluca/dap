from dsl import dap, Trace, Event
from dsl.dap import DAPState
from utils import grids
from lib import dap_cffi

ffi = dap_cffi.ffi
lib = dap_cffi.lib

def addr(cstruct):
    return hex(int(ffi.cast("uintptr_t", cstruct)))

neighbors = grids.grid_of_ids(5, 5)
for n in neighbors:
    print(n)

all_neighbors = ffi.new(f"Neighbors[{len(neighbors)}]")
for i, n in enumerate(neighbors):
    all_neighbors[i].point = n.c_struct.point
    all_neighbors[i].neighbors = n.c_struct.neighbors

# all_neighbors = ffi.new(f"Neighbors[{len(neighbors)}]", [n.c_struct[0] for n in neighbors])

print("*" * 120)

for i in range(len(neighbors)):
    print(f"Neighbors of {all_neighbors[i].point.x, all_neighbors[i].point.y} are:")
    for j in range(all_neighbors[i].neighbors.size):
        print(f"\t {all_neighbors[i].neighbors.elements[j].x, all_neighbors[i].neighbors.elements[j].y}")

print("*" * 120)

place = dap.Place.of("a")

# 1) a|a --1_000--> a
# 2) a --1--> a|^a
rule1 = dap.Rule(
    preconditions = dap.MSet([place, place]),
    rate = 1_000.0,
    effects = dap.MSet([place]),
    messages = dap.MSet([], dap.Place)
)
rule2 = dap.Rule(
    preconditions=dap.MSet([place]),
    rate=1.0,
    effects=dap.MSet([place]),
    messages=dap.MSet([place])
)
all_rules = ffi.new("Rule[2]", [rule1.c_struct[0], rule2.c_struct[0]])
my_dap = lib.create_dap_from_rules(all_rules, 2)
dap_ptr = ffi.new("DAP *")
dap_ptr[0] = my_dap
print("Creating ctmc")
ctmc = lib.dap_to_ctmc(dap_ptr)

ctmc_ptr = ffi.new("CTMC *")
ctmc_ptr[0] = ctmc

# ###################################################################################################

left_corner = neighbors[0].point
initial_token = dap.Token.of(id=left_corner, place=place)
initial_tokens = dap.MSet([initial_token])
initial_msgs = dap.MSet([], dap.Token)

initial_state = dap.DAPState.of(initial_tokens, initial_msgs)

steps = 100
trace_ptr = lib.simulate_dap(ctmc_ptr, initial_state.c_struct, all_neighbors, len(neighbors), steps)
events_ptr = trace_ptr.events
print(events_ptr)
events = ffi.unpack(events_ptr, steps)
for e in events:
    print(f">>>> {e.time}")
    print("\t Tokens")
    for i in range(e.state.tokens.size):
        t = dap.Token(e.state.tokens.elements[i])
        print(t)
    print("\t Messages")
    for j in range(e.state.messages.size):
        m = dap.Token(e.state.messages.elements[j])
        print(m)

