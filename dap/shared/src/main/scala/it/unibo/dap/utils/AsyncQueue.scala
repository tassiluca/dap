package it.unibo.dap.utils

import scala.collection.mutable.Queue
import scala.concurrent.{ Future, Promise }

class AsyncQueue[T]:
  private val buffer = Queue.empty[T]
  private var waiting: Option[Promise[T]] = None

  def push(item: T): Unit = this.synchronized:
    if waiting.isDefined then
      val promise = waiting.get
      waiting = None
      promise.success(item)
    else buffer.enqueue(item)

  def pop(): Future[T] = this.synchronized:
    if buffer.nonEmpty then Future.successful(buffer.dequeue())
    else
      val p = Promise[T]()
      waiting = Some(p)
      p.future

  def poll(): Option[T] = this.synchronized:
    if buffer.nonEmpty then Some(buffer.dequeue()) else None
