import sys
import weakref
from typing import Optional, TypeVar, Type, Generic, List

from lib import dap_cffi

ffi = dap_cffi.ffi
lib = dap_cffi.lib

class Token:
    _weak_refs = weakref.WeakKeyDictionary()
    
    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.token = None if c_struct == ffi.NULL else ffi.string(c_struct.token).decode()

    @classmethod
    def of(cls, token: str):
        c_struct = ffi.new("struct TokenImpl *")
        token_str = ffi.new("char[]", token.encode())
        cls._weak_refs[c_struct] = token_str  # !Warning! Avoid garbage collection!
        c_struct.token = token_str
        return cls(c_struct)

    def __str__(self) -> str:
        return f"Token({self.token})"

class Neighbour:
    def __init__(self, c_struct):
        self.c_struct = c_struct

    @classmethod
    def of(cls, neighbour: str):
        c_struct = ffi.new("char[]", neighbour.encode())
        return cls(c_struct)

    def __str__(self):
        return f"Neighbor: {ffi.string(self.c_struct).decode()}"

T = TypeVar("T", Neighbour, Token)

class MSet(Generic[T]):
    def __init__(self, elems: List[T], type_hint: Type[T] = None):
        if not elems and type_hint is None:
            raise ValueError("Cannot determine type from empty list. Please provide type hint.")
        type_name = elems[0].__class__.__name__ if elems else type_hint.__name__
        self.c_struct = ffi.new(f"MSet_{type_name} *")
        self.c_struct.size = len(elems)
        # !Warning! this is necessary to avoid elements to be garbage collected!
        self.c_elements = ffi.new(f"{type_name}[{len(elems)}]", [e.c_struct for e in elems])
        self.c_struct.elements = self.c_elements
        self.elements = [getattr(sys.modules[__name__], type_name)(e.c_struct) for e in elems]

    def __str__(self) -> str:
        return "{ " + ",".join([str(elem) for elem in self.elements]) + " }"

class Rule:
    def __init__(self, preconditions: MSet[Token], rate: float, effects: MSet[Token], msg: Optional[Token]):
        self.c_struct = ffi.new("Rule *")
        self.c_struct.preconditions = preconditions.c_struct[0]
        self.preconditions = preconditions
        self.c_struct.effects = effects.c_struct[0]
        self.effects = effects
        self.c_struct.msg = ffi.NULL if msg is None else msg.c_struct
        self.rate_fun = create_fixed_rate_function(rate)
        self.c_struct.rate = self.rate_fun
        self.msg = msg

    def __str__(self) -> str:
        return ("Rule: { preconditions = "  + str(self.preconditions) + ", " +
                        "effects = "        + str(self.effects) + ", " +
                        "messages = "       + str(self.msg) + " " +
                      "}")


def create_fixed_rate_function(rate_value):
    @ffi.callback("double(MSet_Token)")
    def rate_func(mset_place_ptr):
        return rate_value
    return rate_func

class DAPState:
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.tokens = [ Token(self.c_struct.tokens.elements[i]) for i in range(self.c_struct.tokens.size) ]
        self.msg = Token(self.c_struct.msg)

    @classmethod
    def of(cls, tokens: MSet[Token], msg: Optional[Token]):
        c_struct = ffi.new("struct DAPState *")
        c_struct.tokens = tokens.c_struct[0]
        c_struct.msg = ffi.NULL if msg is None else msg.c_struct
        cls._weak_refs[c_struct] = (tokens, msg) # !Warning! Avoid garbage collection!
        return cls(c_struct)

    def __str__(self) -> str:
        return ("DAPState(tokens = { " + ",".join([str(t) for t in self.tokens]) + " }, " +
                         "msg = { " + str(self.msg) + " } ")

class DAP:
    def __init__(self, rules: List[Rule], state: DAPState):
        self.rules = ffi.new(f"Rule[{len(rules)}]", [rule.c_struct[0] for rule in rules])
        self.state = state.c_struct
        self.rules_num = len(rules)

    def launch_simulation(self, port: int, neighbors: List[Neighbour]) -> None:
        net = MSet(neighbors)
        lib.launch_simulation(self.rules, self.rules_num, self.state, port, net.c_struct)
