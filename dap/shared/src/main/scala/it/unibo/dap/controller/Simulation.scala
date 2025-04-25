package it.unibo.dap.controller

import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.experimental.betterFors
import scala.concurrent.duration.DurationDouble

import it.unibo.dap.utils.{ unit, Async }
import it.unibo.dap.model.Simulatable.Event
import it.unibo.dap.model.Simulatable

/** A distributed simulation.
  * @tparam B the [[Simulatable]] behaviour to simulate
  * @tparam T the type of the messages to exchange
  * @tparam S the [[DistributableState]] on which the simulation can be
  */
trait Simulation[B[_]: Simulatable, T, S: DistributableState[T]]:
  ctx: ExchangeComponent[T] =>

  /** A simulation explanatory error message. */
  type SimulationError = String

  /** The initial state of the simulation. */
  def initial: S

  /** The behavior to simulate. */
  def behavior: B[S]

  private val isRunning = AtomicBoolean(false)

  /** Stops the simulation at the first possible round. */
  def stop(): Either[SimulationError, Unit] = Either
    .cond(isRunning.compareAndSet(true, false), (), "Simulation is not running.")
    .map(_ => exchange.close())

  /** Launches the simulation. */
  def launch(conf: ctx.Configuration, updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    if isRunning.compareAndSet(false, true) then
      val tasks = loop(initial, updateFn) :: ctx.exchange.spawn(conf) :: Nil
      Future.sequence(tasks).unit
    else Future.failed(IllegalStateException("Simulation is already running."))

  private def loop(state: S, updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    for
      event = behavior.simulateStep(state)(using Random())
      _ <- Async.operations.sleep(event.time.seconds)
      if isRunning.get()
      _ <- updateLogic(event, updateFn)
    yield ()

  private def updateLogic(event: Event[S], updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    updateFn(event.state)
    event.state.msg.foreach(ctx.exchange.outputs.push)
    val in = ctx.exchange.inputs.poll()
    val newState = in.fold(event.state)(event.state.updated)
    loop(newState, updateFn)
end Simulation
