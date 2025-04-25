package it.unibo.dap.api

trait ProductApi extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import scala.concurrent.ExecutionContext
    export ProductADTsConversions.{ *, given }

    given ExecutionContext = compiletime.deferred

    override def simulation[Token](
        rules: ISeq[Rule[Token]],
        initialState: State[Token],
        neighborhood: ISeq[Neighbor],
        serializer: IFunction1[Token, String],
        deserializer: IFunction1[String, Token],
        equalizer: IFunction2[Token, Token, Boolean],
    ): DASPSimulation[Token] =
      given Equatable[Token] = equalizer(_, _)
      given Serializable[Token] = Serializable.from(serializer(_).getBytes, b => deserializer(new String(b)))
      DASPSimulation.withStaticNeighbors(
        initialState.as,
        rules.as.map(given_Conversion_Rule_Rule).toSet,
        neighborhood.as.toSet,
      )

    override def launch[Token](
        simulation: DASPSimulation[Token],
        port: Int,
        updateFn: IFunction1[State[Token], Unit],
    ): Unit = simulation.launch(port, s => updateFn(s.back))

    override def stop[Token](simulation: DASPSimulation[Token]): Unit = simulation.stop()

    override def simulate[Token: {Serializable, Equatable}](
        rules: Set[Rule[Token]],
        initial: State[Token],
        updateFn: State[Token] => Unit,
    )(port: Int, neighbors: Set[Neighbor]): Unit =
      DASPSimulation
        .withStaticNeighbors(initial.as, rules.map(given_Conversion_Rule_Rule), neighbors)
        .launch(port, updateFn)

    object ProductADTsConversions:

      export it.unibo.dap.utils.{ as, back, Iso }
      import it.unibo.dap.model

      given [T] => Iso[MSet[T], model.MSet[T]] = Iso(m => model.MSet(m.elems*), m => MSet(m.asList*))

      given [Token] => Iso[State[Token], model.DAP.State[Token]] =
        Iso(s => model.DAP.State(s.tokens.as, s.msg.as), s => State(s.tokens.back, s.msg.back))

      given [Token] => Conversion[State[Token] => Unit, model.DAP.State[Token] => Unit] = f => s => f(s.back)

      given [Token] => Conversion[Rule[Token], model.DAP.Rule[Token]] = r =>
        model.DAP.Rule(r.pre.as, _ => r.rate, r.eff.as, r.msg.as)
    end ProductADTsConversions
  end ProductInterface

end ProductApi
