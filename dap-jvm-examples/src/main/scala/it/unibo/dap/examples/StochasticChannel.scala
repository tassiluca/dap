package it.unibo.dap.examples

import it.unibo.dap.model.dsl.CTMCDsl
import it.unibo.dap.utils.TimeOps

object StochasticChannel:

  enum State:
    case IDLE, SEND, DONE, FAIL;

  export State.*
  export CTMCDsl.*

  def stocChannel: CTMC[State] = CTMC.ofTransitions(
    IDLE -- 1.0 --> SEND,
    SEND -- 100_000.0 --> SEND,
    SEND -- 200_000.0 --> DONE,
    SEND -- 100_000.0 --> FAIL,
    FAIL -- 100_000.0 --> IDLE,
    DONE -- 1.0 --> DONE,
  )

@main def mainStochasticChannel(): Unit =
  import StochasticChannel.*
  State.values.foreach(s => println(s"$s,${stocChannel.transitions(s)}"))

@main def mainStochasticChannelSimulation(): Unit =
  import java.util.Random
  import StochasticChannel.*
  import it.unibo.dap.utils.TimeOps.*
  timed:
    println:
      stocChannel
        .simulate(IDLE)(using Random())
        .take(10)
        .toList
        .mkString("\n")
