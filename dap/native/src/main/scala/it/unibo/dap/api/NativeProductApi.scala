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

object NativeProductApi extends ProductApi:

  override val interface: NativeInterface.type = NativeInterface

  object NativeInterface extends ProductInterface with NativeADTs:
    override given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool())

    @exported
    def simulation(
        rules: ISeq[CRule],
        initialState: Ptr[CState],
        neighborhood: ISeq[CNeighbor],
        serializer: CFuncPtr1[CToken, CString],
        deserializer: CFuncPtr1[CString, CToken],
        equalizer: CFuncPtr2[CToken, CToken, CBool],
    ): DASPSimulation[CToken] = withLogging:
      val allRules = rules.pipe(_.map(toRule))
      val allNeighbors = neighborhood.pipe(_.map(toNeighbor))
      interface.simulation(allRules, initialState, allNeighbors, serializer(_), deserializer(_), equalizer)

    @exported("launch")
    def launchSimulation(simulation: DASPSimulation[CToken], port: Int, updateFn: CFuncPtr1[Ptr[CState], Unit]): Unit =
      interface.launch(simulation, port, updateFn(_))

    @exported("stop")
    def stopSimulation(simulation: DASPSimulation[CToken]): Unit = interface.stop(simulation)
  end NativeInterface

  trait NativeADTs extends ADTs:

    // Implementation of native-independent types
    override type IString = CString

    override given Iso[IString, String] = Iso(
      fromCString(_),
      s =>
        val ptr = CUtils.freshPointer[CChar](s.length() + 1)
        for i <- 0 until s.length() do ptr(i) = s.charAt(i).toByte
        ptr(s.length()) = 0.toByte
        ptr,
    )

    override type IOption[T] = T | Null
    override given [T] => Iso[IOption[T], Option[T]] = Iso(t => Option.when(t != null)(t.asInstanceOf[T]), _.orNull)

    override type ISeq[T] = Ptr[CSeq[T]]

    given iseqc[T]: Conversion[ISeq[T], Seq[T]] with

      inline def apply(x: ISeq[T]): Seq[T] =
        summonFrom:
          case _: Tag[T] =>
            summonFrom:
              case _: ClassTag[T] =>
                val size = (!x)._2.toInt
                val data = (!x)._1
                (0 until size).map(data.apply)
              case _ => scribe.error("[to] I don't have a class tag"); ???
          case _ => scribe.error("[to] I don't have a tag"); ???

    given iseqcc[T]: Conversion[Seq[T], ISeq[T]] with

      inline def apply(s: Seq[T]): ISeq[T] =
        summonFrom:
          case _: Tag[T] =>
            summonFrom:
              case _: ClassTag[T] =>
                val ptr = CUtils.freshPointer[CSeq[T]]()
                (!ptr)._1 = CUtils.freshPointer[T](s.length)
                (!ptr)._2 = s.length.toCSize
                for i <- 0 until s.length do (!ptr)._1(i) = s(i)
                ptr
              case _ => scribe.error("[back] I don't have a class tag"); ???
          case _ => scribe.error("[back] I don't have a tag!"); ???

    /* Currently, it is not possible to use `CFuncPtrN` as reification of agnostic function types
     * because, since the C types are not automatically generated from the agnostic types,
     * when we need to convert a Scala function using an agnostic type back to a C function
     * we would need to perform a conversion of the input argument like this:
     *
     * {{
     *    val updateFn: CFuncPtr1[Ptr[CState], Unit] = ??? // some C callback provided by the C client
     *    val f = CFuncPtr1.fromScalaFunction[State[CToken], Unit]: s =>
     *      updateFn(s /* using Conversion[State[CToken], Ptr[CState]] */)))
     *              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     *    Closing over local state of parameter updateFn in function transformed to CFuncPtr
     *    results in undefined behaviour
     * }}
     * 
     * But this is actually forbidden! When Scala Native allows to support automatic conversion
     * of agnostic types to C types, we should be able to use `CFuncPtrN`.
     */
    override type IFunction1[T1, R] = T1 => R

    given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R] with
      inline def apply(f: IFunction1[T1, R]) = f.apply

    override type IFunction2[T1, T2, R] = CFuncPtr2[T1, T2, R]

    given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R] with
      inline def apply(f: IFunction2[T1, T2, R]): (T1, T2) => R = f.apply

    // API Conversions

    given toNeighbor: Conversion[CNeighbor, Neighbor] = n => Neighbor(address = n._1, port = n._2)

    given toRule: Conversion[CRule, Rule[CToken]] = r =>
      Rule(pre = (r._1).pipe(pre => MSet(pre)), rate = r._2, eff = (r._3).pipe(eff => MSet(eff)), msg = r._4)

    given Conversion[Ptr[CState], State[CToken]] = s => State(tokens = MSet(!s.at1), msg = !s.at2)

    given Conversion[State[CToken], Ptr[CState]] = s =>
      val ptr = CUtils.freshPointer[CState]()
      (!ptr)._1 = s.tokens.elems
      (!ptr)._2 = s.msg
      ptr
  end NativeADTs
end NativeProductApi
