package it.unibo.dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe.*
import gears.async.default.given
import gears.async.{Async, AsyncOperations, Future}

import java.nio.charset.Charset
import scala.concurrent.duration.DurationInt

/** Static object exposing native API for Distributed Asynchronous Petri-Nets creation and simulation. */
object DAPSimulationAPI:

  import Bindings.{ *, given }

  import it.unibo.dap.utils.MSet
  import it.unibo.dap.modelling.{ CTMC, DAP }

  @exported("launch_simulation")
  def launchSimulation(rulesPtr: Ptr[CRule], size: CSize, s0: Ptr[CDAPState], port: CInt, neighborhood: Ptr[CMSet[Neighbour]]) =
    Async.blocking:
      try
        val rules = (0 until size.toInt)
          .map(i => rulesPtr(i))
          .map(cRuleCvt)
          .toSet
        val net = cNeighborsCvt(!neighborhood)
        val initialState: DAP.State[String] = cDapStateCvt(!s0)
        scribe.info(s"Port: $port, Neighbours: $net")
        Api.launchSimulation(rules, initialState, s => scribe.info(s"State: $s"))(port, net.asList.toSet)
      catch case e => scribe.error(s"Error: $e")

  object Bindings:
    type CToken = Ptr[CStruct1[CString]]
    type CMSet[T] = CStruct2[Ptr[T], CSize]
    type CDAPState = CStruct2[CMSet[CToken], CToken]
    type CRateFunction = CFuncPtr1[CMSet[CToken], CDouble]
    type CRule = CStruct4[CMSet[CToken], CRateFunction, CMSet[CToken], CToken]
    type Neighbour = CString

    import it.unibo.dap.utils.MSet

    given cNeighborsCvt: Conversion[CMSet[Neighbour], MSet[String]] = _.toMSet[String](fromCString(_))

    given cTokenCvt: Conversion[CToken, Option[String]] = t => Option(t).map(t => fromCString((!t)._1))

    given cTokensCvt: Conversion[CMSet[CToken], MSet[String]] = _.toMSet[String](t => fromCString((!t)._1))

    given cRuleCvt: Conversion[CRule, DAP.Rule[String]] = r =>
      val rateFun: MSet[String] => Double = _ => r._2.apply(r._1)
      DAP.Rule(pre = cTokensCvt(r._1), rateExp = rateFun, eff = cTokensCvt(r._3), msg = cTokenCvt(r._4))

    given cDapStateCvt: Conversion[CDAPState, DAP.State[String]] = s =>
      DAP.State(tokens = cTokensCvt(s._1), msg = cTokenCvt(s._2))

    extension [T: {ClassTag, Tag}](m: CMSet[T])
      private def toMSet[R](mapping: T => R): MSet[R] = MSet.ofList:
        (0 until m._2.toInt)
          .map(i => m._1.apply(i))
          .map(mapping)
          .toList

end DAPSimulationAPI
