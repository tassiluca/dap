package it.unibo.dap.model

object Stochastics:

  import scala.util.Random

  /** `[(p1, a1), ..., (pn, an)] --> [(p1, a1), (p1 + p2, a2), ..., (p1 + ... + pn, an)]`. */
  def cumulative[A](l: List[(Double, A)]): List[(Double, A)] =
    l.tail.scanLeft(l.head):
      case ((r, _), (r2, a2)) => (r + r2, a2)

  /** `(p1, a1), ..., (pn, an) --> ai`, selected randomly and fairly. */
  def draw[A](cumulativeList: List[(Double, A)])(using rnd: Random = Random()): A =
    val rndVal = rnd.nextDouble() * cumulativeList.last._1
    cumulativeList.collectFirst { case (r, a) if r >= rndVal => a }.get
end Stochastics
