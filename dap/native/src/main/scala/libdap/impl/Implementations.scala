package libdap.impl

import libdap.aliases.size_t
import libdap.structs.{ DAPState as CDAPState, MSet_Neighbour as CMsetNeighbour, Rule as CRule }

import scala.scalanative.unsafe.{ CInt, Ptr }

object Implementations extends libdap.ExportedFunctions:

  override def launch_simulation(
      rules: Ptr[CRule],
      rules_size: size_t,
      s0: Ptr[CDAPState],
      port: CInt,
      neighborhood: Ptr[CMsetNeighbour],
  ): Unit = ???

//object ProductApiBindings:
//
//  import it.unibo.dap.ProductAPI.ADTs.*
//
//  given Conversion[CMSetToken, MSet[Token]] = m => ???
//
//  given Conversion[CMSetToken, MSet[Token]] = m =>
//    (0 until m.size.toInt)
//      .map(i => m.elements(i))
//      .map(t => ???)
//      .toSet
//    ???
