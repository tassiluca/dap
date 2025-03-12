package it.unibo.dap.modelling

/** A multiset datatype. */
trait MSet[A] extends (A => Int):

  /** @return a new multiset as the union of `this` and `m`. */
  infix def union(m: MSet[A]): MSet[A]

  /** @return a new multiset as the difference of `this` and `m`. */
  def diff(m: MSet[A])(using Equatable[A]): MSet[A]

  /** @return true if `this` can be [[extract]]ed from `m`, false otherwise. */
  def matches(m: MSet[A])(using Equatable[A]): Boolean

  /** @return a defined `Option` with the result of removing the elements of `m` from `this`
    *         iff all the elements of `m` are present in `this`, otherwise `None` is returned.
    */
  infix def extract(m: MSet[A])(using Equatable[A]): Option[MSet[A]]

  def map[B](f: A => B): MSet[B]
  def flatMap[B](f: A => MSet[B]): MSet[B]
  def filter(f: A => Boolean): MSet[A]
  def collect[B](f: PartialFunction[A, B]): MSet[B]

  def size: Int
  def asList: List[A]
  def asMap: Map[A, Int]
  def iterator: Iterator[A]

end MSet

object MSet:

  def apply[A](l: A*): MSet[A] = new MSetImpl(l.toList)
  def ofList[A](l: List[A]): MSet[A] = new MSetImpl(l)
  def ofMap[A](m: Map[A, Int]): MSet[A] = MSetImpl(m)

  import scala.collection.immutable

  private case class MSetImpl[A](asMap: Map[A, Int]) extends MSet[A]:
    def this(list: List[A]) = this(list.groupBy(a => a).map { case (a, n) => (a, n.size) })

    override val asList: List[A] = asMap.toList.flatMap { case (a, n) => immutable.List.fill(n)(a) }
    override def apply(v1: A): Int = asMap.getOrElse(v1, 0)
    override def union(m: MSet[A]) = new MSetImpl[A](asList ++ m.asList)

    override def diff(m: MSet[A])(using Equatable[A]) = new MSetImpl[A](
      m.asList.foldLeft(asList): (acc, elem) =>
        val index = acc.indexWhere(_ === elem)
        if index >= 0 then acc.patch(index, Nil, 1) else acc,
    )

    override def matches(m: MSet[A])(using Equatable[A]): Boolean = extract(m).isDefined

    override def extract(m: MSet[A])(using Equatable[A]): Option[MSet[A]] =
      Some(this.diff(m)).filter(_.size == size - m.size)

    override def iterator: Iterator[A] = asMap.keysIterator
    override def map[B](f: A => B) = new MSetImpl[B](asList.map(f))
    override def flatMap[B](f: A => MSet[B]) = new MSetImpl[B](asList.flatMap(f(_).asList))
    override def filter(f: A => Boolean) = new MSetImpl[A](asList.filter(f))
    override def collect[B](f: PartialFunction[A, B]) = new MSetImpl[B](asList.collect(f))
    override def size: Int = asList.size
    override def toString = s"{${asList.mkString("|")}}"
  end MSetImpl

end MSet
