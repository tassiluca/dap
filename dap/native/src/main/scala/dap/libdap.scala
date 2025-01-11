package dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe.*
import dap.CUtils.*
import dap.modelling.{ CTMC, DAP }
import dap.modelling.DAP.*
import dap.utils.MSet
import dap.modelling.CTMCSimulation.*

import scala.scalanative.libc.{ stdio, stdlib }
import scala.scalanative.unsafe.Size.intToSize

object libdap:

  type Place = Ptr[CStruct0]
  type Id = Ptr[CStruct0]

  type CMSetPlace = CStruct2[Ptr[Place], CSize]
  type CMSetId = CStruct2[Ptr[Id], CSize]

  type CToken = CStruct2[Id, Place]
  type CMSetToken = CStruct2[Ptr[CToken], CSize]

  type CState = CStruct2[Ptr[CMSetToken], Ptr[CMSetToken]]

  type CRateFunction = CFuncPtr1[Ptr[CMSetPlace], CDouble]

  type CRule = CStruct4[Ptr[CMSetPlace], CRateFunction, Ptr[CMSetPlace], Ptr[CMSetPlace]]

  type Event = CStruct2[CDouble, Ptr[CState]]
  type Trace = CStruct2[Ptr[Event], CSize]

  type Neighbors = CStruct2[Id, Ptr[CMSetId]]

  @exported("create_dap_from_rules")
  def createDAP(rules: Ptr[CRule], size: CSize): DAP[Place] =
    val allrules: List[Rule[Place]] = (0 until size.toInt)
      .map(rules(_).toRule)
      .toList
    DAP[Place](allrules*)

  @exported("dap_to_ctmc")
  def dapToCTMC(dap: Ptr[DAP[Place]]): CTMC[State[Id, Place]] = DAP.toCTMC(!dap)

  @exported("simulate_dap")
  def simulateDAP(
      ctmcPtr: Ptr[CTMC[State[Id, Place]]],
      s0: Ptr[CState],
      neighbors: Ptr[Neighbors],
      neighborsSize: CInt,
      steps: CInt,
  ): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = stdlib.malloc(sizeOf[Trace]).asInstanceOf[Ptr[Trace]]
    val events = stdlib.malloc(sizeOf[Event] * steps).asInstanceOf[Ptr[Event]]
    Zone(stdio.printf(c"Neighbors size: %d\n", neighborsSize))
    try
      val net = (0 until neighborsSize)
        .map(i => neighbors(i).toNeighborsMap)
        .toMap
      val initialState = (!s0).toState(net)
      Zone(stdio.printf(c"Start simulating...\n"))
      ctmc
        .newSimulationTrace(initialState, new java.util.Random)
        .take(steps)
        .zipWithIndex
        .foreach: (e, i) =>
          Zone:
            stdio.printf(c"-------------------------")
            stdio.printf(c"Event %d: %f\n", i, e.time)
          val current = events(i)
          current._1 = e.time
          current._2 = e.state.toCState
          Zone:
            stdio.printf(c"Concluded event %d\n", i)
    catch case e => Zone(stdio.printf(c"Error: %s\n", toCString(e.toString)))
    end try
    trace._1 = events
    trace._2 = steps.toCSize
    trace

  end simulateDAP

  extension (m: CMSetToken)

    def toMSetToken: MSet[Token[Id, Place]] =
      val elems = (0 until m._2.toInt)
        .map(i => Token(id = m._1.apply(i)._1, p = m._1.apply(i)._2))
        .toList
      assert(elems.size == m._2, "Conversion failed due to size mismatch between elements and its size")
      MSet.ofList(elems)

  extension (m: MSet[Token[Id, Place]])

    def toCMSetToken: Ptr[CMSetToken] =
      val cm = stdlib.malloc(sizeOf[CMSetToken]).asInstanceOf[Ptr[CMSetToken]]
      val arrayOfPtrs = stdlib.malloc(sizeof[CToken] * m.size.toCSize).asInstanceOf[Ptr[CToken]]
      m.asList.zipWithIndex.foreach: (t, i) =>
        arrayOfPtrs(i)._1 = t.id
        arrayOfPtrs(i)._2 = t.p
      cm._1 = arrayOfPtrs
      cm._2 = m.size.toCSize
      cm

  given Conversion[CMSetPlace, MSet[Place]] = m =>
    val elems = (0 until m._2.toInt)
      .map(i => m._1.apply(i))
      .toList
    assert(elems.size == m._2, "Conversion failed due to size mismatch between elements and its size")
    MSet.ofList(elems)

  given Conversion[CRule, Rule[Place]] = r =>
    val rateFun: MSet[Place] => Double = _ => r._2.apply(r._1) // TODO: fix
    Rule(!r._1, rateFun, !r._3, !r._4)

  extension (r: CRule) def toRule: Rule[Place] = r

  extension (s: CState)

    def toState(net: Map[Id, Set[Id]]): State[Id, Place] =
      State(
        tokens = Option(s._1).fold(MSet())(ptr => (!ptr).toMSetToken),
        messages = Option(s._2).fold(MSet())(ptr => (!ptr).toMSetToken),
        neighbours = net,
      )

  extension (s: State[Id, Place])

    def toCState: Ptr[CState] =
      val cs = stdlib.malloc(sizeOf[CState]).asInstanceOf[Ptr[CState]]
      cs._1 = s.tokens.toCMSetToken
      cs._2 = s.messages.toCMSetToken
      cs

  extension (m: CMSetId)

    def toMSetID: MSet[Id] =
      val elems = (0 until m._2.toInt)
        .map(i => m._1.apply(i))
        .toList
      assert(elems.size == m._2, "Conversion failed due to size mismatch between elements and its size")
      MSet.ofList(elems)

  extension (n: Neighbors) def toNeighborsMap: (Id, Set[Id]) = (n._1, (!n._2).toMSetID.asList.toSet)

end libdap
