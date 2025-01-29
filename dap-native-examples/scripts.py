from functools import wraps
from pathlib import Path
import cffi
import glob
import subprocess
import tempfile

this_dir = Path().resolve()
lib_folder = this_dir / "src" / "main" / "python" / "lib"

def with_banner(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        text = func.__name__.replace("_", " ").title() + " task"
        print("=" * len(text))
        print(text)
        print("=" * len(text))
        return func(*args, **kwargs)
    return wrapper

@with_banner
def clean():
    subprocess.run([ "rm", "-rf", lib_folder ], check = True)

@with_banner
def build_library():
    subprocess.run([ "sbt", "dapNative/nativeLink" ], check = True, cwd = this_dir.parent)
    lib_files = glob.glob(str(this_dir.parent / "dap" / "native" / "target" / "scala-*" / "lib*"))
    print("* Copying library to", lib_folder)
    subprocess.run([ "mkdir", "-p", lib_folder ], check = True)
    subprocess.run([ "cp", *lib_files, lib_folder ], check = True)
    print("* Copying header file to", lib_folder)
    h_files_paths = this_dir.glob("src/main/c/*.h")
    for h_file_path in h_files_paths:
        content = _pre_process(h_file_path)
        dest_path = Path(lib_folder) / h_file_path.name
        dest_path.write_text(content)

@with_banner
def build_cffi():
    build_library()
    ffi = cffi.FFI()
    h_file_name = lib_folder / "ctmc.h"
    print("* Reading CFFI Declarations from", h_file_name)
    with open(h_file_name) as h_file:
        lines = h_file.read().splitlines()
        lines += [
            "struct State {",
            "   const char* name;",
            "};",
        ]
        ffi.cdef("\n".join(lines))
    ffi.set_source(
        "dap_cffi",
        """
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
    ffi.compile(tmpdir=lib_folder.as_posix())

# !Warning! cffi does not support C preprocessor directives. This function pre-processes the header file to remove them.
def _pre_process(header_file_path: str) -> str:
    with tempfile.NamedTemporaryFile(suffix=".i", delete=True) as temp_file:
        out_path = temp_file.name
        # Run the preprocessor on the header file (-E) suppressing line markers (-P) directives
        subprocess.run(["clang", "-E", "-P", header_file_path, "-o", out_path], check=True)
        with open(out_path, "r") as f:
            return f.read()
