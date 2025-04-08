package it.unibo.dap.api

trait JVMNativeAPI extends ProductAPI:

  trait JVMNativeInterface extends ProductInterface with ADTs:

    import it.unibo.dap.utils.as
    import it.unibo.dap.modelling.Equatable
    import it.unibo.dap.controller.Serializable

    override def simulate(
        rules: Set[Rule],
        initial: State,
        updateFn: State => Unit,
    )(port: Int, neighbours: Set[Neighbour]): Unit =
      given Serializable[Token] = serializerRegistry
        .of[Token]
        .getOrElse(throw IllegalArgumentException("Token serializer not found"))
      given Equatable[Token] = equatablesRegistry
        .of[Token]
        .getOrElse(throw IllegalArgumentException("Token equalizer not found"))
      DAPSimulation(initial.as, rules.map(rCvt))(port, neighbours).launch(updateFn)
end JVMNativeAPI
