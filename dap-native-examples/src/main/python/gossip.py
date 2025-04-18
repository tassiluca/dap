from dap import *
import argparse
from datetime import datetime
import msgpack
import time

parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
parser.add_argument("--port", type=int, required=True, help="Service port")
parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Neighbors to connect to")
args = parser.parse_args()
port = args.port
neighbors = [n for n in args.neighbors]

class TokenImpl():
    def __init__(self, name: str, device_id: int):
        self.name = name
        self.device_id = device_id
        
    def serialize(self) -> bytes:
        return msgpack.packb({"name": self.name, "device_id": self.device_id})
        
    @classmethod
    def deserialize(cls, data: bytes):
        obj = msgpack.unpackb(data, raw=False)
        return cls(obj["name"], obj["device_id"])

    def __str__(self):
        return "Token[Name: {}, Device ID: {}]".format(self.name, self.device_id)

# === Token ===
token_a = TokenImpl("a", port)
token_b = TokenImpl("b", port)
a = pack(token_a.serialize())
b = pack(token_b.serialize())

# === Global instances ===
_global_eq_instance = None
_global_state_change_listener = None

class PyEq(Equatable):
    def __init__(self):
        global _global_eq_instance
        super().__init__()
        _global_eq_instance = self

    def equals(self, a, b):
        try:
            t1 = TokenImpl.deserialize(a.to_bytes())
            t2 = TokenImpl.deserialize(b.to_bytes())
            return t1.name == t2.name
        except Exception as e:
            print(f"Exception in equals: {e}")
            import traceback
            traceback.print_exc()
            return 0

class PyStateChangeListener(StateChangeListener):
    def __init__(self):
        global _global_state_change_listener
        super().__init__()
        _global_state_change_listener = self

    def on_state_change(self, state):
        tokens = state.tokens
        msg = state.msg
        print(f"[🐍] {datetime.now().strftime('%H:%M:%S.%f')[:-3]}")
        for i in range(tokens.size):
            token = TokenImpl.deserialize(MSet_Token_get(tokens, i).to_bytes())
            print(f"[🐍] State: {token}]")
        if msg is not None:
            token = TokenImpl.deserialize(msg.to_bytes())
            print(f"[🐍] Msg: {token}]")
        else:
            print("[🐍] Msg: None")

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
rule2.msg = None
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
rule4.msg = None
all_rules = [rule, rule2, rule3, rule4]

# === Initial state ===
if port == 2550:
    initial_tokens = MSet_Token_of([a])
elif port == 2553:
    initial_tokens = MSet_Token_of([b])
else:
    initial_tokens = MSet_Token_of([])
initial_state = DAPState()
initial_state.tokens = initial_tokens
initial_state.msg = None

# === Launch simulation ===

print("[🐍] Launching simulation...")

launch_simulation_wrapper(all_rules, initial_state, port, neighbors, PyStateChangeListener(), PyEq())

time.sleep(30)
print("[🐍] Simulation finished.")
