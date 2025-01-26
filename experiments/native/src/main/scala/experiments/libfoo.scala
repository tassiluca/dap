package experiments

import scala.scalanative.libc.stdio
import scala.scalanative.unsafe.{ exported, CQuote, CStruct0, Ptr }

object libfoo:
  type State = Ptr[CStruct0]

  @exported("pass_around")
  def passAround(state: State): Unit =
    stdio.printf(c"State: %p\n", state)
