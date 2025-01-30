from abc import ABC, abstractmethod
from tabulate import tabulate
from typing import List

class State(ABC):
    @abstractmethod
    def __str__(self):
        pass

class Event:
    def __init__(self, time: float, state: State):
        self.time = time
        self.state = state

    def __str__(self):
        return f"Event({self.time}, {str(self.state)})"

class Trace:
    def __init__(self, events: List[Event]):
        self.events = events

    def __str__(self):
        headers = ["Time", "Event Type"]
        data = [[e.time, e.state] for e in self.events]
        return "Simulation trace:\n" + tabulate(data, headers, tablefmt="pretty")
