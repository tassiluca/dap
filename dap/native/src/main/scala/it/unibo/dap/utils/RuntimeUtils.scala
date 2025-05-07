package it.unibo.dap.utils

import scala.compiletime.summonFrom
import scala.reflect.ClassTag
import scala.scalanative.unsafe.Tag

/** Performs a conversion from G[T] to H[T] giving the runtime information of T,
  * searching for them in the context. If those cannot be found an error is thrown.
  */
inline def withRuntimeInfo[T, G[_], H[_]](h: G[T])(conversion: Tag[T] ?=> ClassTag[T] ?=> G[T] => H[T]): H[T] =
  summonFrom:
    case _: ClassTag[T] =>
      summonFrom:
        case _: Tag[T] => conversion(h)
        case _ => error(s"A ClassTag is required to perform G[T] => H[T] conversion but it wasn't found.")
    case _ => error("An unsafe.Tag is required to perform G[T] => H[T] conversion but it wasn't found.")
