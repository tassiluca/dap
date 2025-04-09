package it.unibo.dap.utils

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait AsyncOperations:
  def sleep(duration: FiniteDuration): Future[Unit]

object Async:
  def operations: AsyncOperations = Platform.asyncOps
