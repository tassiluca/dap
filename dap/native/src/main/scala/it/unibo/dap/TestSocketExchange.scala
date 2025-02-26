//package it.unibo.dap
//
//import gears.async.*
//import gears.async.default.given
//import it.unibo.dap.controller.SocketExchange
//
//import scala.concurrent.duration.DurationInt
//import scala.scalanative.unsafe.{ exported, CInt }
//
//object Pinger:
//
//  @exported("run_pinger")
//  def run(port: CInt, other: CInt) = Async.blocking:
//    val exchange = SocketExchange(port, Set(("localhost", other)))
//    Future(exchange.start)
//    exchange.outputs.send("Ping") // trigger the first message
//    while true do
//      exchange.inputs
//        .read()
//        .foreach: msg =>
//          scribe.info(s"Pinger received: $msg")
//          AsyncOperations.sleep(2.seconds)
//          scribe.info("Pinger sending message")
//          exchange.outputs.send("Ping")
//
//object Ponger:
//
//  @exported("run_ponger")
//  def run(port: CInt, other: CInt) = Async.blocking:
//    val exchange = SocketExchange(port, Set(("localhost", other)))
//    Future(exchange.start)
//    while true do
//      exchange.inputs
//        .read()
//        .foreach: msg =>
//          scribe.info(s"Ponger received: $msg")
//          scribe.info("Ponger sending message")
//          exchange.outputs.send("Pong")
