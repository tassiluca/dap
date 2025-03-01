package it.unibo.dap.api

object ProductAPI extends Api:

  override val interface: ProductAPI.Interface = ProductInterface()

  private class ProductInterface extends Interface:

    import ProductADTsConversions.given
    import ProductAPI.ADTs.*
    import gears.async.Async
    import gears.async.default.given
    import it.unibo.dap.utils.as

    override def simulate(rules: Set[Rule], s0: State, updateFn: State => Unit)(
        port: Int,
        neighbors: Set[Neighbour],
    ): Unit = Async.blocking:
      DAPSimulation(s0.as, rules.map(rCvt))(port, neighbors).launch(updateFn)

  object ProductADTsConversions:

    import ProductAPI.ADTs
    import it.unibo.dap.utils.*
    import it.unibo.dap.modelling.*

    given [T] => Iso[ADTs.MSet[T], MSet[T]] = Iso(
        mset => MSet(mset.elems*),
        mset => ADTs.MSet(mset.asList*),
    )

    given Iso[ADTs.State, DAP.State[ADTs.Token]] = Iso(
        s => DAP.State(s.tokens.as, s.msg),
        s => ADTs.State(s.tokens.back, s.msg),
    )

    given Conversion[ADTs.State => Unit, DAP.State[ADTs.Token] => Unit] = f => s => f(s.back)

    given rCvt: Conversion[ADTs.Rule, DAP.Rule[ADTs.Token]] = r =>
      DAP.Rule(r.pre.as, m => r.rateExp(m.back), r.eff.as, r.msg)

end ProductAPI
