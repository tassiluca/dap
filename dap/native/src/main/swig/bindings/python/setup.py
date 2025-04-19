#!/usr/bin/env python

from setuptools import setup, Extension
from setuptools.command.build_ext import build_ext
import subprocess
import sys
import os
import shutil

print("Running setup.py...")
subprocess.run(["swig", "-python", "-c++", "-threads", "-outdir", ".", "dap.i"])

print("SWIG command executed successfully.")
extra_link_args = []
if sys.platform == 'darwin':
    extra_link_args = ['-Wl,-rpath,@loader_path/lib', '-Wl,-rpath,@loader_path']

dap_module = Extension(
    '_dap',
    sources=['dap_wrap.cxx'],
    libraries=['dap'],
    library_dirs=['.'], # where libdap
    extra_link_args=extra_link_args,
)

class CustomBuildExt(build_ext):
    def run(self):
        build_ext.run(self)
        build_lib = self.build_lib
        ext_path = self.get_ext_fullpath('_dap')
        ext_dir = os.path.dirname(ext_path)
        shutil.copy('libdap.dylib', ext_dir)

setup(
    name='dap',
    version='0.1',
    author='Luca Tassinari',
    description='Distributed Asynchronous Petri-nets (DAP) Library',
    ext_modules=[dap_module],
    py_modules=['dap'],
    cmdclass={'build_ext': CustomBuildExt},
    package_data={'': ['libdap.dylib']},
    include_package_data=True,
)
