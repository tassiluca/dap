package it.unibo.dap.api

import scala.scalanative.unsafe.{ Tag, * }
import scala.scalanative.libc.stddef.size_t
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.dap.utils.CUtils

/** A C sequence of elements of type T. */
type CSeq[T] = CStruct2[Ptr[T], size_t]

extension [T](a: CSeq[T])
  def elements: Ptr[T] = a._1
  def size: size_t = a._2

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
