package it.unibo.dap.controller

/** A type class for serialization and deserialization capabilities.
  * @tparam T the type to serialize and deserialize
  */
trait Serializable[T]:
  def serialize(t: T): Array[Byte]
  def deserialize(bytes: Array[Byte]): T

object Serializable:
  def serialize[T](t: T)(using s: Serializable[T]): Array[Byte] = s.serialize(t)
  def deserialize[T](t: Array[Byte])(using s: Serializable[T]): T = s.deserialize(t)

object SerializableInstances:

  given Serializable[String] with
    override def serialize(t: String): Array[Byte] = t.getBytes
    override def deserialize(bytes: Array[Byte]): String = String(bytes)
