package libdap.impl

import scala.scalanative.unsafe.*
import it.unibo.dap.utils.CUtils.withLogging
import it.unibo.dap.controller.SerializerRegistry
import libdap.aliases.{ size_t, Token }
import libdap.structs.*

import scala.scalanative.libc.{ stdio, stdlib }
import scala.scalanative.unsigned.UByte

object Implementations extends libdap.ExportedFunctions:

  import it.unibo.dap.api.NativeProductApi.*
  import it.unibo.dap.api.NativeProductApi.NativeADTs.Bindings.ruleCvt

  val serializersRegistry = SerializerRegistry()

  override def use_just_for_fun(token: Token): Token =
    scribe.info("Just for fun!!")
    val serde = serializersRegistry.get("Token")
    if serde.isEmpty then stdlib.exit(1)
    val ser = serde.get._1
    val de = serde.get._2
    val serialized = ser(token.asInstanceOf[AnyRef])
    val deserialized = de(serialized).asInstanceOf[Token]
    scribe.info(s"Little faith...")
    deserialized

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
    serializersRegistry.register(fromCString(name), ser, de)
    0

  override def launch_simulation(
      rules: Ptr[Rule],
      rules_size: size_t,
      s0: Ptr[DAPState],
      port: CInt,
      neighborhood: Ptr[MSet_Neighbour],
      n_state_change: CFuncPtr1[Ptr[DAPState], Unit],
  ): Unit = withLogging:
    val allRules = (0 until rules_size.toInt).map(i => rules(i)).map(ruleCvt).toSet
    allRules.foreach: r =>
      scribe.info(s"Rule to be applied: $r")
//    val simulation = ProductAPI.interface.simulate(allRules, !s0, s => Zone(n_state_change(s.toDAPState)))
//    simulation(port, !neighborhood)

end Implementations
