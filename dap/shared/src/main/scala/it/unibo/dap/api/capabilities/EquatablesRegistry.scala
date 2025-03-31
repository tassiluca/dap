package it.unibo.dap.api.capabilities

import it.unibo.dap.modelling.Equatable

import scala.reflect.ClassTag

/** This registry allows registering and retrieve equatables for different types.
  * It uses reflection to store the equatables in a map, using the runtime class of the type as the key.
  */
trait EquatablesRegistry:
  def register[T: ClassTag](equals: (T, T) => Boolean): Unit

  def get[T: ClassTag]: Option[(T, T) => Boolean]

  def of[T: ClassTag]: Option[Equatable[T]] = get[T].map(e => e(_, _))

object EquatablesRegistry:

  def apply(): EquatablesRegistry = EquatablesRegistryImpl()

  private class EquatablesRegistryImpl extends EquatablesRegistry:
    private var equatables = Map.empty[Class[?], (?, ?) => Boolean]

    override def register[T: ClassTag](equals: (T, T) => Boolean): Unit =
      equatables += summon[ClassTag[T]].runtimeClass -> equals

    override def get[T: ClassTag]: Option[(T, T) => Boolean] =
      equatables.get(summon[ClassTag[T]].runtimeClass).collect { case e: ((T, T) => Boolean) => e }
