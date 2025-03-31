package it.unibo.dap.api.capabilities

import it.unibo.dap.boundary.Serializable
import scala.reflect.ClassTag

case class SerDe[T](serialize: T => Array[Byte], deserialize: Array[Byte] => T)

trait SerDeRegistry:

  def register[T: ClassTag](serialize: T => Array[Byte], deserialize: Array[Byte] => T): Unit =
    register(SerDe(serialize, deserialize))

  def register[T: ClassTag](serde: SerDe[T]): Unit

  def get[T: ClassTag]: Option[SerDe[T]]

  def of[T: ClassTag]: Option[Serializable[T]] =
    get[T].map: serde =>
      new Serializable[T]:
        override def serialize(t: T): Array[Byte] = serde.serialize(t)
        override def deserialize(bytes: Array[Byte]): T = serde.deserialize(bytes)

object SerializerRegistry:

  def apply(): SerDeRegistry = SerializerRegistryImpl()

  private class SerializerRegistryImpl extends SerDeRegistry:
    private var serializers = Map.empty[Class[?], SerDe[?]]

    override def get[T: ClassTag]: Option[SerDe[T]] =
      serializers.get(summon[ClassTag[T]].runtimeClass).map(_.asInstanceOf[SerDe[T]])

    override def register[T: ClassTag](serde: SerDe[T]): Unit =
      serializers += summon[ClassTag[T]].runtimeClass -> serde
