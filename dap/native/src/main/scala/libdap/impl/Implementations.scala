package libdap.impl

import it.unibo.dap.api.NativeProductApi
import it.unibo.dap.model.Equatable
import it.unibo.dap.utils.CUtils.*
import libdap.aliases.{ size_t, Neighbour, Token }
import libdap.structs.*

import scala.scalanative.libc
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.*

object Implementations extends libdap.ExportedFunctions:
  import it.unibo.dap.api.NativeProductApi.NativeInterface.given

  override def MSet_Token_free(set: Ptr[MSet_Token]): Unit =
    stdlib.free((!set).elements)
    stdlib.free(set)

  override def MSet_Token_of(elements: Ptr[Token], size: size_t): Ptr[MSet_Token] =
    val mset = freshPointer[MSet_Token]()
    (!mset).size = size
    (!mset).elements = freshPointer[Token](size.toInt)
    for i <- 0 until size.toInt do (!mset).elements(i) = elements(i)
    mset

  override def launch_simulation(
      rules: Ptr[Rule],
      rules_size: size_t,
      s0: Ptr[DAPState],
      port: CInt,
      neighbors: Ptr[Neighbour],
      neighbors_size: size_t,
      on_state_change: CFuncPtr1[Ptr[DAPState], Unit],
      equals_fn: CFuncPtr2[Ptr[RawData], Ptr[RawData], CInt],
  ): Unit =
    given Equatable[Token] = (t1, t2) => equals_fn(t1.value, t2.value) == 1
    val allRules = (0 until rules_size.toInt).map(rules(_)).map(given_Conversion_CRule_Rule).toSet
    val neighbourhood =
      (0 until neighbors_size.toInt).map(neighbors(_)).map(given_Conversion_CNeighbour_Neighbour).toSet
    val initialState = given_Conversion_CDAPState_State(!s0)
    val simulate = NativeProductApi.interface.simulate(allRules, initialState, s => Zone(on_state_change(s.toDAPState)))
    simulate(port, neighbourhood)
  end launch_simulation

end Implementations
