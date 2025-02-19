package it.unibo.dap.controller

import gears.async.{ ReadableChannel, SendableChannel }

trait Exchange[T]:
  def inputs: ReadableChannel[T]
  def outputs: SendableChannel[T]
