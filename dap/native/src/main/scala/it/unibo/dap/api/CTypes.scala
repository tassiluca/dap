package it.unibo.dap.api

import scala.scalanative.unsafe.{ Tag, * }
import scala.scalanative.libc.stddef.size_t

type CSeq[T] = CStruct2[Ptr[T], size_t]

type CToken = CVoidPtr

extension [T](a: CSeq[T])
  def elements: Ptr[T] = a._1
  def size: size_t = a._2

type CRule = CStruct4[Ptr[CSeq[CToken]], Double, Ptr[CSeq[CToken]], CToken]

type CNeighbor = CStruct1[CString]

type CState = CStruct2[Ptr[CSeq[CToken]], CToken]
