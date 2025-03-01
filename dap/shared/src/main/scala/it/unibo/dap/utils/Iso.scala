package it.unibo.dap.utils

trait Iso[A, B]:
  def to(a: A): B
  def from(b: B): A

object Iso:

  def apply[A, B](toFn: A => B, fromFn: B => A): Iso[A, B] =
    new Iso[A, B]:
      def to(a: A): B = toFn(a)
      def from(b: B): A = fromFn(b)

extension [A](a: A) def as[B](using iso: Iso[A, B]): B = iso.to(a)

extension [B](b: B) def back[A](using iso: Iso[A, B]): A = iso.from(b)
