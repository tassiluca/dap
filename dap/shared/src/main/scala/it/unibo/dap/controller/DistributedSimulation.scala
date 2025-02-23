package it.unibo.dap.controller

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.{ Async, AsyncOperations, Future, Task }
import it.unibo.dap.modelling.Simulatable

import java.util
import java.util.Random
import java.util.concurrent.ConcurrentLinkedDeque
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

class DistributedSimulation[T](boundary: Exchange[T]):

  def launch[B[_]: Simulatable, S: DistributableState[T]](
      initial: S,
      behavior: B[S],
  )(using Async.Spawn, AsyncOperations): Unit =
    val queue = ConcurrentLinkedDeque[T]()
    val all = Task(boundary.inputs.read().foreach(queue.add)).schedule(RepeatUntilFailure()).start() ::
      Future(loop(queue, behavior, initial)) ::
      Nil
    all.awaitAll

  @tailrec
  private final def loop[B[_]: Simulatable, S: DistributableState[T]](
      queue: util.Deque[T],
      behavior: B[S],
      state: S,
  )(using Async.Spawn, AsyncOperations): Unit =
    val event = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(event.time.seconds)
    scribe.info("Event: " + event)
    event.state.msg.foreach(boundary.outputs.send)
    scribe.info("Sent: " + event.state.msg)
    val in = Option(queue.poll())
    scribe.info("Received: " + in)
    val newState = in.fold(event.state)(event.state.updated)
    scribe.info(s"New state: $newState")
    loop(queue, behavior, newState)

end DistributedSimulation
