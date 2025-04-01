package it.unibo.dap.api.capabilities

import it.unibo.dap.modelling.Equatable

import scala.reflect.ClassTag

/** This registry allows registering and retrieve equatables for different types.
  * It uses reflection to store the equatables in a map, using the runtime class of the type as the key.
  */
trait EquatablesRegistry:
  def register[T: ClassTag](equatable: Equatable[T]): Unit

  def get[T: ClassTag]: Option[Equatable[T]]

  def of[T: ClassTag]: Option[Equatable[T]] = get[T].map(e => e(_, _))

object EquatablesRegistry:

  def apply(): EquatablesRegistry = EquatablesRegistryImpl()

  private class EquatablesRegistryImpl extends EquatablesRegistry:
    private var equatables = Map.empty[Class[?], Equatable[?]]

    override def register[T: ClassTag](equatable: Equatable[T]): Unit =
      equatables += summon[ClassTag[T]].runtimeClass -> equatable

    override def get[T: ClassTag]: Option[Equatable[T]] =
      equatables.get(summon[ClassTag[T]].runtimeClass).map(_.asInstanceOf[Equatable[T]])
