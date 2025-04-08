import java.io.{ ByteArrayOutputStream, ObjectOutputStream, ObjectInputStream, ByteArrayInputStream }

object SerDe:

  def serialize[T](obj: T): Array[Byte] =
    val buffer = ByteArrayOutputStream()
    val out = ObjectOutputStream(buffer)
    out.writeObject(obj)
    out.close()
    buffer.toByteArray

  def deserialize[T](bytes: Array[Byte]): T =
    val in = ObjectInputStream(ByteArrayInputStream(bytes))
    in.readObject().asInstanceOf[T]

@main def testWithLambdas(): Unit =
  val f = (x: Int) => x + 1
  val serialized = SerDe.serialize(f)
  val deserialized = SerDe.deserialize[(Int) => Int](serialized)
  println(deserialized(1)) // Should print 2
