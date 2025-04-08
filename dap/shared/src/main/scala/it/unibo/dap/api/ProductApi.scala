package it.unibo.dap.api

trait ProductAPI extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import scala.concurrent.ExecutionContext
    import scala.reflect.ClassTag
    import it.unibo.dap.api.resolvers.{ EquatablesResolver, SerDeResolver }
    import it.unibo.dap.modelling.Equatable
    export ProductADTsConversions.given

    given ClassTag[Token] = compiletime.deferred
    given ExecutionContext = compiletime.deferred

    protected val serializerRegistry = SerDeResolver()
    protected val equatablesRegistry = EquatablesResolver()

    override def registerSerDe[T: ClassTag](serialize: T => Array[Byte], deserialize: Array[Byte] => T): Unit =
      serializerRegistry.register[T](serialize, deserialize)

    override def registerEquatable[T: ClassTag](equals: (T, T) => Boolean): Unit =
      equatablesRegistry.register[T](equals(_, _))

    object ProductADTsConversions:

      import it.unibo.dap.utils.{ as, back, Iso }
      import it.unibo.dap.modelling
      import it.unibo.dap.modelling.DAP

      given [T] => Iso[MSet[T], modelling.MSet[T]] = Iso(m => modelling.MSet(m.elems*), m => MSet(m.asList*))

      given Iso[State, DAP.State[Token]] = Iso(s => DAP.State(s.tokens.as, s.msg), s => State(s.tokens.back, s.msg))

      given Conversion[State => Unit, DAP.State[Token] => Unit] = f => s => f(s.back)

      given rCvt: Conversion[Rule, DAP.Rule[Token]] = r => DAP.Rule(r.pre.as, _ => r.rate, r.eff.as, r.msg)
    end ProductADTsConversions
  end ProductInterface

end ProductAPI
