package dap.examples

import dap.shared.utils.Time
import java.util.Random
import dap.examples.StochasticChannel.*

@main def mainStochasticChannelSimulation(): Unit =
  Time.timed:
    println:
      stocChannel.newSimulationTrace(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")
