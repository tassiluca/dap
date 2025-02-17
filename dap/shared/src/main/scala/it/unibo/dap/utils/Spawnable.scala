package it.unibo.dap.utils

import gears.async.{Async, AsyncOperations, Future}

trait Spawnable:
  def start(using Async.Spawn, AsyncOperations): Future[Unit]
