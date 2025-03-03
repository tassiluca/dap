# DAP proof of concept

**Distributed** version of the Asynchronous Petri Nets DSL running over plain sockets.

## Requirments

- LLVM 17
- `Boehm GC ` package (`apt-get install -y libgc-dev` || `brew install bdw-gc`)

## How to run examples

The simulation program is written in [C](./dap-native-examples/src/main/c/gossip.c), [Python](./dap-native-examples/src/main/python/gossip.py) and [Scala](./dap-jvm-examples/src/main/scala/it/unibo/dap/examples/GossipSimulationApp.scala).

The C and Python examples target the Native platform, while the Scala example targets the JVM platform.

To execute them:

```bash
sbt "dapJVMExamples/run 2550 localhost:2551 localhost:2552"
```

```bash
cd ./dap-native-examples/src/main/c
make
./gossip.exe 2551 localhost:2550 localhost:2553
```

```bash
./gossip.exe 2552 localhost:2550 localhost:2553
```

```bash
cd ./dap-native-examples
pip install -r requirements.txt
poetry install && poetry run invoke build-cffi
poetry run python src/main/python/gossip.py --port 2553 --neighbors localhost:2551 localhost:2552
```

On MacOs this env variable can be needed in case of errors with the Boehm GC library:

```bash
export LIBRARY_PATH=/opt/homebrew/lib:$LIBRARY_PATH
```
