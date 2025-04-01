package it.unibo.dap.utils

import scala.concurrent.ExecutionContext

trait Spawnable:
  def spawn(using ExecutionContext): Task[Unit]
