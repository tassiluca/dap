import faulthandler
from dap import *

faulthandler.enable()

token = TokenImpl()
token_impl__init(token)
token.name = "a"
token.device_id = 1

net = MSet_Neighbour_create(1)
MSet_Neighbour_set(net, 0, "localhost:2551")

print(net)

class PyCodec(Codec):
    def __init__(self):
        Codec.__init__(self)

    def serialize(self, data, out_size):
        print("PyCodec::serialize")
        try:
            size = token_impl__get_packed_size(data)
            bytes = UInt8Array(size)
            return token_impl__pack(data, bytes)
        except Exception as e:
            print(f"Exception in serialize: {e}")
            import traceback
            traceback.print_exc()
            return

    def deserialize(self, bytes, size):
        ...
        
class PyEq(Equatable):
    def __init__(self):
        Equatable.__init__(self)
        
    def equals(self, a, b):
        print("PyEq::equals")
        try:
            if not isinstance(a, TokenImpl) or not isinstance(b, TokenImpl):
                return False
            return a.device_id == b.device_id and a.name == b.name
        except Exception as e:
            print(f"Exception in equals: {e}")
            import traceback
            traceback.print_exc()
            return

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
rule2.rate = 1_000.0
rule2.effects = MSet_Token_create(1)
MSet_Token_set(rule2.effects, 0, token)

# Initial state
initial_tokens = MSet_Token_create(1)
MSet_Token_set(initial_tokens, 0, token)
initial_state = DAPState()
initial_state.tokens = initial_tokens
initial_state.msg = None

all_rules = MSet_Rule_create(2)
MSet_Rule_set(all_rules, 0, rule)
MSet_Rule_set(all_rules, 1, rule2)

register_eq_wrapper("Token", PyEq())
register_serde_wrapper("Token", PyCodec())

launch_simulation(all_rules, initial_state, 2550, net, None)
