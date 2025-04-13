package it.unibo.dap.model

/** A type class encoding the equality capability for a type `T`, i.e., the
  * ability to compare two instances of `T` for equality.
  */
trait Equatable[T] extends ((T, T) => Boolean)

object Equatable:
  def apply[T](using Equatable[T]): Equatable[T] = summon[Equatable[T]]

extension [T](t1: T) def ===(t2: T)(using eq: Equatable[T]): Boolean = eq(t1, t2)
