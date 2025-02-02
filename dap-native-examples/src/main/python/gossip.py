from dsl import dap, Trace, Event
from dsl.dap import DAPState
from utils import grids
from lib import dap_cffi
from collections import defaultdict

from collections import defaultdict


class GridPrinter:
    def __init__(self, width, height, state):
        self.width = width
        self.height = height
        self.state = state
        self.token_counts = defaultdict(lambda: defaultdict(int))
        self.messages = defaultdict(lambda: defaultdict(str))

    def get_cell_content(self, x, y):
        """Restituisce il contenuto formattato di una cella."""
        token_count = self.token_counts[x][y]
        message = self.messages[x][y]

        if token_count == 0 and not message:
            return "( )"

        if message:
            return f"{'a' * token_count}({message})"
        return f"{'a' * token_count}( )"

    def print_grid(self):
        # Conta i token e registra i messaggi per ogni cella
        for token in self.state.tokens:
            x, y = token.id.coordinates
            self.token_counts[x][y] += 1

        for message in self.state.messages:
            x, y = message.id.coordinates
            self.messages[x][y] = message.place.p

        # Calcola la larghezza massima delle celle
        max_width = 3  # minimo per "( )"
        for i in range(self.height):
            for j in range(self.width):
                cell = self.get_cell_content(i, j)
                max_width = max(max_width, len(cell))

        # Stampa la griglia
        for i in range(self.height):
            row = []
            for j in range(self.width):
                cell = self.get_cell_content(i, j)
                row.append(f"{cell:<{max_width}}")
            print(" ".join(row))

ffi = dap_cffi.ffi
lib = dap_cffi.lib

def addr(cstruct):
    return hex(int(ffi.cast("uintptr_t", cstruct)))

neighbors = grids.grid_of_ids(5, 5)
all_neighbors = ffi.new(f"Neighbors[{len(neighbors)}]", [n.c_struct[0] for n in neighbors])

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
ctmc = lib.dap_to_ctmc(dap_ptr)

ctmc_ptr = ffi.new("CTMC *")
ctmc_ptr[0] = ctmc

left_corner = neighbors[0].point
initial_token = dap.Token.of(id=left_corner, place=place)
initial_tokens = dap.MSet([initial_token])
initial_msgs = dap.MSet([], dap.Token)

initial_state = dap.DAPState.of(initial_tokens, initial_msgs)

steps = 100
trace_ptr = lib.simulate_dap(ctmc_ptr, initial_state.c_struct, all_neighbors, len(neighbors), steps)
events_ptr = trace_ptr.events
events = ffi.unpack(events_ptr, steps)
trace = Trace([Event(e.time, DAPState(e.state)) for e in events])
for e in trace.events:
    print(e.time)
    print(e.state)
    gp = GridPrinter(5, 5, e.state)
    gp.print_grid()
