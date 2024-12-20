package experiments

import scala.scalanative.libc.{ stdio, stdlib }
import scala.scalanative.unsafe.{ exported, sizeOf, CInt, CQuote, Ptr }

case class SimpleCounter(private var counter: Int = 0):
  def inc(value: Int = 1): SimpleCounter = SimpleCounter(counter + value)
  def value: Int = counter

case class CounterFacade(private var counter: Counter[Int] = Counter.ofIntegers()):
  def inc(value: Int = 1): CounterFacade = CounterFacade(counter.inc(value))
  def value: Int = counter.value

object libadvcounter:

  type IntCounter = CounterFacade

  @exported("create_counter")
  def createCounter(): Ptr[IntCounter] =
    val ptr = freshCounterPtr
    !ptr = CounterFacade()
    ptr

  @exported("dec_counter")
  def decCounter(counter: Ptr[IntCounter], delta: CInt): Ptr[IntCounter] = incCounter(counter, -delta)

  @exported("inc_counter")
  def incCounter(counter: Ptr[IntCounter], delta: CInt): Ptr[IntCounter] =
    requireNonNull(counter)
    val ptr = freshCounterPtr
    !ptr = (!counter).inc(delta)
    ptr

  @exported("counter_value")
  def counterValue(counter: Ptr[IntCounter]): CInt =
    if counter != null then (!counter).value else -1

  private def requireNonNull[T](obj: T): T =
    if obj == null then throw new NullPointerException(s"Object $obj is null") else obj

  private def freshCounterPtr: Ptr[IntCounter] =
    stdio.printf(c"Size of counter: %d\n", sizeOf[CounterFacade])
    stdio.printf(c"Size of simple counter: %d\n", sizeOf[SimpleCounter])
    val ptr: Ptr[IntCounter] = stdlib.malloc(sizeOf[CounterFacade]).asInstanceOf[Ptr[IntCounter]]
    if ptr == null then throw new OutOfMemoryError("Failed to allocate memory for Counter") else ptr

end libadvcounter
