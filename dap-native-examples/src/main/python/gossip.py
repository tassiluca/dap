from dap import *
import argparse
import json
from datetime import datetime
import time
import sys

parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
parser.add_argument("--port", type=int, required=True, help="Service port")
parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Node neighborhood")
args = parser.parse_args()
port = args.port
neighbors = []
for neighbor in args.neighbors:
    n = Neighbor()
    n.name = neighbor.split(":")[0]
    n.port = int(neighbor.split(":")[1])
    neighbors.append(n)

print(f"Port: {port}")
print(f"Neighbors: [{', '.join([f'{n.name}:{n.port}' for n in neighbors])}]")
print()

print("Neighborhood:")
neighborhood = Array_Neighbor_of(neighbors)

class TokenImpl():
    def __init__(self, name: str, device_id: int):
        self.name = name
        self.device_id = device_id

    def serialize(self) -> str:
        return json.dumps({'name': self.name,'device_id': self.device_id})

    @classmethod
    def deserialize(cls, data: str):
        obj = json.loads(data)
        return cls(name = obj['name'], device_id = obj['device_id'])

    def __str__(self):
        return "Token[Name: {}, Device ID: {}]".format(self.name, self.device_id)

# === Token ===
a = TokenImpl("a", port)
b = TokenImpl("b", port)

# === Global instances ===
_global_eq_instance = None
_global_codec_instance = None
_global_state_change_listener = None

class PyEq(Equalizer):
    def __init__(self):
        global _global_eq_instance
        super().__init__()
        _global_eq_instance = self

    def equals(self, t1, t2):
        try:
            return isinstance(t1, TokenImpl) and isinstance(t2, TokenImpl) and t1.name == t2.name
        except Exception as e:
            print(f"Exception in equals: {e}")
            import traceback
            traceback.print_exc()
            return False

class PyCodec(Codec):
    
    def __init__(self):
        global _global_codec_instance
        self._token_cache = [] # TODO: implement in swig instead of here
        super().__init__()
        _global_codec_instance = self

    def serialize(self, token) -> str:
        if isinstance(token, TokenImpl):
            return token.serialize()
        else:
            return ""

    def deserialize(self, data):
        try:
            token = TokenImpl.deserialize(data)
            self._token_cache.append(token)
            return token
        except Exception as e:
            print(f"[🐍] Exception in deserialization: {e}")
            import traceback
            traceback.print_exc()
            return None

class PyStateChangeListener(StateChangeListener):
    def __init__(self):
        global _global_state_change_listener
        super().__init__()
        _global_state_change_listener = self

    def on_state_change(self, state):
        tokens = state.tokens
        msg = state.msg
        print("-" * 50)
        print(f"[🐍] {datetime.now().strftime('%H:%M:%S.%f')[:-3]}")
        for i in range(tokens.size):
            token = Array_Token_get(tokens, i)
            print(f"[🐍] {token}")
        if msg is not None:
            print(f"[🐍] Msg: {msg}")
        else:
            print("[🐍] Msg: None")
        print("-" * 50, end = "\n\n")

# === Rules ===
# 1) a --1--> a|^a
rule = Rule()
rule.preconditions = Array_Token_of([a])
rule.rate = 1.0
rule.effects = Array_Token_of([a])
rule.msg = a
# 2) a|a --1000--> a
rule2 = Rule()
rule2.preconditions = Array_Token_of([a, a])
rule2.rate = 1_000.0
rule2.effects = Array_Token_of([a])
# rule2.msg = None
# # 3) a|b --2--> a|b|^b
# rule3 = Rule()
# rule3.preconditions = MSet_Token_of([a, b])
# rule3.rate = 2.0
# rule3.effects = MSet_Token_of([a, b])
# rule3.msg = b
# # 4) b|b --1_000--> b
# rule4 = Rule()
# rule4.preconditions = MSet_Token_of([b, b])
# rule4.rate = 1000.0
# rule4.effects = MSet_Token_of([b])
# rule4.msg = None
all_rules = Array_Rule_of([rule, rule2])

# # === Initial state ===
if port == 2550:
    initial_tokens = Array_Token_of([a])
# elif port == 2553:
#     initial_tokens = MSet_Token_of([b])
else:
    initial_tokens = Array_Token_of([])
initial_state = DAPState()
initial_state.tokens = initial_tokens
#initial_state.msg = None

# === Launch simulation ===
sim = simulation(all_rules, initial_state, neighborhood, PyCodec() , PyEq())
print("[🐍] Launching simulation...")
launch(sim, port, PyStateChangeListener())
time.sleep(30)
stop(sim)
