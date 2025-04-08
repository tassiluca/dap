package it.unibo.dap.api.resolvers

import it.unibo.dap.controller.{ SerDe, Serializable }
import it.unibo.dap.controller.Serializable.asSerializable

import scala.reflect.ClassTag

trait SerDeResolver:

  def register[T: ClassTag](serialize: T => Array[Byte], deserialize: Array[Byte] => T): Unit =
    register(SerDe(serialize, deserialize))

  def register[T: ClassTag](serde: SerDe[T]): Unit

  def get[T: ClassTag]: Option[SerDe[T]]

  def of[T: ClassTag]: Option[Serializable[T]] = get[T].map(_.asSerializable)

object SerDeResolver:

  def apply(): SerDeResolver = SerializerRegistryImpl()

  private class SerializerRegistryImpl extends SerDeResolver:
    private var serializers = Map.empty[Class[?], SerDe[?]]

    override def get[T: ClassTag]: Option[SerDe[T]] =
      serializers.get(summon[ClassTag[T]].runtimeClass).map(_.asInstanceOf[SerDe[T]])

    override def register[T: ClassTag](serde: SerDe[T]): Unit =
      serializers += summon[ClassTag[T]].runtimeClass -> serde
