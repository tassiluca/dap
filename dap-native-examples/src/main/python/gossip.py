import argparse
import textwrap
from datetime import datetime
from rich import print  
from dsl.dap import *

def main():
    """
    Example: python gossip.py --port 2550 --neighbors localhost:2551 localhost:2552
    """
    
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

    # Initial state
    port = args.port
    neighbors = [Neighbour.of(n) for n in args.neighbors]
    initial_state = DAPState.of(MSet(a), None) if port == 2550 else DAPState.of(MSet(b), None) if port == 2553 else DAPState.of(MSet(type_hint=Token), None)

    # Actual semantics
    on_state_change = lambda state: print(textwrap.dedent(f"""
        [cyan][🐍][⏰] [/cyan]{datetime.now().strftime('%H:%M:%S.%f')[:-3]}
        [green][🐍][📦] State Tokens:[/green] [yellow]{{ { " | ".join(str(t.token) for t in state.tokens) } }}[/yellow]
        [green][🐍][💬] Message:[/green] [magenta]"[/magenta]{ state.msg.token }[magenta]"[/magenta]
        [cyan]----------------------------------------[/cyan]
    """).strip())
    DAP([rule1, rule2, rule3, rule4], initial_state).launch_simulation(port, neighbors, on_state_change)

if __name__ == "__main__":
    main()
