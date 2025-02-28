package it.unibo.dap

import gears.async.Async.Spawn
import gears.async.AsyncOperations

object ProductAPI extends Api:

  object ProductADTsConversions:
    import ProductAPI.ADTs.*
    import it.unibo.dap

    given apiMSetCvt[T]: Conversion[MSet[T], utils.MSet[T]] = mset => utils.MSet.ofList(mset.elems.toList)
    given mSetCvt[T]: Conversion[utils.MSet[T], MSet[T]] = mset => MSet(mset.asList*)
    given apiStateCvt: Conversion[State, modelling.DAP.State[Token]] = s => modelling.DAP.State(s.tokens, s.msg)
    given stateCvt: Conversion[modelling.DAP.State[Token], State] = s => State(s.tokens, s.msg)
    given Conversion[State => Unit, modelling.DAP.State[Token] => Unit] = f => s => f(stateCvt(s))
    given ruleCvt: Conversion[Rule, modelling.DAP.Rule[Token]] = r =>
      modelling.DAP.Rule(apiMSetCvt(r.pre), m => r.rateExp(mSetCvt(m)), apiMSetCvt(r.eff), r.msg)

  private class ProductInterface extends Interface:
    import ProductAPI.ADTs.*
    import ProductADTsConversions.given

    override def launchSimulation(rules: Set[Rule], s0: State, updateFn: State => Unit)(
        port: Int,
        neighbors: Set[Neighbour],
    )(using spawnCapability: Spawn, asyncOpsCapabilities: AsyncOperations): Unit =
      DAPSimulation(apiStateCvt(s0), rules.map(ruleCvt))(port, neighbors).launch(updateFn)

  override val interface: ProductAPI.Interface = ProductInterface()
end ProductAPI
