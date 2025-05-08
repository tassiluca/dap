from dap import *
from gossip_model import *
import argparse
import time

def main(port: int, static_neighbors: list):
    neighbors = []
    for n in static_neighbors:
        neighbor = Neighbor()
        neighbor.name = n.split(":")[0]
        neighbor.port = int(n.split(":")[1])
        neighbors.append(neighbor)
    neighborhood = Array_Neighbor_of(neighbors)

    # === Tokens ===
    a = TokenImpl("a", port)
    b = TokenImpl("b", port)

    # === Rules ===
    # 1) a --1--> a|^a
    rule = Rule()
    rule.preconditions = MSet_Token_of([a])
    rule.rate = 1.0
    rule.effects = MSet_Token_of([a])
    rule.msg = a
    # 2) a|a --1000--> a
    rule2 = Rule()
    rule2.preconditions = MSet_Token_of([a, a])
    rule2.rate = 1_000.0
    rule2.effects = MSet_Token_of([a])
    # 3) a|b --2--> a|b|^b
    rule3 = Rule()
    rule3.preconditions = MSet_Token_of([a, b])
    rule3.rate = 2.0
    rule3.effects = MSet_Token_of([a, b])
    rule3.msg = b
    # 4) b|b --1_000--> b
    rule4 = Rule()
    rule4.preconditions = MSet_Token_of([b, b])
    rule4.rate = 1000.0
    rule4.effects = MSet_Token_of([b])
    all_rules = Array_Rule_of([rule, rule2, rule3, rule4])
    # # === Initial state ===
    if port == 2550:
        initial_tokens = MSet_Token_of([a])
    elif port == 2553:
        initial_tokens = MSet_Token_of([b])
    else:
        initial_tokens = MSet_Token_of([])
    initial_state = DAPState()
    initial_state.tokens = initial_tokens

    # === Launch simulation ===
    sim = simulation(all_rules, initial_state, neighborhood, PyCodec() , PyEq())
    print("[🐍] Launching simulation...")
    launch(sim, port, PyStateChangeListener())
    time.sleep(30)
    stop(sim)
    time.sleep(5)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
    parser.add_argument("--port", type=int, required=True, help="Service port")
    parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Node neighborhood")
    args = parser.parse_args()
    main(port=args.port, static_neighbors=args.neighbors)
