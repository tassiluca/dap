package it.unibo.dap.controller

import it.unibo.dap.utils.Spawnable

import gears.async.{ ReadableChannel, SendableChannel }

trait ExchangeComponent[T]:
  ctx: NeighbourhoodResolverComponent =>

  val exchange: Exchange

  trait Exchange extends Spawnable:
    def inputs: ReadableChannel[T]
    def outputs: SendableChannel[T]
