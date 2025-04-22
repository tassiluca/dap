package it.unibo.dap.utils

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.{ Future, Promise }

trait ReadableChannel[+T]:
  def poll(): Option[T]
  def pop(): Future[T]

trait SendableChannel[-T]:
  def push(item: T): Unit

trait Channel[T] extends ReadableChannel[T] with SendableChannel[T]:
  def asReadable: ReadableChannel[T] = this
  def asSendable: SendableChannel[T] = this

object Channel:

  def apply[T](): Channel[T] = new ChannelImpl[T]()

  def readable[T](): ReadableChannel[T] = apply().asReadable

  def sendable[T](): SendableChannel[T] = apply().asSendable

  private class ChannelImpl[T] extends Channel[T]:
    private val buffer = ConcurrentLinkedQueue[T]()
    private val waiters = ConcurrentLinkedQueue[Promise[T]]()

    override def push(item: T): Unit = Option(waiters.poll()) match
      case Some(waiter) => waiter.success(item)
      case _ => buffer.add(item)

    override def pop(): Future[T] = poll() match
      case Some(item) => Future.successful(item)
      case _ =>
        val promise = Promise[T]()
        waiters.add(promise)
        promise.future

    override def poll(): Option[T] = Option(buffer.poll())
end Channel
