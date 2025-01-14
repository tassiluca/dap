package dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe
import scala.scalanative.unsafe.*
import dap.CUtils.*

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.Size.intToSize

object NativeDAPApi:

  import dap.NativeDAPBindings.*
  import dap.NativeDAPBindings.{ cNeighborsConversion, cRuleConversion, stateConversion }
  import dap.shared.modelling.*
  import dap.shared.modelling.DAP.*
  import dap.shared.modelling.CTMCSimulation.*

  @exported("create_dap_from_rules")
  def createDAP(rulesPtr: Ptr[CRule], size: CSize): DAP[Place] = DAP:
    (0 until size.toInt)
      .map(rulesPtr(_))
      .map(cRuleConversion)
      .toSet

  @exported("dap_to_ctmc")
  def dapToCTMC(dapPtr: Ptr[DAP[Place]]): CTMC[State[Id, Place]] = DAP.toCTMC(!dapPtr)

  @exported("simulate_dap")
  def simulateDAP(
      ctmcPtr: Ptr[CTMC[State[Id, Place]]],
      s0: Ptr[CState],
      neighbors: Ptr[CNeighbors],
      neighborsSize: CInt,
      steps: CInt,
  ): Ptr[CTrace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = stdlib.malloc(sizeOf[CTrace]).asInstanceOf[Ptr[CTrace]]
    val events = stdlib.malloc(sizeOf[CEvent] * steps).asInstanceOf[Ptr[CEvent]]
    val net = (0 until neighborsSize)
      .map(i => neighbors(i))
      .map(cNeighborsConversion)
      .toMap
    val initialState = (!s0).toState(net)
    ctmc
      .newSimulationTrace(initialState, new java.util.Random)
      .take(steps)
      .zipWithIndex
      .foreach: (e, i) =>
        val current = events(i)
        current._1 = e.time
        current._2 = e.state
    trace._1 = events
    trace._2 = steps.toCSize
    trace
  end simulateDAP
end NativeDAPApi

object NativeDAPBindings:
  type Place = Ptr[CStruct0]
  type Id = Ptr[CStruct0]
  type CMSet[T] = CStruct2[Ptr[T], CSize]
  type CToken = CStruct2[Id, Place]
  type CState = CStruct2[Ptr[CMSet[CToken]], Ptr[CMSet[CToken]]]
  type CRateFunction = CFuncPtr1[Ptr[CMSet[Place]], CDouble]
  type CRule = CStruct4[Ptr[CMSet[Place]], CRateFunction, Ptr[CMSet[Place]], Ptr[CMSet[Place]]]
  type CEvent = CStruct2[CDouble, Ptr[CState]]
  type CTrace = CStruct2[Ptr[CEvent], CSize]
  type CNeighbors = CStruct2[Id, Ptr[CMSet[Id]]]

  import dap.shared.utils.MSet
  export dap.shared.modelling.DAP.{ Rule, State, Token }

  given cMSetPlaceConversion: Conversion[CMSet[Place], MSet[Place]] = _.toMSet(identity)

  given cRuleConversion: Conversion[CRule, Rule[Place]] = r =>
    val rateFun: MSet[Place] => Double = _ => r._2.apply(r._1)
    Rule(!r._1, rateFun, !r._3, !r._4)

  given cMSetTokenConversion: Conversion[CMSet[CToken], MSet[Token[Id, Place]]] =
    _.toMSet(t => Token(id = t._1, p = t._2))

  given cMSetIdConversion: Conversion[CMSet[Id], MSet[Id]] = _.toMSet(identity)

  extension [T: ClassTag: unsafe.Tag](m: CMSet[T])

    private def toMSet[R](mapping: T => R = identity): MSet[R] = MSet.ofList:
      (0 until m._2.toInt)
        .map(i => m._1.apply(i))
        .map(mapping)
        .toList

  given Conversion[MSet[Token[Id, Place]], Ptr[CMSet[CToken]]] = m =>
    val cm = stdlib.malloc(sizeOf[CMSet[CToken]]).asInstanceOf[Ptr[CMSet[CToken]]]
    val arrayOfPtrs = stdlib.malloc(sizeof[CToken] * m.size.toCSize).asInstanceOf[Ptr[CToken]]
    m.asList.zipWithIndex.foreach: (t, i) =>
      arrayOfPtrs(i)._1 = t.id
      arrayOfPtrs(i)._2 = t.p
    cm._1 = arrayOfPtrs
    cm._2 = m.size.toCSize
    cm

  given stateConversion: Conversion[State[Id, Place], Ptr[CState]] = s =>
    val cs = stdlib.malloc(sizeOf[CState]).asInstanceOf[Ptr[CState]]
    cs._1 = s.tokens
    cs._2 = s.messages
    cs

  extension (s: CState)

    def toState(net: Map[Id, Set[Id]]): State[Id, Place] =
      State(tokens = Option(s._1).fold(MSet())(ptr => !ptr), messages = Option(s._2).fold(MSet())(ptr => !ptr), neighbours = net)

  given cNeighborsConversion: Conversion[CNeighbors, (Id, Set[Id])] = n => (n._1, cMSetIdConversion(!n._2).asList.toSet)

end NativeDAPBindings
