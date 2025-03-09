# DAP proof of concept

**Distributed** version of the Asynchronous Petri Nets DSL running over plain sockets.

## Requirments

- LLVM 17
- `Boehm GC` package (`apt-get install -y libgc-dev` || `brew install bdw-gc`)

## How to run examples

The simulation program is written in [C](./dap-native-examples/src/main/c/gossip.c), [Python](./dap-native-examples/src/main/python/gossip.py) and [Scala](./dap-jvm-examples/src/main/scala/it/unibo/dap/examples/GossipSimulationApp.scala).

The C and Python examples target the Native platform, while the Scala example targets the JVM platform.

To execute them, you need to run the following commands, each on a separate shell.
The idea is that each program will simulate a node in a gossip network, where each node will send a message to the neighbors specified in the command line, following the following  Petri Nets like rules:

```
1) a|a --1000--> a
2) a --1--> a|^a
3) a|b --2--> a|b|^b
4) b|b --1000--> b
```

Upon running, you should see the `a` token to be gossiped around the network.
When it reaches the last node (i.e. the one listening on port 2553), the `b` token will be spread back to the first node.

Communications between nodes happen over plain sockets.

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

Why?

> LIBRARY_PATH is used by gcc before compilation to search directories containing static and shared libraries that need to be linked to your program.
>
> LD_LIBRARY_PATH is used by your program to search directories containing shared libraries after it has been successfully compiled and linked.
>
> EDIT: As pointed below, your libraries can be static or shared. If it is static then the code is copied over into your program and you don't need to search for the library after your program is compiled and linked. If your library is shared then it needs to be dynamically linked to your program and that's when LD_LIBRARY_PATH comes into play.
> 
> [from [https://stackoverflow.com/questions/4250624/ld-library-path-vs-library-path](https://stackoverflow.com/questions/4250624/ld-library-path-vs-library-path)]
