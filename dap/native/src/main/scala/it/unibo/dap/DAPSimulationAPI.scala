package it.unibo.dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe.*

/** Static object exposing native API for Distributed Asynchronous Petri-Nets creation and simulation. */
object DAPSimulationAPI:

  import Bindings.{*, given}

  @exported("launch_simulation")
  def launchSimulation(rulesPtr: Ptr[CRule], size: CSize, s0: Ptr[CDAPState], port: CInt, neighborhood: Ptr[CMSet[Neighbour]]) =
    try
      val rules = (0 until size.toInt)
        .map(i => rulesPtr(i))
        .map(cRuleCvt)
        .toSet
      val net = cNeighborsCvt(!neighborhood)
      val initialState = cDapStateCvt(!s0)
      ProductAPI.interface.launchSimulation(rules, initialState, s => scribe.info(s"State: $s"))(port, net.elems.toSet)
    catch case e => scribe.error(s"Error: $e")

  object Bindings:
    type CToken = Ptr[CStruct1[CString]]
    type CMSet[T] = CStruct2[Ptr[T], CSize]
    type CDAPState = CStruct2[CMSet[CToken], CToken]
    type CRateFunction = CFuncPtr1[CMSet[CToken], CDouble]
    type CRule = CStruct4[CMSet[CToken], CRateFunction, CMSet[CToken], CToken]
    type Neighbour = CString

    import it.unibo.dap.ProductAPI.ADTs.*

    given cNeighborsCvt: Conversion[CMSet[Neighbour], MSet[String]] = _.toMSet[String](fromCString(_))

    given cTokenCvt: Conversion[CToken, Option[String]] = t => Option(t).map(t => fromCString((!t)._1))

    given cTokensCvt: Conversion[CMSet[CToken], MSet[String]] = _.toMSet[String](t => fromCString((!t)._1))

    given cRuleCvt: Conversion[CRule, Rule] = r =>
      val rateFun: MSet[String] => Double = _ => r._2.apply(r._1)
      Rule(pre = cTokensCvt(r._1), rateExp = rateFun, eff = cTokensCvt(r._3), msg = cTokenCvt(r._4))

    given cDapStateCvt: Conversion[CDAPState, State] = s =>
      State(tokens = cTokensCvt(s._1), msg = cTokenCvt(s._2))

    extension [T: {ClassTag, Tag}](m: CMSet[T])
      private def toMSet[R](mapping: T => R): MSet[R] = MSet(
        (0 until m._2.toInt)
          .map(i => m._1.apply(i))
          .map(mapping)
          .toList *
      )

end DAPSimulationAPI
