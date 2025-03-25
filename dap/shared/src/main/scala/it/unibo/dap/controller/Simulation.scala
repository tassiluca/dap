package it.unibo.dap.controller

import java.util.{ Deque, Random }
import java.util.concurrent.ConcurrentLinkedDeque

import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

import it.unibo.dap.modelling.Simulatable

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.*

/** A distributed simulation.
  * @tparam B the [[Simulatable]] behaviour to simulate
  * @tparam T the type of the messages to exchange
  * @tparam S the [[DistributableState]] on which the simulation can be
  */
trait Simulation[B[_]: Simulatable, T, S: DistributableState[T]]:
  boundary: ExchangeComponent[T] =>

  /** The initial state of the simulation. */
  def initial: S

  /** The behaviour to simulate. */
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
    scribe.info(s"[Sim] Simulating step for state: $state")
    val event = behavior.simulateStep(state)(using Random())
    scribe.info(s"[Sim] Next event: ${event.state} - ${event.time}")
    AsyncOperations.sleep(event.time.seconds)
    scribe.info("[Sim] Calling update function")
    updateFn(event.state)
    event.state.msg.foreach(boundary.exchange.outputs.send)
    val in = Option(queue.poll())
    val newState = in.fold(event.state)(event.state.updated)
    loop(queue, newState, updateFn)
end Simulation
