package it.unibo.dap.api

import it.unibo.dap.modelling.Equatable

trait ProductAPI extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import ProductADTsConversions.given
    import gears.async.Async
    import gears.async.default.given
    import it.unibo.dap.utils.as
    import it.unibo.dap.api.capabilities.{ EquatablesRegistry, SerializerRegistry }
    import it.unibo.dap.boundary.Serializable

    private val serializerRegistry = SerializerRegistry()
    private val equatablesRegistry = EquatablesRegistry()

    override def simulate(rules: Set[Rule], s0: State, updateFn: State => Unit)(
        port: Int,
        neighbors: Set[Neighbour],
    ): Unit = Async.blocking:
      given Serializable[Token] = serializerRegistry
        .of[Token]("Token")
        .getOrElse(throw IllegalArgumentException("Token serializer not found"))
      given Equatable[Token] = equatablesRegistry
        .of[Token]("Token")
        .getOrElse(throw IllegalArgumentException("Token equalizer not found"))
      DAPSimulation(s0.as, rules.map(rCvt))(port, neighbors).launch(updateFn)

    override def registerSerDe(
        typeName: String,
        serializer: AnyRef => Array[Byte],
        deserializer: Array[Byte] => AnyRef,
    ): Unit = serializerRegistry.register(typeName, serializer, deserializer)

    override def registerEquatable(typeName: String, equalizer: (AnyRef, AnyRef) => Boolean): Unit =
      equatablesRegistry.register(typeName, equalizer)

    private object ProductADTsConversions:

      import it.unibo.dap.utils.{ as, back, Iso }
      import it.unibo.dap.modelling
      import it.unibo.dap.modelling.DAP
      import it.unibo.dap.utils

      given [T] => Iso[MSet[T], modelling.MSet[T]] = Iso(
        m => modelling.MSet(m.elems*),
        m => MSet(m.asList*),
      )

      given Iso[State, DAP.State[Token]] = Iso(
        s => DAP.State(s.tokens.as, s.msg),
        s => State(s.tokens.back, s.msg),
      )

      given Conversion[State => Unit, DAP.State[Token] => Unit] = f => s => f(s.back)

      given rCvt: Conversion[Rule, DAP.Rule[Token]] = r => DAP.Rule(r.pre.as, _ => r.rate, r.eff.as, r.msg)
    end ProductADTsConversions
  end ProductInterface

end ProductAPI
