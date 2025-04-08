package it.unibo.dap.controller

import java.util.Random
import scala.annotation.tailrec
import it.unibo.dap.modelling.Simulatable
import it.unibo.dap.utils.PlatformSleep

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }

/** A distributed simulation.
  * @tparam B the [[Simulatable]] behaviour to simulate
  * @tparam T the type of the messages to exchange
  * @tparam S the [[DistributableState]] on which the simulation can be
  */
trait Simulation[B[_]: Simulatable, T, S: DistributableState[T]]:
  ctx: ExchangeComponent[T] =>

  /** The initial state of the simulation. */
  def initial: S

  /** The behaviour to simulate. */
  def behavior: B[S]

  /** Launches the simulation. */
  def launch(updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    val tasks = Future(loop(initial, updateFn)) :: ctx.exchange.spawn :: Nil
    Future.sequence(tasks).map(_ => ())

  @tailrec
  private final def loop(state: S, updateFn: S => Unit): Unit =
    scribe.info(s"[Sim] Simulating step for state: $state")
    val event = behavior.simulateStep(state)(using Random())
    PlatformSleep().sleep(Duration(event.time, TimeUnit.SECONDS))
    updateFn(event.state)
    event.state.msg.foreach(ctx.exchange.outputs.push)
    val in = ctx.exchange.inputs.poll()
    val newState = in.fold(event.state)(event.state.updated)
    loop(newState, updateFn)
end Simulation
