package dap

import scala.reflect.ClassTag
import scala.scalanative.libc.{ stdio, stdlib }
import scala.scalanative.unsafe.*
import scala.scalanative.unsafe.Size.intToSize
import dap.modelling.DAP.*
import dap.utils.MSet

import scala.scalanative.runtime.struct

object libdap:

  @exported("stringed_rule")
  def stringedRule(
      preconditions: Ptr[CMSet],
      rate: CFuncPtr1[Ptr[CMSet], CDouble],
      effects: Ptr[CMSet],
      messages: Ptr[CMSet],
  ): Ptr[Rule[String]] =
    val rulePtr = freshPointer[Rule[String]]()
    val f = (m: MSet[String]) => rate.apply(CMSet(m.asList)).toDouble
    !rulePtr = Rule(
      pre = MSet.ofList(preconditions.toList),
      rateExp = f(_),
      eff = MSet.ofList(effects.toList),
      msg = MSet.ofList(messages.toList),
    )
    rulePtr

  @exported("stringed_dap")
  def stringedDAP(): Ptr[DAP[String]] = ???

  extension (ms: Ptr[CMSet])
    private def toList: List[String] = (0 until ms._2.toInt).map(i => fromCString(!(ms._1 + i))).toList

  type CMSet = CStruct2[Ptr[CString], CSize]

  object CMSet:
    def apply(xs: List[String]): Ptr[CMSet] = apply(xs*)

    def apply(xs: String*): Ptr[CMSet] =
      Zone: // TODO: this may memory leak with a large number of strings (e.g. > 6000)
        val elements = freshPointer[CString](xs.length)
        xs.zipWithIndex.foreach { (str, idx) => !(elements + idx) = toCString(str) }
        val multiSetPtr = freshPointer[CMSet]()
        (!multiSetPtr)._1 = elements
        (!multiSetPtr)._2 = xs.length.toCSize
        multiSetPtr

end libdap

object libctmc:

  type State = Ptr[Byte]
  type Action = CStruct2[CDouble, State]
  type Transition = CStruct2[State, Action]

@SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.asInstanceOf"))
private def freshPointer[T](factor: Int = 1)(using ClassTag[T]): Ptr[T] =
  val ptr = stdlib.malloc(sizeOf[T] * factor).asInstanceOf[Ptr[T]]
  if ptr == null then throw new OutOfMemoryError("Failed to allocate memory") else ptr
