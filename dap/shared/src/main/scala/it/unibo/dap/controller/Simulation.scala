package it.unibo.dap.controller

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.{ Async, AsyncOperations, Future, Task }
import it.unibo.dap.modelling.Simulatable

import java.util.{ Deque, Random }
import java.util.concurrent.ConcurrentLinkedDeque
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

trait Simulation[B[_]: Simulatable, T, S: DistributableState[T]]:
  boundary: ExchangeComponent[T] =>

  def initial: S

  def behavior: B[S]

  def launch(updateFn: S => Unit)(using Async.Spawn, AsyncOperations): Unit =
    val queue = ConcurrentLinkedDeque[T]()
    (
      Task(boundary.exchange.inputs.read().foreach(queue.add)).schedule(RepeatUntilFailure()).start() ::
        Future(boundary.exchange.start) ::
        Future(loop(queue, initial, updateFn)) ::
        Nil
    ).awaitAll

  @tailrec
  private final def loop(queue: Deque[T], state: S, updateFn: S => Unit)(using Async.Spawn, AsyncOperations): Unit =
    val event = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(event.time.seconds)
    updateFn(event.state)
    event.state.msg.foreach(boundary.exchange.outputs.send)
    val in = Option(queue.poll())
    val newState = in.fold(event.state)(event.state.updated)
    loop(queue, newState, updateFn)
end Simulation
