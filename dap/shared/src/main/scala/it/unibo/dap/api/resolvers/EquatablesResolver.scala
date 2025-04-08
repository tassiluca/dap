package it.unibo.dap.api.resolvers

import it.unibo.dap.modelling.Equatable

import scala.reflect.ClassTag

/** An [[Equatable]] capabilities resolver allowing register and retrieve equatables for different types. */
trait EquatablesResolver:
  def register[T: ClassTag](equatable: Equatable[T]): Unit

  def get[T: ClassTag]: Option[Equatable[T]]

  def of[T: ClassTag]: Option[Equatable[T]] = get[T].map(e => e(_, _))

object EquatablesResolver:

  def apply(): EquatablesResolver = EquatablesRegistryImpl()

  private class EquatablesRegistryImpl extends EquatablesResolver:
    private var equatables = Map.empty[Class[?], Equatable[?]]

    override def register[T: ClassTag](equatable: Equatable[T]): Unit =
      equatables += summon[ClassTag[T]].runtimeClass -> equatable

    override def get[T: ClassTag]: Option[Equatable[T]] =
      equatables.get(summon[ClassTag[T]].runtimeClass).map(_.asInstanceOf[Equatable[T]])
