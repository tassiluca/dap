import pathlib
import cffi
import subprocess
import re
import glob
import pathlib
from pathlib import Path

this_dir = pathlib.Path().resolve()
lib_folder = this_dir / "src" / "main" / "python" / "lib"

def print_banner(text):
    print("=" * len(text))
    print(text)
    print("=" * len(text))

def clean():
    print_banner("Cleaning")
    subprocess.run([ "rm", "-rf", lib_folder ], check = True)

def clean_header(content):
    content = re.sub(r'^\s*#ifndef.*\n\s*#define.*\n', '', content, count=1, flags=re.MULTILINE)
    content = re.sub(r'\n\s*#endif.*$', '', content, flags=re.MULTILINE)
    return content

def build_library():
    print_banner("Building Native Library")
    print("* Compiling library")
    subprocess.run([ "sbt", "dapNative/nativeLink" ], check = True, cwd = this_dir.parent)
    lib_files = glob.glob(str(this_dir.parent / "dap" / "native" / "target" / "scala-*" / "lib*"))
    print("* Copying library to", lib_folder)
    subprocess.run([ "mkdir", "-p", lib_folder ], check = True)
    subprocess.run([ "cp", *lib_files, lib_folder ], check = True)
    print("* Copying header file to", lib_folder)
    h_files = this_dir.glob("src/main/c/*.h")
    for h_file in h_files:
        # cffi doesn't support `#ifndef` declarations in headers :(
        content = h_file.read_text()
        cleaned_content = clean_header(content)
        dest_path = Path(lib_folder) / h_file.name
        dest_path.write_text(cleaned_content)
    print("* Complete")

def build_cffi():
    build_library()
    print_banner("Building CFFI Module")
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
    print("* Complete")
