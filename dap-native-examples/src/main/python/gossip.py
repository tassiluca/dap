from dsl.dap import *
from utils import grids
from utils.grids import print_grid

# Grid environment
grid_width = 5
grid_height = 5
neighbors = grids.grid_of_ids(rows = grid_height, cols = grid_width)

# Create a DAP model with two rules
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
# Build the Distributed Asynchronous Petri Net from the rules
dap_net = DAP([rule1, rule2])
# Actual semantics
simulation_steps = 100
left_corner = neighbors[0].point
initial_token = Token.of(id = left_corner, place = place)
initial_state = DAPState.of(tokens = MSet([initial_token]), messages = MSet([], Token))
trace = dap_net.simulate(initial_state, neighbors, simulation_steps)
for e in trace.events:
    print(e.time)
    print_grid(width = grid_width, height = grid_height, state = e.state)
