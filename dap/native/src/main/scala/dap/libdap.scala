package dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe.*
import dap.CUtils.*
import dap.modelling.{ CTMC, DAP }
import dap.modelling.DAP.*
import dap.utils.{ Grids, MSet }
import dap.modelling.CTMCSimulation.*

import scala.scalanative.libc.stdio
import scala.scalanative.unsafe.Size.intToSize

object libdap:

  type Place = Ptr[Unit] // void*
  type CMSetPlace = CStruct2[Ptr[Place], CSize]
  type Id = Ptr[Unit] // void*
  type CMSetId = CStruct2[Ptr[Id], CSize]
  type CToken = CStruct2[Id, Place]
  type CMSetToken = CStruct2[Ptr[Ptr[CToken]], CSize]
  type CState = CStruct2[Ptr[CMSetToken], Ptr[CMSetToken]]
  type CRateFunction = CFuncPtr1[Ptr[CMSetPlace], CDouble]
  type CRule = CStruct4[Ptr[CMSetPlace], CRateFunction, Ptr[CMSetPlace], Ptr[CMSetPlace]]
  type Event = CStruct2[CDouble, Ptr[CState]]
  type Trace = CStruct2[Ptr[Event], CSize]

  type Neighbors = CStruct2[Id, Ptr[CMSetId]]

  @exported("create_dap_from_rules")
  def createDAP(rules: Ptr[CRule], size: CSize): Ptr[DAP[Place]] =
    val dapPtr = freshPointer[DAP[Place]](size.toInt)
    val allrules: List[Rule[Place]] = (0 until size.toInt)
      .map(i => rules(i).toRule)
      .toList
    Zone:
      allrules.foreach(r => stdio.printf(c"%s\n", toCString(r.toString)))
    !dapPtr = DAP[Place](allrules*)
    dapPtr

  @exported("dap_to_ctmc")
  def dapToCTMC(dap: Ptr[DAP[Place]]): Ptr[CTMC[State[Id, Place]]] =
    val ctmcPtr = freshPointer[CTMC[State[Id, Place]]]()
    !ctmcPtr = DAP.toCTMC(!dap)
    ctmcPtr

  @exported("simulate_dap")
  def simulateDAP(
      ctmcPtr: Ptr[CTMC[State[Id, Place]]],
      s0: Ptr[CState],
      neighbors: Ptr[Neighbors],
      neighborsSize: CSize,
      steps: CSize,
  ): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = freshPointer[Trace]()
    val events = freshPointer[Event](steps.toInt)
    val net = (0 until neighborsSize.toInt)
      .map(i => neighbors(i).toNeighborsMap)
      .toMap
    val initialState = (!s0).toState(net)
    if ctmc.transitions(initialState).isEmpty then Zone(stdio.printf(c"Initial state has no transitions\n"))
    ctmc
      .newSimulationTrace(initialState, new java.util.Random)
      .take(steps.toInt)
      .zipWithIndex
      .foreach: (e, i) =>
        Zone(stdio.printf(c"\t Event %d @ time: %f -> %s\n", i, e.time, toCString(e.state.toString)))
        try
          val current = events(i)
          current._1 = e.time
          current._2 = e.state.toCState
        catch case e => Zone(stdio.printf(c"Error: %s\n", toCString(e.toString)))
    trace._1 = events
    trace._2 = steps
    Zone:
      stdio.printf(c"Trace @ %p\n", trace)
      stdio.printf(c"Trace size: %d\n", sizeOf[Trace])
      stdio.printf(c"Len of trace %d\n", trace._2)
    trace

  end simulateDAP

  extension (m: CMSetToken)

    def toMSetToken: MSet[Token[Id, Place]] =
      val elems = (0 until m._2.toInt)
        .map(i => Token(id = (!m._1.apply(i))._1, p = (!m._1.apply(i))._2))
        .toList
      assert(elems.size == m._2, "Conversion failed due to size mismatch between elements and its size")
      MSet.ofList(elems)

  extension (m: MSet[Token[Id, Place]])

    def toCMSetToken: Ptr[CMSetToken] =
      val cm = freshPointer[CMSetToken]()
      val arrayOfPtrs = freshPointer[Ptr[CToken]](m.size)
      m.asList.zipWithIndex.foreach: (t, i) =>
        val ctoken = freshPointer[CToken]()
        ctoken._1 = t.id
        ctoken._2 = t.p
        arrayOfPtrs(i) = ctoken
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
      Zone:
        stdio.printf(c"Neighbours: %s\n", toCString(net.toString))
      State(
        tokens = Option(s._1).fold(MSet())(ptr => (!ptr).toMSetToken),
        messages = Option(s._2).fold(MSet())(ptr => (!ptr).toMSetToken),
        neighbours = net,
      )

  extension (s: State[Id, Place])

    def toCState: Ptr[CState] =
      val cs = freshPointer[CState]()
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
