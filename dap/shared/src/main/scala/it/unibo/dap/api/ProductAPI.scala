package it.unibo.dap.api

trait ProductAPI extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import scala.concurrent.ExecutionContext
    import scala.reflect.ClassTag
    import ProductADTsConversions.given
    import it.unibo.dap.utils.as
    import it.unibo.dap.api.capabilities.{ EquatablesRegistry, SerializerRegistry }
    import it.unibo.dap.modelling.Equatable
    import it.unibo.dap.boundary.Serializable

    given ClassTag[Token] = compiletime.deferred
    given ExecutionContext = compiletime.deferred

    private val serializerRegistry = SerializerRegistry()
    private val equatablesRegistry = EquatablesRegistry()

    override def simulate(rules: Set[Rule], s0: State, updateFn: State => Unit)(
        port: Int,
        neighbors: Set[Neighbour],
    ): Unit =
      given Serializable[Token] = serializerRegistry
        .of[Token]
        .getOrElse(throw IllegalArgumentException("Token serializer not found"))
      given Equatable[Token] = equatablesRegistry
        .of[Token]
        .getOrElse(throw IllegalArgumentException("Token equalizer not found"))
      DAPSimulation(s0.as, rules.map(rCvt))(port, neighbors).launch(updateFn)()

    override def registerSerDe[T: ClassTag](serialize: T => Array[Byte], deserialize: Array[Byte] => T): Unit =
      serializerRegistry.register[T](serialize, deserialize)

    override def registerEquatable[T: ClassTag](equals: (T, T) => Boolean): Unit =
      equatablesRegistry.register[T](equals(_, _))

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
