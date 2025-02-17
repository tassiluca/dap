package it.unibo.dap.core

import gears.async.{ ReadableChannel, SendableChannel }

trait Exchange[T]:
  def inputs: ReadableChannel[T]
  def outputs: SendableChannel[T]
