package it.unibo.dap.controller

trait SerializersRegistry:

  def register(
      typeName: String,
      serializer: AnyRef => Array[Byte],
      deserializer: Array[Byte] => AnyRef,
  ): Unit
  
  def get(typeName: String): Option[(AnyRef => Array[Byte], Array[Byte] => AnyRef)]

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
    
    
