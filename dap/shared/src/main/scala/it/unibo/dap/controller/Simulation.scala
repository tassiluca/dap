package it.unibo.dap.controller

import java.util.{ Deque, Random }
import java.util.concurrent.ConcurrentLinkedDeque
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble
import it.unibo.dap.modelling.Simulatable
import it.unibo.dap.utils.Task

import scala.collection.Iterator.continually
import scala.concurrent.{ ExecutionContext, Future }

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

  def launch(updateFn: S => Unit)(using ExecutionContext): Task[Unit] = () =>
    val queue = ConcurrentLinkedDeque[T]()
    val all = Future(continually(boundary.exchange.inputs.take()).foreach(queue.add)) ::
      Future(loop(queue, initial, updateFn)) ::
      boundary.exchange.spawn() ::
      Nil
    Future.sequence(all).map(_ => ())

  @tailrec
  private final def loop(queue: Deque[T], state: S, updateFn: S => Unit): Unit =
    scribe.info(s"[Sim] Simulating step for state: $state")
    val event = behavior.simulateStep(state)(using Random())
    Thread.sleep(event.time.seconds.toMillis)
    updateFn(event.state)
    event.state.msg.foreach(boundary.exchange.outputs.offer)
    val in = Option(queue.poll())
    val newState = in.fold(event.state)(event.state.updated)
    loop(queue, newState, updateFn)
end Simulation
