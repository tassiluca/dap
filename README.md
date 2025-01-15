# DAP proof of concept

## Native interoperability

### Implementation details

- `dap/shared`: contains the shared scala code between all platforms;
- `dap/native`: contains the code for the native platform;
    ```mermaid
    classDiagram
        direction TB
        class NativeCTMCApi {
            <<trait>>
            + type State
            + type Event = CStruct2[CDouble, State]
            + type Trace = CStruct2[Ptr[Event], CSize]
            + simulate(ctmcPtr: Ptr[CTMC[T]], s0: State, steps: CInt, ...) Trace
        }
        class BasicNativeCTMCApi {
            <<object>>
            + override type State = Ptr[CStruct0]
            + type Action = CStruct2[CDouble, State]
            + type Transition = CStruct2[State, Action]
            + @exported def ofTransitions(...) CTMC[State]
            + @exported simulateCTMC(...) Ptr[Trace]
        }
        class NativeDAPApi {
            <<object>>
            + override type State = Ptr[DAPState]
            + @exported def createDAP(...) DAP[Place]
            + @exported def dapToCTMC(...) CTMC[DAP.State[Id, Place]]
            + @exported def simulateDAP(...) Ptr[Trace]
        }
        class NativeDAPBindings {
            <<object>>
            + type Place = Ptr[CStruct0]
            + type Id = Ptr[CStruct0]
            + type CMSet[T] = CStruct2[Ptr[T], CSize]
            + type Token = CStruct2[Id, Place]
            + type DAPState = CStruct2[Ptr[CMSet[Token]], Ptr[CMSet[Token]]]
            + ...
            + given Conversion[MSet[DAP.Token[Id, Place]], Ptr[CMSet[Token]]]
            + given Conversion[..., ...]
        }
        NativeCTMCApi <|-- BasicNativeCTMCApi
        NativeCTMCApi <|-- NativeDAPApi
        NativeDAPApi .. NativeDAPBindings
    ```
    - `NativeCTMCApi` is a trait defining the generic native binding for executing a simulation;
      - `BasicNativeCTMCApi` defines the bindings for creating and simulating a basic CTMC model;
      - `NativeDAPApi` defines the bindings for creating and simulating a DAP model;
          - `NativeDAPBindings` includes the necessary conversion utilities for converting Scala types to C types back and forth.
- `dap-native-examples`: contains the native examples (at the moment, only C) for programming using the DAP native library bindings;
  - `*.h` contains the data structures and prototypes definitions;
    - the actual implementations are provided by `@exported` methods in scala native module.
    - for generic types, opaque pointers are used. The scala code only knows the generic type is a pointer to an opaque structure, hence it can only pass it around without knowing its internals, while the C code knows the actual implementation of the structure and can properly interact with it.
      - _side effect_: all the operations that need to work on the internals of the generic types must be implemented in C and make them available to the scala code (see `@extern` in scala native). This is necessary, for example, to implement the distribution layer for (un)marshalling the data structures
- `dap-jvm-examples`: contains the JVM examples for programming using the DAP JVM library.

### How to run examples

At the moment two C examples are available:

- `ctmc.c`: a basic example of a continuous-time Markov chain;
- `gossip.c`: example of gossiping simulation using the DAP library.

To execute them:

```bash
git clone https://github.com/tassiluca/dap.git
cd ./dap/dap-native-examples/src/main/c
make
./<executable-name>.exe
```

where `<executable-name>` is either `ctmc` or `gossip`.

[Expected outcome](https://github.com/tassiluca/dap/actions/runs/12787902057/job/35648073751#step:7:94).

### Known issues (and their solutions)

- On MacOs it is necessary to increase the Garbage Collector initial heap size with:

    ```bash
    export GC_INITIAL_HEAP_SIZE=512M
    ```

  512 MB should be sufficient to run successfully the examples.
