import sys

from dsl import State
from lib import dap_cffi
from typing import TypeVar, Type, Generic, List
import weakref

ffi = dap_cffi.ffi
lib = dap_cffi.lib

class Id:
    def __init__(self, c_struct):
        self.c_struct = c_struct

    @classmethod
    def of(cls, x: int, y: int):
        c_struct = ffi.new("struct Id *")
        c_struct.x = x
        c_struct.y = y
        return cls(c_struct)

    def __str__(self):
        return f"Id({self.c_struct.x}, {self.c_struct.y})"

class Place:
    def __init__(self, c_struct):
        self.c_struct = c_struct

    @classmethod
    def of(cls, token: str):
        assert len(token) == 1
        c_struct = ffi.new("struct Place *")
        c_struct.p = token.encode()
        return cls(c_struct)

    def __str__(self):
        return f"Place({self.c_struct.p.decode()})"

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

    def __str__(self):
        return f"Token({self.id}, {self.place}"

T = TypeVar("T", Id, Place, Token)

class MSet(Generic[T]):
    def __init__(self, elems: List[T], type_hint: Type[T] = None):
        if not elems and type_hint is None:
            raise ValueError("Cannot determine type from empty list. Please provide type hint.")
        type_name = elems[0].__class__.__name__ if elems else type_hint.__name__
        self.c_struct = ffi.new(f"MSet_{type_name} *")
        self.c_struct.size = len(elems)
        # !Warning! this is necessary to avoid to elements to be garbage collected
        self.c_elements = ffi.new(f"{type_name}[{len(elems)}]", [e.c_struct for e in elems])
        self.c_struct.elements = self.c_elements
        self.elements = [getattr(sys.modules[__name__], type_name)(e.c_struct) for e in elems]

    def __str__(self):
        return f"MSet({self.c_struct.size}, {[str(elem) for elem in self.elements]})"

class Neighbors:
    #_weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, id: Id, mset: MSet):
        self.c_struct = ffi.new("Neighbors *")
        #self._weak_refs[self.c_struct] = (id, mset)
        self.c_struct.point = id.c_struct
        self.c_struct.neighbors = mset.c_struct
        self.point = id
        self.neighbors = mset

    def __str__(self):
        return f"Neighbors of {str(self.point)} are {str(self.neighbors)}"

class Rule:
    def __init__(self, preconditions: MSet[Place], rate: float, effects: MSet[Place], messages: MSet[Place]):
        self.c_struct = ffi.new("Rule *")
        self.c_struct.preconditions = preconditions.c_struct
        self.preconditions = preconditions
        self.c_struct.effects = effects.c_struct
        self.effects = effects
        self.c_struct.messages = messages.c_struct
        self.message = messages
        self.c_struct.rate = lib.constant_1_rate if rate == 1.0 else lib.constant_1k_rate

    def __str__(self):
        return f"Rule: {(str(self.preconditions), str(self.effects), str(self.message))}"

@ffi.def_extern()
def constant_1_rate(mset):
    return 1.0

@ffi.def_extern()
def constant_1k_rate(mset):
    return 1000.0

class DAPState(State):
    @classmethod
    def of(cls, tokens: MSet[Token], messages: MSet[Token]):
        c_struct = ffi.new("struct State *")
        c_struct.tokens = tokens.c_struct
        c_struct.messages = messages.c_struct
        return cls(c_struct)

    def __init__(self, c_struct):
        self.c_struct = c_struct

    def __str__(self):
        return f"DAPState(tokens={self.tokens}, messages={self.messages})"
