import pathlib
import cffi
import subprocess
import sys
import glob
import pathlib

def print_banner(text):
    print("=" * len(text))
    print(text)
    print("=" * len(text))

def build_library():
    """Build the native library"""
    print_banner("Building Native Library")
    this_dir = pathlib.Path().resolve()
    print("* Compiling library")
    subprocess.run([ "sbt", "dapNative/nativeLink" ], check = True, cwd = this_dir.parent)
    lib_files = glob.glob(str(this_dir.parent / "dap" / "native" / "target" / "scala-*" / "lib*"))
    lib_folder = this_dir / "lib"
    print("* Copying library to", lib_folder)
    subprocess.run([ "mkdir", "-p", lib_folder ], check = True)
    subprocess.run([ "cp", *lib_files, lib_folder ], check = True)
    print("* Complete")

def build_cffi():
    """Build the CFFI Python bindings"""
    print_banner("Building CFFI Module")
    ffi = cffi.FFI()
    this_dir = pathlib.Path().resolve()
    h_file_name = this_dir / "lib" / "ctmc.h"
    print("* Reading CFFI Declarations from", h_file_name)
    with open(h_file_name) as h_file:
        lines = h_file.read().splitlines()
        ffi.cdef("\n".join(lines))
    ffi.set_source(
        "dap_cffi",
        '#include "ctmc.h"',
        libraries=["dap"],
        library_dirs=[(this_dir / "lib").as_posix()],
        include_dirs=[(this_dir / "lib").as_posix()],
        runtime_library_dirs=[(this_dir / "lib").as_posix()],
    )
    ffi.compile(tmpdir=(this_dir / "lib").as_posix())
    print("* Complete")
