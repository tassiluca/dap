package it.unibo.dap.utils

import scala.concurrent.{ ExecutionContext, Future }

/** A concurrent task, as a lazy computation that can be executed in a separate thread.
  * @tparam T the type of the result of the task
  */
trait Task[T] extends (() => Future[T])

/** A spawnable entity, i.e., an entity that can be [[spawn]]ed in a separate thread
  * using a [[Configuration]] to configure the execution environment.
  */
trait Spawnable[Configuration]:
  def spawn(configuration: Configuration)(using ExecutionContext): Future[Unit]

/** A simple [[Spawnable]] entity that does not require any configuration. */
trait PlainSpawnable extends Spawnable[Unit]
