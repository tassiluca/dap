from dap import *
import argparse
from datetime import datetime
import struct

parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
parser.add_argument("--port", type=int, required=True, help="Service port")
parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Neighbors to connect to")
args = parser.parse_args()
port = args.port
neighbors = [n for n in args.neighbors]

class TokenImpl:
    def __init__(self, name: str, device_id: int):
        self.name = name
        self.device_id = device_id
        
    def serialize(self) -> bytes:
        name_bytes = self.name.encode('utf-8') + b'\x00'
        return struct.pack(f'I{len(name_bytes)}s', self.device_id, name_bytes)
        
    @classmethod
    def deserialize(cls, data: bytes):
        device_id, name_bytes = struct.unpack(f'I{len(data) - 4}s', data)
        name = name_bytes.decode('utf-8').rstrip('\x00')
        return cls(name, device_id)

    def __str__(self):
        return "Token[Name: {}, Device ID: {}]".format(self.name, self.device_id)

# === Token ===
token_a = TokenImpl("a", port)
token_b = TokenImpl("b", port)
a = pack(token_a.serialize())
b = pack(token_b.serialize())

# === Neighbours net ===
print("Port: ", port)
net = MSet_Neighbour_create(len(neighbors))
for i in range(len(neighbors)):
    MSet_Neighbour_set(net, i, neighbors[i])

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
rule.preconditions = MSet_Token_create(1)
MSet_Token_set(rule.preconditions, 0, a)
rule.rate = 1.0
rule.effects = MSet_Token_create(1)
MSet_Token_set(rule.effects, 0, a)
rule.msg = a
# 2) a|a --1000--> a
rule2 = Rule()
rule2.preconditions = MSet_Token_create(2)
MSet_Token_set(rule2.preconditions, 0, a)
MSet_Token_set(rule2.preconditions, 1, a)
rule2.rate = 1000.0
rule2.effects = MSet_Token_create(1)
MSet_Token_set(rule2.effects, 0, a)
rule2.msg = None
# 3) a|b --2--> a|b|^b
rule3 = Rule()
rule3.preconditions = MSet_Token_create(2)
MSet_Token_set(rule3.preconditions, 0, a)
MSet_Token_set(rule3.preconditions, 1, b)
rule3.rate = 2.0
rule3.effects = MSet_Token_create(2)
MSet_Token_set(rule3.effects, 0, a)
MSet_Token_set(rule3.effects, 1, b)
rule3.msg = b
# 4) b|b --1_000--> b
rule4 = Rule()
rule4.preconditions = MSet_Token_create(2)
MSet_Token_set(rule4.preconditions, 0, b)
MSet_Token_set(rule4.preconditions, 1, b)
rule4.rate = 1000.0
rule4.effects = MSet_Token_create(1)
MSet_Token_set(rule4.effects, 0, b)
rule4.msg = None
# All rules
all_rules = MSet_Rule_create(4)
MSet_Rule_set(all_rules, 0, rule)
MSet_Rule_set(all_rules, 1, rule2)
MSet_Rule_set(all_rules, 2, rule3)
MSet_Rule_set(all_rules, 3, rule4)

# === Initial state ===
if port == 2550:
    initial_tokens = MSet_Token_create(1)
    MSet_Token_set(initial_tokens, 0, a)
elif port == 2553:
    initial_tokens = MSet_Token_create(1)
    MSet_Token_set(initial_tokens, 0, b)
else:
    initial_tokens = MSet_Token_create(0)
initial_state = DAPState()
initial_state.tokens = initial_tokens
initial_state.msg = None

# === Launch simulation ===
register_eq_wrapper(PyEq())
launch_simulation_wrapper(all_rules, initial_state, port, net, PyStateChangeListener())
