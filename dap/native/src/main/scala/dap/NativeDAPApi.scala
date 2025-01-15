package dap

import scala.reflect.ClassTag
import scala.scalanative.unsafe
import scala.scalanative.unsafe.*
import dap.CUtils.*

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.Size.intToSize

/** Static object exposing native API for Distributed Asynchronous Petri-Nets creation and simulation. */
object NativeDAPApi extends NativeCTMCBaseApi:

  import dap.NativeDAPBindings.*
  import dap.NativeDAPBindings.{ cNeighborsConversion, cRuleConversion }

  override type State = Ptr[DAPState]

  import dap.shared.modelling.{ CTMC, DAP }
  import dap.shared.modelling.DAP.DAP

  @exported("create_dap_from_rules")
  def createDAP(rulesPtr: Ptr[Rule], size: CSize): DAP[Place] = DAP:
    (0 until size.toInt)
      .map(rulesPtr(_))
      .map(cRuleConversion)
      .toSet

  @exported("dap_to_ctmc")
  def dapToCTMC(dapPtr: Ptr[DAP[Place]]): CTMC[DAP.State[Id, Place]] = DAP.toCTMC(!dapPtr)

  @exported("simulate_dap")
  def simulateDAP(
      ctmcPtr: Ptr[CTMC[DAP.State[Id, Place]]],
      s0: State,
      neighbors: Ptr[Neighbors],
      neighborsSize: CInt,
      steps: CInt,
  ): Ptr[Trace] =
    val net = (0 until neighborsSize)
      .map(i => neighbors(i))
      .map(cNeighborsConversion)
      .toMap
    simulate(ctmcPtr, s0, steps, ptr => (!ptr).toState(net), s => s.toCState)
  end simulateDAP
end NativeDAPApi

object NativeDAPBindings:
  type Place = Ptr[CStruct0]
  type Id = Ptr[CStruct0]
  type CMSet[T] = CStruct2[Ptr[T], CSize]
  type Token = CStruct2[Id, Place]
  type DAPState = CStruct2[Ptr[CMSet[Token]], Ptr[CMSet[Token]]]
  type RateFunction = CFuncPtr1[Ptr[CMSet[Place]], CDouble]
  type Rule = CStruct4[Ptr[CMSet[Place]], RateFunction, Ptr[CMSet[Place]], Ptr[CMSet[Place]]]
  type Neighbors = CStruct2[Id, Ptr[CMSet[Id]]]

  import dap.shared.utils.MSet
  export dap.shared.modelling.DAP

  given cMSetPlaceConversion: Conversion[CMSet[Place], MSet[Place]] = _.toMSet(identity)

  given cRuleConversion: Conversion[Rule, DAP.Rule[Place]] = r =>
    val rateFun: MSet[Place] => Double = _ => r._2.apply(r._1)
    DAP.Rule(!r._1, rateFun, !r._3, !r._4)

  given cMSetTokenConversion: Conversion[CMSet[Token], MSet[DAP.Token[Id, Place]]] =
    _.toMSet(t => DAP.Token(id = t._1, p = t._2))

  given cMSetIdConversion: Conversion[CMSet[Id], MSet[Id]] = _.toMSet(identity)

  extension [T: ClassTag: unsafe.Tag](m: CMSet[T])

    private def toMSet[R](mapping: T => R = identity): MSet[R] = MSet.ofList:
      (0 until m._2.toInt)
        .map(i => m._1.apply(i))
        .map(mapping)
        .toList

  given Conversion[MSet[DAP.Token[Id, Place]], Ptr[CMSet[Token]]] = m =>
    val cm = freshPointer[CMSet[Token]]()
    val arrayOfPtrs = freshPointer[Token](m.size)
    m.asList.zipWithIndex.foreach: (t, i) =>
      arrayOfPtrs(i)._1 = t.id
      arrayOfPtrs(i)._2 = t.p
    cm._1 = arrayOfPtrs
    cm._2 = m.size.toCSize
    cm

  extension (s: DAP.State[Id, Place])

    def toCState: Ptr[DAPState] =
      val cs = freshPointer[DAPState]()
      cs._1 = s.tokens
      cs._2 = s.messages
      cs

  extension (s: DAPState)

    def toState(net: Map[Id, Set[Id]]): DAP.State[Id, Place] =
      DAP.State(
        tokens = Option(s._1).fold(MSet())(ptr => !ptr),
        messages = Option(s._2).fold(MSet())(ptr => !ptr),
        neighbours = net,
      )

  given cNeighborsConversion: Conversion[Neighbors, (Id, Set[Id])] = n => (n._1, cMSetIdConversion(!n._2).asList.toSet)

end NativeDAPBindings
