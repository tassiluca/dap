package libdap.impl

import it.unibo.dap.api.NativeProductApi
import it.unibo.dap.utils.CUtils.withLogging
import libdap.aliases.{ size_t, uint8_t }
import libdap.structs.*

import scala.scalanative.libc
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.UByte

object Implementations extends libdap.ExportedFunctions:

  import it.unibo.dap.api.NativeProductApi.NativeInterface.{ toDAPState, toState, given }

  override def register_serde(
      name: CString,
      serialize_fn: CFuncPtr2[Ptr[CSignedChar], Ptr[size_t], Ptr[uint8_t]],
      deserialize_fn: CFuncPtr2[Ptr[uint8_t], CInt, Ptr[CSignedChar]],
  ): CInt =
    val ser: AnyRef => Array[Byte] = obj =>
      val size = stackalloc[size_t]()
      val res = serialize_fn(obj.asInstanceOf[Ptr[CSignedChar]], size)
      val arr = new Array[Byte]((!size).toInt)
      for i <- 0 until (!size).toInt do arr(i) = res(i).toByte
      arr
    val de: Array[Byte] => AnyRef = arr =>
      val size = arr.length
      val ptr = stackalloc[CUnsignedChar](size)
      for i <- 0 until size do ptr(i) = UByte.valueOf(arr(i))
      deserialize_fn(ptr, size)
    NativeProductApi.interface.registerSerDe(fromCString(name), ser, de)
    0

  override def register_equatable(name: CString, equals_fn: CFuncPtr2[Ptr[CSignedChar], Ptr[CSignedChar], CInt]): CInt =
    val eq: (AnyRef, AnyRef) => Boolean = (a, b) =>
      val res = equals_fn(a.asInstanceOf[Ptr[CSignedChar]], b.asInstanceOf[Ptr[CSignedChar]])
      res != 0
    NativeProductApi.interface.registerEquatable(fromCString(name), eq)
    0

  override def launch_simulation(
      rules: Ptr[MSet_Rule],
      s0: Ptr[DAPState],
      port: CInt,
      neighborhood: Ptr[MSet_Neighbour],
      n_state_change: CFuncPtr1[Ptr[DAPState], Unit],
  ): Unit = withLogging:
    val allRules = crulesCvt(rules)
    val initialState = (!s0).toState
    val simulate = NativeProductApi.interface.simulate(allRules, initialState, s => Zone(n_state_change(s.toDAPState)))
    simulate(port, neighborhood)

end Implementations
