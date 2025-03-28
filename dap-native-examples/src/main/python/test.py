from dap import *
import argparse
from datetime import datetime
import time

parser = argparse.ArgumentParser(description="Distributed Asynchronous Petri Nets simulation.")
parser.add_argument("--port", type=int, required=True, help="Service port")
parser.add_argument("--neighbors", type=str, nargs="+", required=True, help="Neighbors to connect to")
args = parser.parse_args()
port = args.port
neighbors = [n for n in args.neighbors]

# === Token ===
a = TokenImpl()
token_impl__init(a)
a.name = "a"
a.device_id = 1
b = TokenImpl()
token_impl__init(b)
b.name = "b"
b.device_id = 2

# === Neighbours net ===
print("Port: ", port)
net = MSet_Neighbour_create(len(neighbors))
for i in range(len(neighbors)):
    MSet_Neighbour_set(net, i, neighbors[i])

# === Global instances ===
_global_codec_instance = None
_global_eq_instance = None
_global_state_change_listener = None

class PyCodec(Codec):
    def __init__(self):
        global _global_codec_instance
        super().__init__()
        _global_codec_instance = self

    def serialize(self, data, out_size):
        try:
            size = token_impl__get_packed_size(data)
            out_size.assign(size)
            self._last_buffer = UInt8Array(size)  
            if token_impl__pack(data, self._last_buffer.data()) > 0:
                return self._last_buffer.data()
            print("token_impl__pack failed")
            return None
        except Exception as e:
            print(f"Exception in serialize: {e}")
            import traceback
            traceback.print_exc()
            return None

    def deserialize(self, bytes, size):
        try:
            return token_impl__unpack(None, size, bytes)
        except Exception as e:
            print(f"Exception in deserialize: {e}")
            import traceback
            traceback.print_exc()
            return None
        
class PyEq(Equatable):
    def __init__(self):
        global _global_eq_instance
        super().__init__()
        _global_eq_instance = self
        
    def equals(self, a, b):
        try:
            return token_impl_equals_wrapper(a, b)
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
            token = MSet_Token_get(tokens, i)
            print(f"[🐍] State: [token={token.name}, id={token.device_id}]")
        if msg is not None:
            print(f"[🐍] Msg: [token={msg.name}, id={msg.device_id}]")
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
register_eq_wrapper("Token", PyEq())
register_serde_wrapper("Token", PyCodec())
launch_simulation_wrapper(all_rules, initial_state, port, net, PyStateChangeListener())
