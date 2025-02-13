package it.unibo.dap.examples

object StochasticChannel:

  enum State:
    case IDLE, SEND, DONE, FAIL;

  export State.*
  export it.unibo.dap.dsl.CTMCDsl.*

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
  import it.unibo.dap.utils.Time
  Time.timed:
    println:
      stocChannel
        .simulate(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")
