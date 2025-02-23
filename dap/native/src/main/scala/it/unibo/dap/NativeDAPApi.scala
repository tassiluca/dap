package it.unibo.dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe
import scala.scalanative.unsafe.*
import CUtils.*
import gears.async.default.given
import gears.async.{Async, AsyncOperations, Future}
import it.unibo.dap.boundary.SocketExchange
import it.unibo.dap.controller.DistributedSimulation
import it.unibo.dap.modelling.DAP
import it.unibo.dap.modelling.DAP.State

import java.nio.charset.Charset
import scala.concurrent.duration.DurationInt
import scala.scalanative.libc.stdlib
import scala.scalanative.runtime.ffi
import scala.scalanative.unsafe.Size.intToSize

/** Static object exposing native API for Distributed Asynchronous Petri-Nets creation and simulation. */
object DAPSimulationAPI:

  import Bindings.{ *, given }

  import it.unibo.dap.utils.MSet
  import it.unibo.dap.modelling.{ CTMC, DAP }
  import it.unibo.dap.boundary.SocketExchange.*
  import it.unibo.dap.modelling.DAP.DAP

  @exported("launch_simulation")
  def launchSimulation(rulesPtr: Ptr[CRule], size: CSize, s0: Ptr[CDAPState], neighborhood: Ptr[Neighborhood]) = Zone:
    Async.blocking:
      import it.unibo.dap.controller.DistributableInstances.given
      import it.unibo.dap.modelling.CTMC.given_Simulatable_CTMC
      val rules = (0 until size.toInt)
        .map(i => rulesPtr(i))
        .map(cRuleCvt)
        .toSet
      rules.foreach: r =>
        scribe.info(s"Rate: ${r.rateExp(MSet("a"))}" )
      val dap = DAP[String](rules)
      scribe.info("DAP is: " + dap)
//        val initialState = cDapStateCvt(s0)
//        scribe.info("Initial state is: " + initialState)
      val net: Set[Endpoint] = (0 until (!neighborhood)._4.toInt)
        .map(i => (fromCString((!neighborhood)._2.apply(i)), (!neighborhood)._3.apply(i)))
        .toSet
      val port: Int = (!neighborhood)._1
      scribe.info(s"Net is: $net")
      scribe.info(s"Port is: $port")
      val initialState: DAP.State[String] = if port == 2550 then DAP.State(MSet("a"), None) else DAP.State(MSet(), None)
      scribe.info(s"Initial state is: $initialState")
      scribe.info(s"Trembling...")
      val exchange = SocketExchange(port = port, net = net)
      Future(exchange.start)
      AsyncOperations.sleep(5.seconds)
      scribe.info("Let's gooo...")
      DistributedSimulation(exchange).launch(initialState, DAP.toCTMC(dap))

  object Bindings:
    type CToken = Ptr[CChar]
    type CMSet[T] = CStruct2[Ptr[T], CSize]
    type CDAPState = CStruct2[CMSet[CToken], CToken]
    type CRateFunction = CFuncPtr1[CMSet[CToken], CDouble]
    type CRule = CStruct4[CMSet[CToken], CRateFunction, CMSet[CToken], Ptr[CToken]]
    type Neighborhood = CStruct4[CInt, Ptr[CString], Ptr[CInt], CSize]

    import it.unibo.dap.utils.MSet

    given Conversion[CMSet[CToken], MSet[String]] = _.toMSet[String](t => t.str)

    given cRuleCvt: Conversion[CRule, DAP.Rule[String]] = r =>
      val rateFun: MSet[String] => Double = _ => r._2.apply(r._1)
      DAP.Rule(pre = r._1, rateExp = rateFun, eff = r._3, msg = Option(!r._4).map(_.str))

    extension [T: {ClassTag, unsafe.Tag}](m: CMSet[T])
      def toMSet[R](mapping: T => R): MSet[R] = MSet.ofList:
        (0 until m._2.toInt)
          .map(i => m._1.apply(i))
          .map(mapping)
          .toList

    extension (char: CToken)
      def str: String =
        val bytes = new Array[Byte](1)
        ffi.memcpy(bytes.at(0), char, 1.toCSize)
        new String(bytes,  Charset.defaultCharset())

//    given cDapStateCvt: Conversion[Ptr[CDAPState], DAP.State[String]] = s =>
//      scribe.info(s"Converting state")
//      DAP.State(tokens = (!s)._1, msg = Option((!s)._2).map(_.str))

    //  given Conversion[MSet[CToken], Ptr[CMSet[CToken]]] = m =>
    //    val cm = freshPointer[CMSet[CToken]]()
    //    val arrayOfPtrs = freshPointer[CToken](m.size)
    //    m.asList.zipWithIndex.foreach: (t, i) =>
    //      arrayOfPtrs(i)._1 = t.id
    //      arrayOfPtrs(i)._2 = t.p
    //    cm._1 = arrayOfPtrs
    //    cm._2 = m.size.toCSize
    //    cm
    //
    //  extension (s: DAP.State[Id, Place])
    //
    //    def toCState: Ptr[DAPState] =
    //      val cs = freshPointer[DAPState]()
    //      cs._1 = s.tokens
    //      cs._2 = s.messages
    //      cs
    //  given cNeighborsConversion: Conversion[Neighbors, (Id, Set[Id])] = n => (n._1, cMSetIdConversion(!n._2).asList.toSet)

end DAPSimulationAPI
