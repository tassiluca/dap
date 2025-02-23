package it.unibo.dap.utils

import gears.async.{ Async, AsyncOperations }

trait Spawnable:
  def start(using Async, AsyncOperations): Unit
