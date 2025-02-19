package it.unibo.dap.controller

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.{ Async, AsyncOperations, Future, Task }
import it.unibo.dap.modelling.Simulatable

import java.util.Random
import java.util.concurrent.{ BlockingDeque, LinkedBlockingDeque }
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

class DistributedSimulation[T](boundary: Exchange[T]):

  def of[B[_]: Simulatable, S: DistributableState[T]](
      initial: S,
      behavior: B[S],
  )(using Async.Spawn, AsyncOperations): Unit =
    val queue = LinkedBlockingDeque[T]()
    val all = Task(boundary.inputs.read().foreach(queue.add)).schedule(RepeatUntilFailure()).start() ::
      Future(watchDog(queue)) ::
      Future(loop(queue, behavior, initial)) ::
      Nil
    all.awaitAll

  @tailrec
  private final def loop[B[_]: Simulatable, S: DistributableState[T]](
      queue: BlockingDeque[T],
      behavior: B[S],
      state: S,
  )(using Async.Spawn, AsyncOperations): Unit =
    val event = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(event.time.seconds)
    boundary.outputs.send(event.state.msg)
    val in = Option(queue.poll())
    val newState = in.fold(event.state)(event.state.updated)
    scribe.info(s"New state: $newState")
    loop(queue, behavior, newState)

  private def watchDog(queue: BlockingDeque[?])(using Async, AsyncOperations): Unit =
    while true do
      AsyncOperations.sleep(5.second)
      scribe.info(s"[WatchDog] Accumulated msgs: ${queue.size()}")

end DistributedSimulation
