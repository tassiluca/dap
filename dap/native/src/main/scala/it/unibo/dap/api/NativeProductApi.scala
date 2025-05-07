package it.unibo.dap.api

import java.util.concurrent.ForkJoinPool

import scala.scalanative.unsafe.*
import scala.scalanative.libc.stdlib
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

import it.unibo.dap.utils.CUtils
import scala.util.chaining.scalaUtilChainingOps
import it.unibo.dap.utils.CUtils.withLogging
import scala.reflect.ClassTag
import scala.compiletime.summonFrom
import scala.scalanative.unsigned.UnsignedRichInt
import it.unibo.dap.utils.error

/** Native implementation of the DAP API. */
object NativeProductApi extends ProductApi:

  override val interface: NativeInterface.type = NativeInterface

  object NativeInterface extends ProductInterface with NativeADTs:
    override given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool())

    @exported
    def simulation(
        rules: ISeq[CRule],
        initialState: Ptr[CState],
        neighborhood: ISeq[CNeighbor],
        serializer: CFuncPtr1[CToken, IString],
        deserializer: CFuncPtr1[IString, CToken],
        equalizer: CFuncPtr2[CToken, CToken, Boolean],
    ): DASPSimulation[CToken] = withLogging:
      val allRules = (rules: Seq[CRule]).pipe(r => r.map(toRule))
      val allNeighbors = (neighborhood: Seq[CNeighbor]).pipe(n => (n).map(toNeighbor))
      interface.simulation(allRules, initialState, allNeighbors, serializer(_), deserializer(_), equalizer)

    @exported("launch")
    def launchSimulation(simulation: DASPSimulation[CToken], port: Int, updateFn: CFuncPtr1[Ptr[CState], Unit]): Unit =
      interface.launch(simulation, port, updateFn(_))

    @exported("stop")
    def stopSimulation(simulation: DASPSimulation[CToken]): Unit = interface.stop(simulation)
  end NativeInterface

  trait NativeADTs extends ADTs:

    // Implementation of the platform-independent types for the native platform

    override type IString = CString
    override given Iso[IString, String] = Iso(fromCString(_), _.toCString)

    override type IOption[T] = T | Null
    override given [T] => Iso[IOption[T], Option[T]] = Iso(t => Option.when(t != null)(t.asInstanceOf[T]), _.orNull)

    override type ISeq[T] = Ptr[CSeq[T]]

    inline override given [T]: Iso[ISeq[T], Seq[T]] = Iso(
      toFn = (xs: ISeq[T]) =>
        summonFrom:
          case _: Tag[T] =>
            summonFrom:
              case _: ClassTag[T] =>
                val size = (!xs)._2.toInt
                val data = (!xs)._1
                (0 until size).map(data.apply)
              case _ => error(s"A ClassTag is required to perform ISeq[T] => Seq[T] conversion but it wasn't found.")
          case _ => error("An unsafe.Tag is required to perform ISeq[T] => Seq[T] conversion but it wasn't found.")
      ,
      fromFn = (x: Seq[T]) =>
        summonFrom:
          case _: Tag[T] =>
            summonFrom:
              case _: ClassTag[T] =>
                val ptr = CUtils.freshPointer[CSeq[T]]()
                (!ptr)._1 = CUtils.freshPointer[T](x.length)
                (!ptr)._2 = x.length.toCSize
                for i <- 0 until x.length do (!ptr)._1(i) = x(i)
                ptr
              case _ => error(s"A ClassTag is required to perform Seq[T] => ISeq[T] conversion but it wasn't found.")
          case _ => error("An unsafe.Tag is required to perform Seq[T] => ISeq[T] conversion but it wasn't found."),
    )

    override type IFunction1[T1, R] = T1 => R

    given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R] with
      inline def apply(f: IFunction1[T1, R]) = f.apply

    override type IFunction2[T1, T2, R] = CFuncPtr2[T1, T2, R]

    given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R] with
      inline def apply(f: IFunction2[T1, T2, R]): (T1, T2) => R = f.apply

    // API Conversions

    given toNeighbor: Conversion[CNeighbor, Neighbor] = n => Neighbor(address = n._1, port = n._2)

    given toRule: Conversion[CRule, Rule[CToken]] = r =>
      Rule(pre = (r._1).pipe(MSet(_)), rate = r._2, eff = (r._3).pipe(MSet(_)), msg = r._4)

    given Iso[Ptr[CState], State[CToken]] = Iso(
      cs => State(tokens = MSet(cs._1), msg = cs._2),
      s =>
        val ptr = CUtils.freshPointer[CState]()
        (!ptr)._1 = s.tokens.elems
        (!ptr)._2 = s.msg
        ptr,
    )

  end NativeADTs
end NativeProductApi
