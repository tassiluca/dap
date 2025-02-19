package it.unibo.dap.core

import gears.async.TaskSchedule.RepeatUntilFailure
import gears.async.{ Async, AsyncOperations, Future, Task }
import it.unibo.dap.modelling.DAP
import it.unibo.dap.modelling.Simulatable
import it.unibo.dap.utils.MSet

import java.util.Random
import java.util.concurrent.{ BlockingDeque, LinkedBlockingDeque }
import scala.annotation.tailrec
import scala.concurrent.duration.DurationDouble

class DistributedSimulation(boundary: Exchange[String]):

  def of[F[_]: Simulatable](
      initial: DAP.State,
      behavior: F[DAP.State],
  )(using Async, AsyncOperations): Unit = Async.group:
    val queue = LinkedBlockingDeque[String]()
    val all = Task(boundary.inputs.read().foreach(queue.add)).schedule(RepeatUntilFailure()).start() ::
      Future(watchDog(queue)) ::
      Future(loop(queue, behavior, initial)) ::
      Nil
    all.awaitAll

  @tailrec
  private final def loop[F[_]: Simulatable](
      queue: BlockingDeque[String],
      behavior: F[DAP.State],
      state: DAP.State,
  )(using Async.Spawn, AsyncOperations): Unit =
    val event = behavior.simulateStep(state, new Random())
    AsyncOperations.sleep(event.time.seconds)
    boundary.outputs.send(event.state.msg)
    val in = Option(queue.poll())
    val newState = in.fold(event.state)(t => DAP.State(event.state.local union MSet(t), ""))
    scribe.info(s"New state: $newState")
    loop(queue, behavior, newState)

  private def watchDog(queue: BlockingDeque[String])(using Async, AsyncOperations): Unit =
    while true do
      AsyncOperations.sleep(5.second)
      scribe.info(s"[WatchDog] Accumulated msgs: ${queue.size()}")

end DistributedSimulation

object DistributedSimulation:

  import it.unibo.dap.sockets.SocketExchange
  import SocketExchange.*

  def create[T](port: Port, network: Set[SocketExchange.Endpoint])(using
      Async.Spawn,
      AsyncOperations,
  ): DistributedSimulation =
    val exchange = SocketExchange(port, network)
    exchange.start
    DistributedSimulation(exchange)
