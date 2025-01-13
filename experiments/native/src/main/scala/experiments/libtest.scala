package experiments

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ exported, sizeOf, CDouble, CInt, CSize, CStruct0, CStruct2, Ptr }
import scala.scalanative.unsafe.Size.intToSize

object libtest:

  type State = Ptr[CStruct0]
  type Event = CStruct2[CDouble, State]
  type Trace = CStruct2[Ptr[Event], CSize]

  given Bindable[Ptr[Event], MyEvent[State]] = MyEvent.bind

  @exported
  def test(state: State, times: CInt): Ptr[Trace] =
    val trace = stdlib.malloc(sizeOf[Trace]).asInstanceOf[Ptr[Trace]]
    val events = stdlib.malloc(sizeOf[Event] * times).asInstanceOf[Ptr[Event]]
    for i <- 0 until times do
      val ev = MyEvent[State](i * 2, state)
      val evPtr = events + i
      !evPtr = !ev.from
//      val event = events(i)
//      event._1 = ev.time
//      event._2 = ev.state
    trace._1 = events
    trace._2 = times.toCSize
    trace

end libtest

trait Bindable[T, F]:
  extension (t: T) def to: F
  extension (f: F) def from: T

trait MyEvent[+T]:
  def time: Double
  def state: T

object MyEvent:
  def apply[T](time: Double, state: T): MyEvent[T] = EventImpl(time, state)
  private case class EventImpl[T](time: Double, state: T) extends MyEvent[T]

  val bind = new Bindable[Ptr[libtest.Event], MyEvent[libtest.State]]:
    extension (f: MyEvent[libtest.State])
      override def from: Ptr[libtest.Event] =
        val ptr = stdlib.malloc(sizeOf[libtest.Event]).asInstanceOf[Ptr[libtest.Event]]
        ptr._1 = f.time
        ptr._2 = f.state
        ptr
    extension (t: Ptr[libtest.Event]) override def to: MyEvent[libtest.State] = MyEvent[libtest.State](t._1, t._2)
