package dap

import scala.reflect.ClassTag

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
object CUtils:

  def requireNonNull[T](obj: T): T =
    if obj == null then throw new NullPointerException(s"Object $obj is null") else obj
