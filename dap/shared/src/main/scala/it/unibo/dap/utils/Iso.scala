package it.unibo.dap.utils

/** An isomorphism between two types A and B. */
trait Iso[A, B]:
  def to(a: A): B
  def from(b: B): A

object Iso:

  inline def apply[A, B](inline toFn: A => B, inline fromFn: B => A): Iso[A, B] = IsoImpl(toFn, fromFn)

  class IsoImpl[A, B](val toFn: A => B, val fromFn: B => A) extends Iso[A, B]:
    override def to(a: A): B = toFn(a)
    override def from(b: B): A = fromFn(b)

  given [A, B](using iso: Iso[A, B]): Conversion[A, B] with
    inline def apply(a: A): B = iso.to(a)

  given [A, B](using iso: Iso[A, B]): Conversion[B, A] with
    inline def apply(b: B): A = iso.from(b)
