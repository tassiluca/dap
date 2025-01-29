from cffi import FFI
from lib import dap_cffi
import weakref

ffi = dap_cffi.ffi

class Id:
    def __init__(self, x: int, y: int):
        self.c_struct = ffi.new("struct Id *")
        self.c_struct.x = x
        self.c_struct.y = y

class MSet:
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, type, elems):
        if type not in ["Id", "Place"]:
            raise ValueError("Invalid type. Either 'Id' or 'Place' is allowed.")
        self.c_struct = ffi.new(f"MSet_{type} *")
        self.c_struct.size = len(elems)
        self.c_struct.elements = ffi.new(f"{type}[{len(elems)}]")
        self._weak_refs[self.c_struct] = elems
        for i, elem in enumerate(elems):
            self.c_struct.elements[i] = elem.c_struct
