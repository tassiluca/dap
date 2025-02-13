package dap.examples

object StochasticChannel:

  enum State:
    case IDLE, SEND, DONE, FAIL;

  import dap.shared.dsl.CTMCDsl.*
  export State.*

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
  import dap.shared.utils.Time
  import StochasticChannel.*
  Time.timed:
    println:
      stocChannel
        .simulate(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")
