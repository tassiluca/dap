package it.unibo.dap.api

trait ProductApi extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import scala.concurrent.ExecutionContext
    export ProductADTsConversions.{ *, given }

    given ExecutionContext = compiletime.deferred

    override def simulate[Token: {Serializable, Equatable}](
        rules: Set[Rule[Token]],
        initial: State[Token],
        updateFn: State[Token] => Unit,
    )(port: Int, neighbors: Set[Neighbor]): Unit =
      SocketBasedDAPSimulation
        .withStaticNeighbors(initial.as, rules.map(given_Conversion_Rule_Rule), neighbors)
        .launch(port, updateFn)

    object ProductADTsConversions:

      export it.unibo.dap.utils.{ as, back, Iso }
      import it.unibo.dap.model

      given [T] => Iso[MSet[T], model.MSet[T]] = Iso(m => model.MSet(m.elems*), m => MSet(m.asList*))

      given [Token] => Iso[State[Token], model.DAP.State[Token]] =
        Iso(s => model.DAP.State(s.tokens.as, s.msg), s => State(s.tokens.back, s.msg))

      given [Token] => Conversion[State[Token] => Unit, model.DAP.State[Token] => Unit] = f => s => f(s.back)

      given [Token] => Conversion[Rule[Token], model.DAP.Rule[Token]] = r =>
        model.DAP.Rule(r.pre.as, _ => r.rate, r.eff.as, r.msg)
    end ProductADTsConversions
  end ProductInterface

end ProductApi
