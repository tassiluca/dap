from cffi import FFI
from lib import dap_cffi
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

class MSet:
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, type, elems):
        if type not in ["Id", "Place"]:
            raise ValueError("Invalid type. Either 'Id' or 'Place' is allowed.")
        self.c_struct = ffi.new(f"MSet_{type} *")
        self.c_struct.size = len(elems)
        self.c_struct.elements = ffi.new(f"{type}[{len(elems)}]")
        self._weak_refs[self.c_struct] = elems
        elements = []
        for i, elem in enumerate(elems):
            self.c_struct.elements[i] = elem.c_struct
            if type == "Id":
                elements.append(Id(elem.c_struct))
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
