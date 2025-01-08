package experiments

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ exported, sizeOf, CDouble, CInt, CSize, CStruct0, CStruct2, Ptr }
import scala.scalanative.unsafe.Size.intToSize

object libtest:

  type State = Ptr[CStruct0]
  type Event = CStruct2[CDouble, State]
  type Trace = CStruct2[Ptr[Event], CSize]

  @exported
  def test(state: State, times: CInt): Ptr[Trace] =
    val trace = stdlib.malloc(sizeOf[Trace]).asInstanceOf[Ptr[Trace]]
    val events = stdlib.malloc(sizeOf[Event] * times).asInstanceOf[Ptr[Event]]
    for i <- 0 until times do
      val event = events(i)
      event._1 = i.toDouble
      event._2 = state
    trace._1 = events
    trace._2 = times.toCSize
    trace
