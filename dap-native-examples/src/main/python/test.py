from dap import *
import dap
import time
import os

token = TokenImpl()
token_impl__init(token)
token.name = "a"
token.device_id = 1

print(token)
print(token.name)
print(token.device_id)

net = MSet_Neighbour_create(1)
MSet_Neighbour_set(net, 0, "localhost:2551")

print(net)

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
            bytes = UInt8Array(size)
            if token_impl__pack(data, bytes.data()) > 0:
                return bytes.data()
            return None
        except Exception as e:
            print(f"Exception in serialize: {e}")
            import traceback
            traceback.print_exc()
            return None

    def deserialize(self, bytes, size):
        print("PyCodec::deserialize")
        try:
            result = TokenImpl()
            token_impl__init(result)
            status = token_impl__unpack(result, bytes, size)
            if status is None or status == 0:
                print("token_impl__unpack failed")
                return None   
            return result
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
            res = token_impl_equals_wrapper(a, b)
            time.sleep(1)
            print(f"PyEq::equals: {res}")
            return res
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
        print("New state")
        for i in range(tokens.size):
            token = MSet_Token_get(tokens, i)
            print(f"-> Token: {token.name} {token.device_id}")
        print(f"-> Msg: {msg.name} {msg.device_id}")

# 1) a --1--> a|^a
rule = Rule()
rule.preconditions = MSet_Token_create(1)
MSet_Token_set(rule.preconditions, 0, token)
rule.rate = 1.0
rule.effects = MSet_Token_create(1)
MSet_Token_set(rule.effects, 0, token)
rule.msg = token
# 2) a|a --1000--> a
rule2 = Rule()
rule2.preconditions = MSet_Token_create(2)
MSet_Token_set(rule2.preconditions, 0, token)
MSet_Token_set(rule2.preconditions, 1, token)
rule2.rate = 1000.0
rule2.effects = MSet_Token_create(1)
MSet_Token_set(rule2.effects, 0, token)
rule2.msg = None

# Initial state
initial_tokens = MSet_Token_create(1)
MSet_Token_set(initial_tokens, 0, token)
initial_state = DAPState()
initial_state.tokens = initial_tokens
initial_state.msg = None
print(initial_state)

all_rules = MSet_Rule_create(2)
MSet_Rule_set(all_rules, 0, rule)
MSet_Rule_set(all_rules, 1, rule2)

register_eq_wrapper("Token", PyEq())
register_serde_wrapper("Token", PyCodec())

launch_simulation_wrapper(all_rules, initial_state, 2550, net, PyStateChangeListener())

#launch_simulation(all_rules, initial_state, 2550, net, None)
