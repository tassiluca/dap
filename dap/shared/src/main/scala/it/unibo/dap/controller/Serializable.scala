package it.unibo.dap.controller

trait Serializable[T]:
  def serialize(t: T): Array[Byte]
  def deserialize(bytes: Array[Byte]): T

object SerializableInstances:

  given Serializable[String] with
    override def serialize(t: String): Array[Byte] = t.getBytes
    override def deserialize(bytes: Array[Byte]): String = String(bytes)
