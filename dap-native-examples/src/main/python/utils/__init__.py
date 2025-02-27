from lib import dap_cffi
ffi = dap_cffi.ffi

def addr(cstruct):
    """
    Return the hexadecimal address of a cstruct.
    """
    return hex(int(ffi.cast("uintptr_t", cstruct)))
