import argparse
from dsl.dap import *

def main():
    parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
    parser.add_argument("--port", type=int, required=True, help="Service port")
    parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Neighbors to connect to")

    args = parser.parse_args()

    # Create a DAP model with two rules
    a = Token.of("a")
    b = Token.of("b")
    # 1) a|a --1_000--> a
    rule1 = Rule(preconditions = MSet(a, a), rate = 1_000.0, effects = MSet(a), msg = None)
    # 2) a --1--> a|^a
    rule2 = Rule(preconditions = MSet(a), rate = 1.0, effects = MSet(a), msg = a)
    # 3) a|b --2--> a|b|^b
    rule3 = Rule(preconditions = MSet(a, b), rate = 2.0, effects = MSet(a, b), msg = b)
    # 4) b|b --1_000--> b
    rule4 = Rule(preconditions = MSet(b, b), rate = 1_000.0, effects = MSet(b), msg = None)
    port = args.port
    neighbors = [Neighbour.of(n) for n in args.neighbors]
    initial_state = DAPState.of(MSet(a), None) if port == 2550 else DAPState.of(MSet(b), None) if port == 2553 else DAPState.of(MSet(type_hint=Token), None)
    # Actual semantics
    DAP([rule1, rule2, rule3, rule4], initial_state).launch_simulation(port, neighbors)

if __name__ == "__main__":
    main()
