from dsl.ctmc import CTMCState, StateType, Transition, CTMC

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
