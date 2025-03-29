#!/bin/bash

# 1. Build swig wrappers
swig -python -c++ -threads dap.i

# 2. Setup
python setup.py build_ext --inplace
