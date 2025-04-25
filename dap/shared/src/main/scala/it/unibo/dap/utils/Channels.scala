package it.unibo.dap.utils

import scala.concurrent.{ Future, Promise }
import scala.util.Try

trait ReadableChannel[+T]:
  def poll(): Option[T]
  def pop(): Future[T]
  def close(): Either[Channel.Error, Unit]

trait SendableChannel[-T]:
  def push(item: T): Either[Channel.Error, Unit]
  def close(): Either[Channel.Error, Unit]

trait Channel[T] extends ReadableChannel[T] with SendableChannel[T]:
  def asReadable: ReadableChannel[T] = this
  def asSendable: SendableChannel[T] = this

object Channel:

  type Error = String

  def apply[T](): Channel[T] = new ChannelImpl[T]()

  def readable[T](): ReadableChannel[T] = apply().asReadable

  def sendable[T](): SendableChannel[T] = apply().asSendable

  private class ChannelImpl[T] extends Channel[T]:
    private var closed = false
    private val buffer = scala.collection.mutable.Queue.empty[T]
    private val waiters = scala.collection.mutable.Queue.empty[Promise[T]]

    override def push(item: T): Either[Channel.Error, Unit] = synchronized:
      if closed then Left("Channel is closed. No more items can be pushed.")
      else if waiters.nonEmpty then Try(waiters.dequeue().success(item)).toEither.left.map(_.getMessage).unit
      else Right(buffer.enqueue(item))

    override def pop(): Future[T] = synchronized:
      poll() match
        case Some(item) => Future.successful(item)
        case _ if closed => Future.failed(new NoSuchElementException("Channel is closed and no items are available."))
        case _ =>
          val promise = Promise[T]()
          waiters.enqueue(promise)
          promise.future

    override def poll(): Option[T] = synchronized:
      Option.when(buffer.nonEmpty)(buffer.dequeue())

    override def close(): Either[Error, Unit] = synchronized:
      if closed then Left("Channel is already closed.")
      else
        closed = true
        waiters.foreach(_.failure(new NoSuchElementException("Channel is closed.")))
        waiters.clear()
        Right(())
  end ChannelImpl
end Channel
