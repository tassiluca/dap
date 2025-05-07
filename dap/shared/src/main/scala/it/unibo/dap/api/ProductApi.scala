package it.unibo.dap.api

import scala.util.Failure

trait ProductApi extends Api:

  trait ProductInterface extends Interface:
    ctx: ADTs =>

    import scala.concurrent.ExecutionContext
    export ProductADTsConversions.{ *, given }

    given ExecutionContext = compiletime.deferred

    inline override def simulation[Token](
        rules: ISeq[Rule[Token]],
        initialState: State[Token],
        neighborhood: ISeq[Neighbor],
        serializer: IFunction1[Token, IString],
        deserializer: IFunction1[IString, Token],
        equalizer: IFunction2[Token, Token, Boolean],
    ): DASPSimulation[Token] =
      given Equatable[Token] = equalizer(_, _)
      given Serializable[Token] = Serializable.from(serializer(_).getBytes, b => deserializer(new String(b)))
      val allNeighbors = neighborhood.map(n => ((n.address: String), n.port)).toSet
      val allRules = rules.map(r => given_Conversion_Rule_Rule(r)).toSet
      DASPSimulation.withStaticNeighbors(initialState, allRules, allNeighbors)

    inline override def launch[Token](
        simulation: DASPSimulation[Token],
        port: Int,
        updateFn: IFunction1[State[Token], Unit],
    ): Unit = simulation
      .launch(port)(updateFn.apply)
      .onComplete:
        case Failure(e) => scribe.error(e)
        case _ => scribe.info("Simulation completed")

    override def stop[Token](simulation: DASPSimulation[Token]): Unit =
      simulation.stop().left.foreach(scribe.error(_))

    object ProductADTsConversions:

      export it.unibo.dap.utils.Iso
      export it.unibo.dap.utils.Iso.{ *, given }
      import it.unibo.dap.model

      inline given [T] => Iso[MSet[T], model.MSet[T]] =
        Iso(m => model.MSet.ofList(m.elems.toList), m => MSet(m.asList.toSeq))

      inline given [Token] => Iso[State[Token], model.DAP.State[Token]] =
        Iso(s => model.DAP.State(s.tokens, s.msg), s => State(s.tokens, s.msg))

      given [Token]: Conversion[State[Token] => Unit, model.DAP.State[Token] => Unit] with
        inline def apply(f: State[Token] => Unit): model.DAP.State[Token] => Unit = s => f(s)

      given [Token]: Conversion[Rule[Token], model.DAP.Rule[Token]] with
        inline def apply(r: Rule[Token]): model.DAP.Rule[Token] = model.DAP.Rule(r.pre, _ => r.rate, r.eff, r.msg)
    end ProductADTsConversions
  end ProductInterface

end ProductApi
