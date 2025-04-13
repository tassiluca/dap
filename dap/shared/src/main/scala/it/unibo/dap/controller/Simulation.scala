package it.unibo.dap.controller

import java.util.Random
import it.unibo.dap.model.Simulatable
import it.unibo.dap.model.Simulatable.Event
import it.unibo.dap.utils.Async

import scala.concurrent.duration.DurationDouble
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
    val tasks = loop(initial, updateFn) :: ctx.exchange.spawn :: Nil
    Future.sequence(tasks).map(_ => ())

  private final def loop(state: S, updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    for
      event = behavior.simulateStep(state)(using Random())
      _ <- Async.operations.sleep(event.time.seconds)
      _ <- updateLogic(event, updateFn)
    yield ()

  private final def updateLogic(event: Event[S], updateFn: S => Unit)(using ExecutionContext): Future[Unit] =
    updateFn(event.state)
    event.state.msg.foreach(ctx.exchange.outputs.push)
    val in = ctx.exchange.inputs.poll()
    val newState = in.fold(event.state)(event.state.updated)
    loop(newState, updateFn)
end Simulation
