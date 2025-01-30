from functools import wraps
from pathlib import Path
import cffi
import glob
import invoke
import subprocess
import tempfile

this_dir = Path().resolve()
lib_folder = this_dir / "src" / "main" / "python" / "lib"

def with_banner(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        header = "\n> " + func.__name__.replace("_", " ").title() + " task"
        print(header)
        print("‾" * len(header))
        return func(*args, **kwargs)
    return wrapper

@invoke.task()
@with_banner
def clean(c):
    """
    Clean up the Python `lib` folder.
    """
    invoke.run(f"rm -rf {lib_folder}")

@invoke.task(pre=[clean])
@with_banner
def build_library(c):
    """
    Build the DAP library in a shared object file and copy it to the Python `lib` folder.
    """
    invoke.run("cd .. && sbt dapNative/nativeLink")
    lib_files = glob.glob(str(this_dir.parent / "dap" / "native" / "target" / "scala-*" / "lib*"))
    log(f"Copying library to {lib_folder}")
    invoke.run(f"mkdir -p {lib_folder}")
    invoke.run(f'cp {" ".join(lib_files)} {lib_folder}')
    log(f"Copying header file to {lib_folder}")
    h_files_paths = this_dir.glob("src/main/c/*.h")
    for h_file_path in h_files_paths:
        content = _pre_process(h_file_path)
        dest_path = Path(lib_folder) / h_file_path.name
        dest_path.write_text(content)

@invoke.task(pre=[build_library])
@with_banner
def build_cffi(c):
    """
    Build the CFFI wrapper for the DAP library.
    """
    ctmc_ffi = cffi.FFI()
    ctmc_module_h_file_name = lib_folder / "ctmc.h"
    with open(ctmc_module_h_file_name) as h_file:
        lines = h_file.read().splitlines()
        lines += [
            "struct State {",
            "   const char* name;",
            "};",
        ]
        ctmc_ffi.cdef("\n".join(lines))
    ctmc_ffi.set_source(
        module_name="ctmc_cffi",
        source="""
        #include "ctmc.h"
        struct State {
            const char* name;
        };
        """,
        libraries=["dap"],
        library_dirs=[lib_folder.as_posix()],
        include_dirs=[lib_folder.as_posix()],
        runtime_library_dirs=[lib_folder.as_posix()],
    )
    ctmc_ffi.compile(tmpdir=lib_folder.as_posix())
    dap_ffi = cffi.FFI()
    dap_module_h_file_name = lib_folder / "dap_char2d.h"
    with open(dap_module_h_file_name) as h_file:
        lines = h_file.read().splitlines()
        dap_ffi.cdef("\n".join(lines))
    dap_ffi.set_source(
        module_name="dap_cffi",
        source='#include "dap_char2d.h"',
        libraries=["dap"],
        library_dirs=[lib_folder.as_posix()],
        include_dirs=[lib_folder.as_posix()],
        runtime_library_dirs=[lib_folder.as_posix()],
    )
    dap_ffi.compile(tmpdir=lib_folder.as_posix())

# !Warning! cffi does not support C preprocessor directives. This function pre-processes the header file to remove them.
def _pre_process(header_file_path: str) -> str:
    with tempfile.NamedTemporaryFile(suffix=".i", delete=True) as temp_file:
        out_path = temp_file.name
        # Run the preprocessor on the header file (-E) suppressing line markers (-P) directives
        subprocess.run(["clang", "-E", "-P", header_file_path, "-o", out_path], check=True)
        with open(out_path, "r") as f:
            return f.read()

def log(msg: str):
    print(f"[info] {msg}")
