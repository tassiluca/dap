package it.unibo.dap.controller

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.{ Async, AsyncOperations, Future, Task }
import it.unibo.dap.modelling.Simulatable

import java.util
import java.util.Random
import java.util.concurrent.ConcurrentLinkedDeque
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

trait Simulation[B[_]: Simulatable, T, S: DistributableState[T]]:
  boundary: ExchangeComponent[T] =>

  def initial: S

  def behavior: B[S]

  def launch(using Async.Spawn, AsyncOperations): Unit =
    val queue = ConcurrentLinkedDeque[T]()
    val all = Task(boundary.exchange.inputs.read().foreach(queue.add)).schedule(RepeatUntilFailure()).start() ::
      Future(boundary.exchange.start) ::
      Future(loop(queue, initial)) ::
      Nil
    all.awaitAll

  @tailrec
  private final def loop(queue: util.Deque[T], state: S)(using Async.Spawn, AsyncOperations): Unit =
    val event = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(if event.time != 0 then event.time.seconds else 1.seconds)
    scribe.info("Event: " + event)
    event.state.msg.foreach(boundary.exchange.outputs.send)
    scribe.info("Sent: " + event.state.msg)
    val in = Option(queue.poll())
    scribe.info("Received: " + in)
    val newState = in.fold(event.state)(event.state.updated)
    scribe.info(s"New state: $newState")
    loop(queue, newState)
end Simulation
