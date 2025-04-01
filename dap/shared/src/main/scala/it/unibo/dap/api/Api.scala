package it.unibo.dap.api

/** The library entry-point language- and platform-agnostic API. */
trait Api:

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs:
    type Token
    type Neighbour = String
    case class MSet[T](elems: T*)
    case class Rule(pre: MSet[Token], rate: Double, eff: MSet[Token], msg: Option[Token])
    case class State(tokens: MSet[Token], msg: Option[Token])

  /** The API interface with which platform-specific code interacts. It needs to be mixed-in with the [[ADTs]]. */
  trait Interface:
    ctx: ADTs =>

    import scala.reflect.ClassTag

    def simulate(rules: Set[Rule], initial: State, updateFn: State => Unit)(port: Int, neighbours: Set[Neighbour]): Unit

    def registerSerDe[T: ClassTag](serializer: T => Array[Byte], deserializer: Array[Byte] => T): Unit

    def registerEquatable[T: ClassTag](equalizer: (T, T) => Boolean): Unit
end Api
