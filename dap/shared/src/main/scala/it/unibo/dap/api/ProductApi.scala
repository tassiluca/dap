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
      given Equatable[Token] = (t1, t2) => equalizer(t1, t2)
      given Serializable[Token] = Serializable.from(serializer(_).as.getBytes, b => deserializer(new String(b).back))
      val allNeighbors = neighborhood.toSet.map(_.as)
      val realRules = rules.map(r => given_Conversion_Rule_Rule(r)).toSet
      DASPSimulation.withStaticNeighbors(initialState, realRules, allNeighbors)

    inline override def launch[Token](
        simulation: DASPSimulation[Token],
        port: Int,
        updateFn: IFunction1[State[Token], Unit],
    ): Unit =
      simulation
        .launch(port)(updateFn.apply)
        .onComplete { case Failure(e) => scribe.error(e); case _ => scribe.info("Simulation completed") }

    override def stop[Token](simulation: DASPSimulation[Token]): Unit =
      simulation.stop().left.foreach(scribe.error(_))

    object ProductADTsConversions:

      export it.unibo.dap.utils.{ as, back, Iso }
      import it.unibo.dap.model

      // inline given [T] => Iso[MSet[T], model.MSet[T]] =
      //   Iso(m => model.MSet.ofList(m.elems.toList), m => MSet(m.asList.toSeq))

      given from[T]: Conversion[MSet[T], model.MSet[T]] with
        inline def apply(m: MSet[T]): model.MSet[T] = model.MSet.ofList(m.elems.toList)

      given to[T]: Conversion[model.MSet[T], MSet[T]] with
        inline def apply(m: model.MSet[T]): MSet[T] = MSet(m.asList.toSeq)

      given fromT[Token]: Conversion[State[Token], model.DAP.State[Token]] with
        inline def apply(s: State[Token]): model.DAP.State[Token] = model.DAP.State(s.tokens, s.msg.as)

      given toT[Token]: Conversion[model.DAP.State[Token], State[Token]] with
        inline def apply(s: model.DAP.State[Token]): State[Token] = State(s.tokens, s.msg.back)

      // inline given [Token] => Iso[State[Token], model.DAP.State[Token]] =
      //   Iso(s => model.DAP.State(s.tokens, s.msg.as), s => State(s.tokens, s.msg.back))

      given [Token]: Conversion[State[Token] => Unit, model.DAP.State[Token] => Unit] with
        inline def apply(f: State[Token] => Unit): model.DAP.State[Token] => Unit = s => f(s)

      given [Token]: Conversion[Rule[Token], model.DAP.Rule[Token]] with
        inline def apply(r: Rule[Token]): model.DAP.Rule[Token] = model.DAP.Rule(r.pre, _ => r.rate, r.eff, r.msg.as)
    end ProductADTsConversions
  end ProductInterface

end ProductApi
