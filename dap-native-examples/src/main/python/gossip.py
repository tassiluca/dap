from dsl import dap, Trace, Event
from dsl.dap import *
from utils import grids
from lib import dap_cffi
from utils.grids import print_grid

ffi = dap_cffi.ffi
lib = dap_cffi.lib

grid_width = 5
grid_height = 5

neighbors = grids.grid_of_ids(rows = grid_height, cols = grid_width)
all_neighbors = ffi.new(f"Neighbors[{len(neighbors)}]", [n.c_struct[0] for n in neighbors])

place = Place.of("a")

# 1) a|a --1_000--> a
rule1 = Rule(
    preconditions = MSet([place, place]),
    rate = 1_000.0,
    effects = MSet([place]),
    messages = MSet([], Place)
)
# 2) a --1--> a|^a
rule2 = Rule(
    preconditions = MSet([place]),
    rate = 1.0,
    effects = MSet([place]),
    messages = MSet([place])
)

all_rules = ffi.new("Rule[2]", [rule1.c_struct[0], rule2.c_struct[0]])
my_dap = lib.create_dap_from_rules(all_rules, 2)
dap_ptr = ffi.new("DAP *")
dap_ptr[0] = my_dap
ctmc = lib.dap_to_ctmc(dap_ptr)

ctmc_ptr = ffi.new("CTMC *")
ctmc_ptr[0] = ctmc

left_corner = neighbors[0].point
initial_token = Token.of(id=left_corner, place=place)
initial_tokens = MSet([initial_token])
initial_msgs = MSet([], Token)

initial_state = DAPState.of(initial_tokens, initial_msgs)

steps = 100
trace_ptr = lib.simulate_dap(ctmc_ptr, initial_state.c_struct, all_neighbors, len(neighbors), steps)
events_ptr = trace_ptr.events
events = ffi.unpack(events_ptr, steps)
trace = Trace([Event(e.time, DAPState(e.state)) for e in events])
for e in trace.events:
    print(e.time)
    print_grid(width = grid_width, height = grid_height, state = e.state)
