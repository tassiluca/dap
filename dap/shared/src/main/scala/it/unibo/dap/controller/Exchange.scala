package it.unibo.dap.controller

import it.unibo.dap.utils.Spawnable
import gears.async.{ ReadableChannel, SendableChannel }

trait Exchange[T] extends Spawnable:
  def inputs: ReadableChannel[T]
  def outputs: SendableChannel[T]
