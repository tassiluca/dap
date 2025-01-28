from enum import Enum
from typing import List, Tuple
from lib import dap_cffi
import weakref

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

print("*" * 20)

idle = ffi.new("struct State*")
idle_name = ffi.new("char[]", b"IDLE")
idle.name = idle_name

send = ffi.new("struct State*")
send_name = ffi.new("char[]", b"SEND")
send.name = send_name

done = ffi.new("struct State*")
done_name = ffi.new("char[]", b"DONE")
done.name = done_name

fail = ffi.new("struct State*")
fail_name = ffi.new("char[]", b"FAIL")
fail.name = fail_name

print("*" * 20)

trns = ffi.new("Transition[6]")
trns[0].state = idle
trns[0].action.rate = 1.0
trns[0].action.state = send

trns[1].state = send
trns[1].action.rate = 100_000.0
trns[1].action.state = send

trns[2].state = send
trns[2].action.rate = 200_000.0
trns[2].action.state = done

trns[3].state = send
trns[3].action.rate = 100_000.0
trns[3].action.state = fail

trns[4].state = fail
trns[4].action.rate = 100_000.0
trns[4].action.state = idle

trns[5].state = done
trns[5].action.rate = 1.0
trns[5].action.state = done

ctmc = lib.create_ctmc_from_transitions(trns, 6)

steps = 10
ctmc_ptr = ffi.new("CTMC *")
ctmc_ptr[0] = ctmc
trace_ptr = lib.simulate_ctmc(ctmc_ptr, idle, steps)
events_ptr = trace_ptr.events
events = ffi.unpack(events_ptr, steps)
for e in events:
    print(f"Time: {e.time} - State: {ffi.string(e.state.name).decode()}")



# transitions = ffi.new("Transition[1]")
# transitions[0].state = idle
# transitions[0].action.rate = 1.0
# transitions[0].action.state = idle

# Call the C function to create the CTMC
#ctmc = lib.create_ctmc_from_transitions(transitions, 1)
###############################

# class CTMC:
#     def __init__(self, transitions: List[Transition]):
#         self.transitions = transitions
#         self.ctmc = ffi.new("CTMC *")
#         transition_array = ffi.new("Transition[]", [transition.c_struct[0] for transition in transitions])
#         self.ctmc = lib.create_ctmc_from_transitions(transition_array, len(transitions))
#         print("get from lib")
#         print(self.ctmc)

#     @classmethod
#     def of_transitions(cls, *transitions):
#         return cls(list(transitions))

#     def __repr__(self):
#         return "\n".join(map(str, self.transitions))

#     def simulate(self, state: StateType, steps: int):
#         print("*" * 20)
#         initial_state = State(state)
#         trace_ptr = lib.simulate_ctmc(ctmc_ptr, initial_state.c_struct, steps)
#         print(trace_ptr)
#         print("OK")
#         trace = ffi.unpack(trace_ptr[0], steps)
#         events = []
#         print(trace)
#         print("Are you sure?")
#         # Access events from the trace
#         for i in range(steps):
#             event = ffi.cast("Event *", trace_ptr[i])
#             events.append({
#                 'time': event.time,
#                 'state': ffi.string(event.state.name).decode()
#             })
#         return events

# print("# ------------------- Example Usage -------------------")

# trns = [
#     Transition(idle_state, (1.0, send_state)),
#     Transition(send_state, (100_000.0, send_state)),
#     Transition(send_state, (200_000.0, done_state)),
#     Transition(send_state, (100_000.0, fail_state)),
#     Transition(fail_state, (100_000.0, idle_state)),
#     Transition(done_state, (1.0, done_state)),
# ]
# print("TRANSITIONS")
# [print(trn) for trn in trns]

# print("# ----------------- Example Simulation -----------------")

# transition_array = ffi.new("Transition[6]", [trn.c_struct[0] for trn in trns])
# print(transition_array)
# [print(trn.state) for trn in transition_array]
# ctmc = lib.create_ctmc_from_transitions(transition_array, len(trns))
# print("Got ", ctmc)

# initial_state = State(StateType.IDLE)
# ctmc_ptr = ffi.new("CTMC *", ctmc)

# trace_ptr = lib.simulate_ctmc(ctmc_ptr, initial_state.c_struct, 10)
# print("hoping to get trace ", trace_ptr)

# Simulate the CTMC process for 10 steps starting from IDLE
# trace_events = stocChannel.simulate(StateType.IDLE, 10)

# Print trace events
#for event in trace_events:
#    print(f"Time: {event['time']} - State: {event['state']}")
