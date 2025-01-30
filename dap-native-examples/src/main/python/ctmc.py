import weakref
from enum import Enum
from typing import Tuple

from dsl import *
from lib import ctmc_cffi

ffi = ctmc_cffi.ffi
lib = ctmc_cffi.lib

class StateType(Enum):
    IDLE = "IDLE"
    SEND = "SEND"
    DONE = "DONE"
    FAIL = "FAIL"

class CTMCState(State):
    weak_refs = weakref.WeakKeyDictionary()

    def __init__(self, c_state):
        self.c_struct = c_state

    @classmethod
    def of_type(cls, state_type: StateType):
        c_struct = ffi.new("struct State *")
        name_str = ffi.new("char[]", state_type.value.encode())
        # Unlike C, the returned pointer object has ownership on the allocated memory: 
        # when this exact object is garbage-collected, then the memory is freed. 
        # If, at the level of C, you store a pointer to the memory somewhere else, 
        # then make sure you also keep the object alive for as long as needed. 
        # See https://cffi.readthedocs.io/en/stable/using.html
        cls.weak_refs[c_struct] = name_str
        c_struct.name = name_str
        return cls(c_struct)
    
    @classmethod
    def from_c_struct(cls, c_struct):
        return cls(c_struct)

    def get_name(self) -> str:
        return ffi.string(self.c_struct.name).decode()

    def __str__(self):
        return self.get_name()

class Action:
    def __init__(self, rate: float, target_state: CTMCState):
        self.c_struct = ffi.new("Action *")
        self.c_struct.rate = rate
        self.state = target_state
        self.c_struct.state = self.state.c_struct

    def get_rate(self) -> float:
        return self.c_struct.rate

    def get_state_name(self) -> str:
        return self.state.get_name()
    
class Transition:
    def __init__(self, from_state: CTMCState, action: Tuple[float, CTMCState]) -> None:
        rate, to_state = action
        self.c_struct = ffi.new("Transition *")
        self.state = from_state
        self.action = Action(rate, to_state)
        self.c_struct.state = self.state.c_struct
        self.c_struct.action = self.action.c_struct[0]

    def get_action_rate(self) -> float:
        return self.action.get_rate()

    def get_from_state_name(self) -> str:
        return self.state.get_name()
        
    def get_to_state_name(self) -> str:
        return self.action.get_state_name()
    
    def __str__(self):
        return f"Transition({self.get_from_state_name()} --{self.get_action_rate()}--> {self.get_to_state_name()})"

class CTMC:
    def __init__(self, transitions: List[Transition]):
        self.transitions = transitions
        transition_array = ffi.new(f"Transition[{len(transitions)}]", [trn.c_struct[0] for trn in transitions])
        self.ctmc = lib.create_ctmc_from_transitions(transition_array, len(transitions))

    @classmethod
    def of_transitions(cls, *transitions):
        return cls(list(transitions))
    
    def simulate(self, initial_state: CTMCState, steps: int):
        ctmc_ptr = ffi.new("CTMC *")
        ctmc_ptr[0] = self.ctmc
        trace_ptr = lib.simulate_ctmc(ctmc_ptr, initial_state.c_struct, steps)
        events_ptr = trace_ptr.events
        events = ffi.unpack(events_ptr, steps)
        return Trace([Event(e.time, CTMCState.from_c_struct(e.state)) for e in events])

idle = CTMCState.of_type(StateType.IDLE)
send = CTMCState.of_type(StateType.SEND)
done = CTMCState.of_type(StateType.DONE)
fail = CTMCState.of_type(StateType.FAIL)

ctmc = CTMC.of_transitions(
    Transition(idle, (1.0, send)),
    Transition(send, (100_000.0, send)),
    Transition(send, (200_000.0, done)),
    Transition(send, (100_000.0, fail)),
    Transition(fail, (100_000.0, idle)),
    Transition(done, (1.0, done))
)
trace = ctmc.simulate(idle, 10)
print(trace)
