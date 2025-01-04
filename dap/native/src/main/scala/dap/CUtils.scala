package dap

import scala.reflect.ClassTag
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ sizeOf, Ptr }

@SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.asInstanceOf"))
object CUtils:

  def freshPointer[T](factor: Int = 1)(using ClassTag[T]): Ptr[T] =
    val ptr = stdlib.malloc(sizeOf[T] * factor).asInstanceOf[Ptr[T]]
    if ptr == null then throw new OutOfMemoryError("Failed to allocate memory") else ptr

  def requireNonNull[T](obj: T): T =
    if obj == null then throw new NullPointerException(s"Object $obj is null") else obj
