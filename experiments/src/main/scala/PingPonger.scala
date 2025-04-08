import java.net.*
import java.io.*
import scala.concurrent.*
import ExecutionContext.Implicits.global
import scala.collection.Iterator.continually

object API:

  def rep(x: Int)(f: Int => Int): Int = f(x)

object PingPonger:

  def startServer(bindAddress: InetSocketAddress): Unit =
    val server = new ServerSocket(bindAddress.getPort)
    println(s"[Ponger] Listening on ${bindAddress.getPort}...")
    while true do
      val socket = server.accept()
      Future:
        val in = socket.getInputStream
        val buffer = new Array[Byte](1024)
        continually(in.read(buffer))
          .takeWhile(_ > 0)
          .foreach: bytesRead =>
            val msg = SerDe.deserialize[Int => Int](buffer.take(bytesRead))
            println(s"[Ponger] Received: '$msg'")
            println(s"[Ponger] Applying: ${msg(10)}")
            val out = socket.getOutputStream
            out.write("pong".getBytes)
            println("[Ponger] Sent: pong")
            out.close()
        in.close()
        socket.close()
  end startServer

  def sendPing(remoteAddress: InetSocketAddress): Unit =
    val socket = Socket(remoteAddress.getHostString, remoteAddress.getPort)
    val f: Int => Int = API.rep(_)(x => x + 1)
    val out = socket.getOutputStream
    out.write(SerDe.serialize(f))
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val response = in.readLine()
    println(s"[Pinger] Received: $response")
    in.close()
    out.close()
    socket.close()
end PingPonger

object Ponger:

  def main(args: Array[String]): Unit =
    if args.length != 2 then println("Usage: Ponger <bindHost> <bindPort>")
    else
      val bindHost = args(0)
      val bindPort = args(1).toInt
      val bindAddress = new InetSocketAddress(bindHost, bindPort)
      PingPonger.startServer(bindAddress)

object Pinger:

  def main(args: Array[String]): Unit =
    if args.length != 2 then println("Usage: Pinger <remoteHost> <remotePort>")
    else
      val remoteHost = args(0)
      val remotePort = args(1).toInt
      val remoteAddress = new InetSocketAddress(remoteHost, remotePort)
      PingPonger.sendPing(remoteAddress)
