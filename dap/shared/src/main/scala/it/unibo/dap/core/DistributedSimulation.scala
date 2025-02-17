package it.unibo.dap.core

import gears.async.{ AsyncOperations, Task }
import it.unibo.dap.modelling.{ DistributedState, Simulatable }

import java.util.Random
import java.util.concurrent.BlockingDeque
import scala.concurrent.duration.DurationDouble

trait DistributedSimulation[T]:
  boundary: Exchange[T] =>

  def of[F[_]: Simulatable](
      initial: DistributedState[T],
      behavior: F[DistributedState[T]],
  ): Task[Unit] = ???

  def loop[F[_]: Simulatable](
      queue: BlockingDeque[T],
      behavior: F[DistributedState[T]],
      state: DistributedState[T],
  ): Task[Unit] = Task:
    val nextEvent = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(nextEvent.time.seconds)
    // send to each neighbor the state messages
    val in = Option(queue.poll())
    val newState = in.fold(state)(state.update)
    loop(queue, behavior, newState)
