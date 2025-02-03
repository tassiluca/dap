import weakref
from enum import Enum
from typing import Tuple, List

from dsl import State, Trace, Event
from lib import ctmc_cffi

ffi = ctmc_cffi.ffi
lib = ctmc_cffi.lib

class StateType(Enum):
    IDLE = "IDLE"
    SEND = "SEND"
    DONE = "DONE"
    FAIL = "FAIL"

class CTMCState(State):
    _weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, c_state):
        self.c_struct = c_state
        self.name = ffi.string(self.c_struct.name).decode()

    @classmethod
    def of_type(cls, state_type: StateType):
        c_struct = ffi.new("struct State *")
        name_str = ffi.new("char[]", state_type.value.encode())
        # !Important! to avoid garbage collection free memory (see https://cffi.readthedocs.io/en/stable/using.html)
        cls._weak_refs[c_struct] = name_str
        c_struct.name = name_str
        return cls(c_struct)

    def __str__(self) -> str:
        return self.name

class Action:
    def __init__(self, rate: float, target_state: CTMCState):
        self.c_struct = ffi.new("Action *")
        self.c_struct.rate = rate
        self.c_struct.state = target_state.c_struct
        self.state = target_state
        self.rate = rate

class Transition:
    def __init__(self, from_state: CTMCState, action: Tuple[float, CTMCState]):
        rate, to_state = action
        self.initial_state = from_state
        self.action = Action(rate, to_state)
        self.c_struct = ffi.new("Transition *")
        self.c_struct.state = self.initial_state.c_struct
        self.c_struct.action = self.action.c_struct[0]

    def __str__(self):
        return f"Transition({self.initial_state} --{self.action.rate}--> {self.action.state})"

class CTMC:
    def __init__(self, transitions: List[Transition]):
        self.transitions = transitions
        transition_array = ffi.new(f"Transition[{len(transitions)}]", [trn.c_struct[0] for trn in transitions])
        self.ctmc = lib.create_ctmc_from_transitions(transition_array, len(transitions))

    @classmethod
    def of_transitions(cls, *transitions):
        return cls(list(transitions))

    def simulate(self, initial_state: CTMCState, steps: int) -> Trace:
        ctmc_ptr = ffi.new("CTMC *")
        ctmc_ptr[0] = self.ctmc
        trace_ptr = lib.simulate_ctmc(ctmc_ptr, initial_state.c_struct, steps)
        events_ptr = trace_ptr.events
        events = ffi.unpack(events_ptr, steps)
        return Trace([Event(e.time, CTMCState(e.state)) for e in events])
