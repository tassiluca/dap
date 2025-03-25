package it.unibo.dap.api

trait Api:

  val interface: Interface

  trait ADTs:
    type Token
    type Neighbour = String
    case class MSet[T](elems: T*)
    case class Rule(pre: MSet[Token], rate: Double, eff: MSet[Token], msg: Option[Token])
    case class State(tokens: MSet[Token], msg: Option[Token])

  trait Interface:
    ctx: ADTs =>

    def simulate(
        rules: Set[Rule],
        initial: State,
        updateFn: State => Unit,
    )(port: Int, neighbours: Set[Neighbour]): Unit

    def registerSerDe(typeName: String, serializer: AnyRef => Array[Byte], deserializer: Array[Byte] => AnyRef): Unit

    def registerEquatable(typeName: String, equalizer: (AnyRef, AnyRef) => Boolean): Unit
end Api
