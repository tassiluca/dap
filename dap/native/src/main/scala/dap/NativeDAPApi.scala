package dap

import scala.reflect.ClassTag
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
  def createDAP(rulesPtr: Ptr[CRule], size: CSize): DAP[Place] =
    val rules: Set[Rule[Place]] = (0 until size.toInt)
      .map(rulesPtr(_))
      .map(cRuleConversion)
      .toSet
    DAP[Place](rules)

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
  type CMSetPlace = CStruct2[Ptr[Place], CSize]
  type CMSetId = CStruct2[Ptr[Id], CSize]
  type CToken = CStruct2[Id, Place]
  type CMSetToken = CStruct2[Ptr[CToken], CSize]
  type CState = CStruct2[Ptr[CMSetToken], Ptr[CMSetToken]]
  type CRateFunction = CFuncPtr1[Ptr[CMSetPlace], CDouble]
  type CRule = CStruct4[Ptr[CMSetPlace], CRateFunction, Ptr[CMSetPlace], Ptr[CMSetPlace]]
  type CEvent = CStruct2[CDouble, Ptr[CState]]
  type CTrace = CStruct2[Ptr[CEvent], CSize]
  type CNeighbors = CStruct2[Id, Ptr[CMSetId]]

  import dap.shared.utils.MSet
  export dap.shared.modelling.DAP.{ Rule, State, Token }

  given Conversion[CMSetPlace, MSet[Place]] = m =>
    MSet.ofList:
      (0 until m._2.toInt)
        .map(i => m._1.apply(i))
        .toList

  given Conversion[CMSetToken, MSet[Token[Id, Place]]] = m =>
    MSet.ofList:
      (0 until m._2.toInt)
        .map(i => Token(id = m._1.apply(i)._1, p = m._1.apply(i)._2))
        .toList

  given Conversion[MSet[Token[Id, Place]], Ptr[CMSetToken]] = m =>
    val cm = stdlib.malloc(sizeOf[CMSetToken]).asInstanceOf[Ptr[CMSetToken]]
    val arrayOfPtrs = stdlib.malloc(sizeof[CToken] * m.size.toCSize).asInstanceOf[Ptr[CToken]]
    m.asList.zipWithIndex.foreach: (t, i) =>
      arrayOfPtrs(i)._1 = t.id
      arrayOfPtrs(i)._2 = t.p
    cm._1 = arrayOfPtrs
    cm._2 = m.size.toCSize
    cm

  given cRuleConversion: Conversion[CRule, Rule[Place]] = r =>
    val rateFun: MSet[Place] => Double = _ => r._2.apply(r._1)
    Rule(!r._1, rateFun, !r._3, !r._4)

  given stateConversion: Conversion[State[Id, Place], Ptr[CState]] = s =>
    val cs = stdlib.malloc(sizeOf[CState]).asInstanceOf[Ptr[CState]]
    cs._1 = s.tokens
    cs._2 = s.messages
    cs

  extension (s: CState)

    def toState(net: Map[Id, Set[Id]]): State[Id, Place] =
      State(
        tokens = Option(s._1).fold(MSet())(ptr => !ptr),
        messages = Option(s._2).fold(MSet())(ptr => !ptr),
        neighbours = net,
      )

  given cMSetIdConversion: Conversion[CMSetId, MSet[Id]] = m =>
    MSet.ofList:
      (0 until m._2.toInt)
        .map(i => m._1.apply(i))
        .toList

  given cNeighborsConversion: Conversion[CNeighbors, (Id, Set[Id])] = n => (n._1, cMSetIdConversion(!n._2).asList.toSet)

end NativeDAPBindings
