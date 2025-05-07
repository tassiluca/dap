package it.unibo.dap.api

import scala.reflect.ClassTag
import scala.scalanative.unsafe.{ Tag, * }
import scala.scalanative.libc.stddef.size_t
import scala.scalanative.unsafe.Size.intToSize
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.dap.utils.CUtils

/** A C sequence of elements of type T. */
type CSeq[T] = CStruct2[Ptr[T], size_t]

extension [T](cseq: CSeq[T])
  /** @return a pointer to the first element of the sequence */
  def elements: Ptr[T] = cseq._1

  /** @return the size of the sequence */
  def size: size_t = cseq._2

  /** @return the elements of the sequence as a Scala `Seq[T]` */
  def toSeq(using Tag[T], ClassTag[T]): Seq[T] = (0 until size.toInt).map(elements.apply)

object CSeq:

  /** Converts a Scala `Seq[T]` to a C sequence of elements of type T.
    * @return a pointer to the C sequence
    * @note The caller is responsible for freeing the memory allocated for the sequence.
    */
  def fromSeq[T](seq: Seq[T])(using Tag[T], ClassTag[T]): Ptr[CSeq[T]] = CUtils
    .freshPointer[CSeq[T]]()
    .tap: ptr =>
      (!ptr)._1 = CUtils.freshPointer[T](seq.length)
      (!ptr)._2 = seq.length.toCSize
      for i <- 0 until seq.length do (!ptr)._1(i) = seq(i)

/** A generic token type. */
type CToken = CVoidPtr

/** A C DAP rule of the Petri Net. */
type CRule = CStruct4[Ptr[CSeq[CToken]], Double, Ptr[CSeq[CToken]], CToken]

/** C Neighbor. */
type CNeighbor = CStruct2[CString, CInt]

/** C DAP State. */
type CState = CStruct2[Ptr[CSeq[CToken]], CToken]

extension (s: String)

  /** @return a [[CString]] allocated in the heap, null-terminated, and containing the string `s`
    * @note The caller is responsible for freeing the memory allocated for the string.
    */
  def toCString: CString = CUtils
    .freshPointer[CChar](s.length() + 1)
    .tap: ptr =>
      for i <- 0 until s.length() do ptr(i) = s.charAt(i).toByte
      ptr(s.length()) = 0.toByte
