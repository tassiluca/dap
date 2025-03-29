package libdap.impl

import it.unibo.dap.api.NativeProductApi
import it.unibo.dap.utils.CUtils.{ freshPointer, withLogging }
import libdap.aliases.{ size_t, uint8_t, Token }
import libdap.structs.*

import scala.scalanative.libc
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.UByte
import scala.scalanative.unsafe.Size.intToSize

object Implementations extends libdap.ExportedFunctions:
  import it.unibo.dap.api.NativeProductApi.NativeInterface.{ toDAPState, toState, given }

  setup()

  override def register_equatable(equals_fn: CFuncPtr2[Ptr[SerializedData], Ptr[SerializedData], CInt]): CInt =
    val eq = (d1: AnyRef, d2: AnyRef) =>
      val data1 = d1.asInstanceOf[Token].value
      val data2 = d2.asInstanceOf[Token].value
      equals_fn(data1, data2) == 1
    NativeProductApi.interface.registerEquatable(typeName = "Token", equalizer = eq)
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

  private def setup(): Unit =
    val ser = (obj: AnyRef) =>
      val data = !obj.asInstanceOf[Token].value
      val arr = new Array[Byte](data.size.toInt)
      for i <- 0 until data.size.toInt do arr(i) = data.data(i).toByte
      arr
    val de = (buff: Array[Byte]) =>
      val size = buff.length
      val deserializedData = stdlib.malloc(sizeOf[SerializedData]).asInstanceOf[Ptr[SerializedData]]
      (!deserializedData).size = size.toCSize
      (!deserializedData).data = freshPointer[uint8_t](size)
      for i <- 0 until size do (!deserializedData).data(i) = UByte.valueOf(buff(i))
      deserializedData
    NativeProductApi.interface.registerSerDe(typeName = "Token", serializer = ser, deserializer = de)
  end setup

end Implementations
