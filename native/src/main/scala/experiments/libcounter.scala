package experiments

import scala.scalanative.unsafe.{CQuote, CString, exportAccessors, exported}

object libcounter:
  @exportAccessors("libcounter_current_count", "libcounter_set_counter")
  var counter: Int = 0

  @exportAccessors("error_message")
  val ErrorMessage: CString = c"Something bad just happend!"

  @exported
  def addLongs(l: Long, r: Long): Long = l + r

  @exported("libcounter_addInts")
  def addInts(l: Int, r: Int): Int = l + r
end libcounter
