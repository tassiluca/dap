package libdap.impl

import it.unibo.dap.api.NativeProductApi
import it.unibo.dap.utils.CUtils.withLogging
import libdap.aliases.{Token, size_t}
import libdap.structs.*

import scala.scalanative.libc
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.UByte

object Implementations extends libdap.ExportedFunctions:

  import it.unibo.dap.api.NativeProductApi.NativeInterface.{ toDAPState, toRule, toState, given }

  override def register_codec(
      name: CString,
      serialize_fn: CFuncPtr2[Ptr[CSignedChar], Ptr[size_t], Ptr[CUnsignedChar]],
      deserialize_fn: CFuncPtr2[Ptr[CUnsignedChar], CInt, Ptr[CSignedChar]],
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
    NativeProductApi.interface.register(fromCString(name), ser, de)
    0

  override def launch_simulation(
      rules: Ptr[Rule],
      rules_size: size_t,
      s0: Ptr[DAPState],
      port: CInt,
      neighborhood: Ptr[MSet_Neighbour],
      n_state_change: CFuncPtr1[Ptr[DAPState], Unit],
  ): Unit = withLogging:
    scribe.info("Launching simulation...")
    val allRules = (0 until rules_size.toInt).map(i => rules(i)).map(_.toRule).toSet
    val simulation =
      NativeProductApi.interface.simulate(allRules, (!s0).toState, s => Zone(n_state_change(s.toDAPState)))
    simulation(port, neighborhood)

end Implementations
