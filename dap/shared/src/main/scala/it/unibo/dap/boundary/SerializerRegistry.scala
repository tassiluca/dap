package it.unibo.dap.boundary

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
    val serializers = collection.mutable.Map.empty[String, (AnyRef => Array[Byte], Array[Byte] => AnyRef)]

    override def register(
        typeName: String,
        serializer: AnyRef => Array[Byte],
        deserializer: Array[Byte] => AnyRef,
    ): Unit = serializers += typeName -> (serializer, deserializer)

    override def get(typeName: String): Option[(AnyRef => Array[Byte], Array[Byte] => AnyRef)] =
      serializers.get(typeName)
