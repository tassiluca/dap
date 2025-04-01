#!/bin/bash

# protobuf-c header file is located on macos on:
#   /opt/homebrew/opt/protobuf-c/include/protobuf-c/protobuf-c.h

# 1. generate proto C bindings
pushd ./proto/
if [ ! -f ../gossip.pb-c.h ]; then
    echo "Generating C bindings for gossip.proto"
    protoc --c_out=../lib gossip.proto
else
    echo "Bindings already exists"
fi
popd

# 2. Build swig wrappers
swig -python -c++ -threads dap.swg

# 3. Setup
python setup.py build_ext --inplace
