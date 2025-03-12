package it.unibo.dap.api.capabilities

import it.unibo.dap.modelling.Equatable

trait EquatablesRegistry:
  def register(typeName: String, equalizer: (AnyRef, AnyRef) => Boolean): Unit
  def get(typeName: String): Option[(AnyRef, AnyRef) => Boolean]

  def of[T](typeName: String): Option[Equatable[T]] =
    get(typeName).map: e =>
      (self: T, that: T) => e(self.asInstanceOf[AnyRef], that.asInstanceOf[AnyRef])

object EquatablesRegistry:
  
  def apply(): EquatablesRegistry = EquatablesRegistryImpl()
  
  private class EquatablesRegistryImpl extends EquatablesRegistry:
    private var equatables = Map.empty[String, (AnyRef, AnyRef) => Boolean]

    override def register(typeName: String, equalizer: (AnyRef, AnyRef) => Boolean): Unit = 
      equatables += typeName -> equalizer

    override def get(typeName: String): Option[(AnyRef, AnyRef) => Boolean] = 
      equatables.get(typeName)
