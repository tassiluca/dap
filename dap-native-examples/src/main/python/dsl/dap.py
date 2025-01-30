import sys

from lib import dap_cffi
from typing import TypeVar, Generic, List
import weakref

ffi = dap_cffi.ffi

class Id:
    def __init__(self, c_state):
        self.c_struct = c_state

    @classmethod
    def of(cls, x: int, y: int):
        c_struct = ffi.new("struct Id *")
        c_struct.x = x
        c_struct.y = y
        return cls(c_struct)

    def __str__(self):
        return f"Id({self.c_struct.x}, {self.c_struct.y})"

class Place:
    def __init__(self, c_place):
        self.c_struct = c_place

    @classmethod
    def of(cls, token: str):
        assert len(token) == 1
        c_struct = ffi.new("struct Place *")
        c_struct.p = token
        return cls(c_struct)

    def __str__(self):
        return f"Place({self.c_struct.p})"

T = TypeVar("T", Id, Place)

class MSet(Generic[T]):
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, elems: List[T]):
        type_name = elems[0].__class__.__name__
        self.c_struct = ffi.new(f"MSet_{type_name} *")
        self.c_struct.size = len(elems)
        self.c_struct.elements = ffi.new(f"{type_name}[{len(elems)}]")
        self._weak_refs[self.c_struct] = elems # ensure to not be garbage-collected
        elements = []
        for i, elem in enumerate(elems):
            self.c_struct.elements[i] = elem.c_struct
            elements.append(getattr(sys.modules[__name__], type_name)(elem.c_struct))
        self.elements = elements

    def __str__(self):
        return f"MSet({self.c_struct.size}, {[str(elem) for elem in self.elements]})"

class Neighbors:
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, id: Id, mset: MSet):
        self.c_struct = ffi.new("Neighbors *")
        self._weak_refs[self.c_struct] = (id, mset)
        self.c_struct.point = id.c_struct
        self.c_struct.neighbors = mset.c_struct
        self.point = id
        self.neighbors = mset

    def __str__(self):
        return f"Neighbors of {str(self.point)} are {str(self.neighbors)}"

class Rule:
    def __init__(self, preconditions: MSet[Place], effects: MSet[Place], messages: MSet[Place]):
        self.c_struct = ffi.new("Rule *")
        self.c_struct.preconditions = preconditions.c_struct
        self.c_struct.effects = effects.c_struct
        self.c_struct.message = messages.c_struct
