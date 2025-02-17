package it.unibo.dap.sockets

import gears.async.*
import gears.async.default.given
import scala.concurrent.duration.DurationInt

object Pinger:

  def apply(port: Int, other: Int) = Async.blocking:
    val exchange = SocketExchange(port, Set(("localhost", other)))
    exchange.start
    exchange.outputs.send("Ping") // trigger the first message
    while true do
      exchange.inputs
        .read()
        .foreach: msg =>
          scribe.info(s"Pinger received: $msg")
          AsyncOperations.sleep(2.seconds)
          scribe.info("Pinger sending message")
          exchange.outputs.send("Ping")

object Ponger:

  def apply(port: Int, other: Int) = Async.blocking:
    val exchange = SocketExchange(port, Set(("localhost", other)))
    exchange.start
    while true do
      exchange.inputs
        .read()
        .foreach: msg =>
          scribe.info(s"Ponger received: $msg")
          scribe.info("Ponger sending message")
          exchange.outputs.send("Pong")

@main def runPinger(): Unit = Pinger(3200, 3210)

@main def runPonger(): Unit = Ponger(3210, 3200)
