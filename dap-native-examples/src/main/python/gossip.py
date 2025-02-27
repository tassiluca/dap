import argparse
from dsl.dap import *

def main():
    parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
    parser.add_argument("--port", type=int, required=True, help="Service port")
    parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Neighbors to connect to")

    args = parser.parse_args()

    # Create a DAP model with two rules
    token = Token.of("a")
    mset = MSet([token, token])
    # 1) a|a --1_000--> a
    rule1 = Rule(
        preconditions = MSet([token, token]),
        rate = 1_000.0,
        effects = MSet([token]),
        msg = None
    )
    # 2) a --1--> a|^a
    rule2 = Rule(
         preconditions = MSet([token]),
         rate = 1.0,
         effects = MSet([token]),
         msg = token
    )
    port = args.port
    neighbors = [Neighbour.of(n) for n in args.neighbors]
    initial_state = DAPState.of(MSet([token]), None) if port == 2550 else DAPState.of(MSet([], Token), None)
    # Actual semantics
    DAP([rule1, rule2], initial_state).launch_simulation(port, neighbors)

if __name__ == "__main__":
    main()
