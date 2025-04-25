package it.unibo.dap.controller

case class SerDe[T](serialize: T => Array[Byte], deserialize: Array[Byte] => T)

/** A type class encoding the serialization and deserialization capabilities.
  * @tparam T the type to serialize and deserialize
  */
trait Serializable[T]:
  def serialize(t: T): Array[Byte]
  def deserialize(bytes: Array[Byte]): T

object Serializable:
  def serialize[T](t: T)(using s: Serializable[T]): Array[Byte] = s.serialize(t)
  def deserialize[T](t: Array[Byte])(using s: Serializable[T]): T = s.deserialize(t)

  def from[T](serializer: T => Array[Byte], deserializer: Array[Byte] => T): Serializable[T] =
    SerDe(serializer, deserializer).asSerializable

  extension [T](serde: SerDe[T])

    def asSerializable: Serializable[T] = new Serializable[T]:
      override def serialize(t: T): Array[Byte] = serde.serialize(t)
      override def deserialize(bytes: Array[Byte]): T = serde.deserialize(bytes)

object SerializableInstances:

  given Serializable[String] with
    override def serialize(t: String): Array[Byte] = t.getBytes
    override def deserialize(bytes: Array[Byte]): String = String(bytes)
