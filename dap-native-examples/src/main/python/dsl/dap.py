import sys
import weakref
from typing import TypeVar, Type, Generic, List

from dsl import State, Trace, Event
from lib import dap_cffi

ffi = dap_cffi.ffi
lib = dap_cffi.lib

class Id:
    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.coordinates = (c_struct.x, c_struct.y)

    @classmethod
    def of(cls, x: int, y: int):
        c_struct = ffi.new("struct Id *")
        c_struct.x = x
        c_struct.y = y
        return cls(c_struct)

    def __str__(self) -> str:
        return f"Id({self.coordinates})"

class Place:
    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.p = c_struct.p.decode()

    @classmethod
    def of(cls, token: str):
        assert len(token) == 1
        c_struct = ffi.new("struct Place *")
        c_struct.p = token.encode()
        return cls(c_struct)

    def __str__(self) -> str:
        return f"Place({self.p})"

class Token:
    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.id = Id(c_struct.id)
        self.place = Place(c_struct.place)

    @classmethod
    def of(cls, id: Id, place: Place):
        cls.c_struct = ffi.new("Token *")
        cls.c_struct.id = id.c_struct
        cls.c_struct.place = place.c_struct
        return cls(cls.c_struct[0])

    def __str__(self) -> str:
        return f"Token({self.id}, {self.place})"

T = TypeVar("T", Id, Place, Token)

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

class Neighbors:
    def __init__(self, id: Id, mset: MSet):
        self.c_struct = ffi.new("Neighbors *")
        self.c_struct.point = id.c_struct
        self.c_struct.neighbors = mset.c_struct
        self.point = id
        self.neighbors = mset

    def __str__(self):
        return f"Neighbors of {self.point} are {self.neighbors}"

class Rule:
    def __init__(self, preconditions: MSet[Place], rate: float, effects: MSet[Place], messages: MSet[Place]):
        self.c_struct = ffi.new("Rule *")
        self.c_struct.preconditions = preconditions.c_struct
        self.preconditions = preconditions
        self.c_struct.effects = effects.c_struct
        self.c_struct.messages = messages.c_struct
        self.rate_fun = create_fixed_rate_function(rate)
        self.c_struct.rate = self.rate_fun
        self.effects = effects
        self.message = messages

    def __str__(self) -> str:
        return ("Rule: { preconditions = "  + str(self.preconditions) + ", " +
                        "effects = "        + str(self.effects) + ", " +
                        "messages = "       + str(self.message) + " " +
                      "}")


def create_fixed_rate_function(rate_value):
    @ffi.callback("double(MSet_Place *)")
    def rate_func(mset_place_ptr):
        return rate_value
    return rate_func

class DAPState(State):
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, c_struct):
        self.c_struct = c_struct
        self.tokens = [ Token(self.c_struct.tokens.elements[i]) for i in range(self.c_struct.tokens.size) ]
        self.messages = [ Token(self.c_struct.messages.elements[i]) for i in range(c_struct.messages.size) ]

    @classmethod
    def of(cls, tokens: MSet[Token], messages: MSet[Token]):
        c_struct = ffi.new("struct State *")
        c_struct.tokens = tokens.c_struct
        c_struct.messages = messages.c_struct
        cls._weak_refs[c_struct] = (tokens, messages) # !Warning! Avoid garbage collection!
        return cls(c_struct)

    def __str__(self) -> str:
        return ("DAPState(tokens = { " + ",".join([str(t) for t in self.tokens]) + " }, " +
                         "messages = { " + ",".join([str(m) for m in self.messages]) + "} ")

class DAP:
    def __init__(self, rules: List[Rule]):
        self.all_rules = ffi.new(f"Rule[{len(rules)}]", [rule.c_struct[0] for rule in rules])
        self.c_struct = lib.create_dap_from_rules(self.all_rules, len(rules))

    def simulate(self, initial_state: DAPState, neighbors: List[Neighbors], steps: int) -> Trace:
        dap_ptr = ffi.new("DAP *")
        dap_ptr[0] = self.c_struct
        ctmc = lib.dap_to_ctmc(dap_ptr)
        ctmc_ptr = ffi.new("CTMC *")
        ctmc_ptr[0] = ctmc
        all_neighbors = ffi.new(f"Neighbors[{len(neighbors)}]", [n.c_struct[0] for n in neighbors])
        trace_ptr = lib.simulate_dap(ctmc_ptr, initial_state.c_struct, all_neighbors, len(neighbors), steps)
        events_ptr = trace_ptr.events
        events = ffi.unpack(events_ptr, steps)
        return Trace([Event(e.time, DAPState(e.state)) for e in events])
