package it.unibo.dap.utils

import scala.concurrent.duration.Duration

trait PlatformSleep:
  def sleep(duration: Duration): Unit

object PlatformSleep:
  def apply(): PlatformSleep = Platform.select
