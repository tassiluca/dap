package it.unibo.dap.api.capabilities

import it.unibo.dap.boundary.Serializable

trait SerializersRegistry:
  def register(typeName: String, serializer: AnyRef => Array[Byte], deserializer: Array[Byte] => AnyRef): Unit
  def get(typeName: String): Option[(AnyRef => Array[Byte], Array[Byte] => AnyRef)]

  def of[T](typeName: String): Option[Serializable[T]] =
    get(typeName).map: (s, d) =>
      new Serializable[T]:
        override def serialize(t: T): Array[Byte] = s(t.asInstanceOf[AnyRef])
        override def deserialize(bytes: Array[Byte]): T = d(bytes).asInstanceOf[T]

object SerializerRegistry:

  def apply(): SerializersRegistry = SerializerRegistryImpl()

  private class SerializerRegistryImpl extends SerializersRegistry:
    private var serializers = Map.empty[String, (AnyRef => Array[Byte], Array[Byte] => AnyRef)]

    override def register(
        typeName: String,
        serializer: AnyRef => Array[Byte],
        deserializer: Array[Byte] => AnyRef,
    ): Unit = serializers = serializers + (typeName -> (serializer, deserializer))

    override def get(typeName: String): Option[(AnyRef => Array[Byte], Array[Byte] => AnyRef)] =
      serializers.get(typeName)
