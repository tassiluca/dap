from enum import Enum
from typing import List, Tuple
from lib import dap_cffi

ffi = dap_cffi.ffi
lib = dap_cffi.lib

class StateType(Enum):
    IDLE = "IDLE"
    SEND = "SEND"
    DONE = "DONE"
    FAIL = "FAIL"

class State:
    def __init__(self, state: StateType) -> None:
        self.c_struct = ffi.new("struct State *")
        self.name_str = ffi.new("char[]", state.value.encode())
        self.c_struct.name = self.name_str

    def get_name(self) -> str:
        return ffi.string(self.c_struct.name).decode()

class Action:
    def __init__(self, rate: float, target_state: State):
        self.c_struct = ffi.new("Action *")
        self.c_struct.rate = rate
        self.state = target_state
        self.c_struct.state = self.state.c_struct

    def get_rate(self) -> float:
        return self.c_struct.rate

    def get_state_name(self) -> str:
        return self.state.get_name()
    
class Transition:
    def __init__(self, from_state: State, action: Tuple[float, State]) -> None:
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
    
    def simulate(self, initial_state: State, steps: int):
        ctmc_ptr = ffi.new("CTMC *")
        ctmc_ptr[0] = self.ctmc
        trace_ptr = lib.simulate_ctmc(ctmc_ptr, initial_state.c_struct, steps)
        events_ptr = trace_ptr.events
        events = ffi.unpack(events_ptr, steps)
        for e in events:
            print(f"Time: {e.time} - State: {ffi.string(e.state.name).decode()}")

idle = State(StateType.IDLE)
send = State(StateType.SEND)
done = State(StateType.DONE)
fail = State(StateType.FAIL)

ctmc = CTMC.of_transitions(
    Transition(idle, (1.0, send)),
    Transition(send, (100_000.0, send)),
    Transition(send, (200_000.0, done)),
    Transition(send, (100_000.0, fail)),
    Transition(fail, (100_000.0, idle)),
    Transition(done, (1.0, done))
)
ctmc.simulate(idle, 10)
