package it.unibo.dap.modelling

trait Equatable[T] extends ((T, T) => Boolean)

object Equatable:
  def apply[T](using Equatable[T]): Equatable[T] = summon[Equatable[T]]

extension [T](t1: T) def ===(t2: T)(using Equatable[T]): Boolean = Equatable[T].equals(t1, t2)
