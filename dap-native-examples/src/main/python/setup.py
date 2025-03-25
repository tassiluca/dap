#!/usr/bin/env python

from setuptools import setup, Extension
import subprocess

#subprocess.run(["swig", "-python", "-c++", "-outdir", ".", "example.i"])

dap_module = Extension(
    '_dap',
    sources=['dap_wrap.cxx', 'gossip.pb-c.c'],
    include_dirs=['/opt/homebrew/opt/protobuf-c/include'],
    libraries=['protobuf-c', 'dap'],
    library_dirs=['/opt/homebrew/opt/protobuf-c/lib', './lib/', '.'],
)

setup(
    name='dap',
    version='0.1',
    author='LT',
    description='DAP Library',
    ext_modules=[dap_module],
    py_modules=['dap'],
)
