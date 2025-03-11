package it.unibo.dap.boundary

/** A type class for serialization and deserialization capabilities.
  * @tparam T the type to serialize and deserialize
  */
trait Serializable[T]:
  def serialize(t: T): Array[Byte]
  def deserialize(bytes: Array[Byte]): T

object SerializableInstances:

  given Serializable[String] with
    override def serialize(t: String): Array[Byte] = t.getBytes
    override def deserialize(bytes: Array[Byte]): String = String(bytes)
