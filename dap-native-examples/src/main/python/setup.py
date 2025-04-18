#!/usr/bin/env python

from setuptools import setup, Extension
import subprocess
import sys

subprocess.run(["swig", "-python", "-c++", "-threads", "-outdir", ".", "dap.i"])

extra_link_args = []
if sys.platform == 'darwin':
    extra_link_args = ['-Wl,-rpath,@loader_path/lib', '-Wl,-rpath,@loader_path']

dap_module = Extension(
    '_dap',
    sources=['dap_wrap.cxx'],
    include_dirs=[],
    libraries=['dap'],
    library_dirs=['./lib/', '.'],
    extra_link_args=extra_link_args,
)

setup(
    name='dap',
    version='0.1',
    author='LT',
    description='DAP Library',
    ext_modules=[dap_module],
    py_modules=['dap'],
)
