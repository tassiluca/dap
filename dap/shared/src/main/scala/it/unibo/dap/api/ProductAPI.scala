package it.unibo.dap.api

trait ProductAPI extends Api:

  override val interface: Interface = ProductInterface()

  private class ProductInterface extends Interface with ADTs:

    import ProductADTsConversions.given
    import gears.async.Async
    import gears.async.default.given
    import it.unibo.dap.utils.as
    import it.unibo.dap.controller.Serializable

    override def simulate(rules: Set[Rule], s0: State, updateFn: State => Unit)(
        port: Int,
        neighbors: Set[Neighbour],
    ): Unit = Async.blocking:
      given Serializable[Token] = ???
      DAPSimulation(s0.as, rules.map(rCvt))(port, neighbors).launch(updateFn)

    private object ProductADTsConversions:

      import it.unibo.dap.utils.{Iso, as, back}
      import it.unibo.dap.modelling.DAP
      import it.unibo.dap.utils

      given [T] => Iso[MSet[T], utils.MSet[T]] = Iso(
        m => utils.MSet(m.elems*),
        m => MSet(m.asList*),
      )

      given Iso[State, DAP.State[Token]] = Iso(
        s => DAP.State(s.tokens.as, s.msg),
        s => State(s.tokens.back, s.msg),
      )

      given Conversion[State => Unit, DAP.State[Token] => Unit] = f => s => f(s.back)

      given rCvt: Conversion[Rule, DAP.Rule[Token]] = r =>
        DAP.Rule(r.pre.as, m => r.rateExp(m.back), r.eff.as, r.msg)
    end ProductADTsConversions
  end ProductInterface

end ProductAPI
