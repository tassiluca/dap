package it.unibo.dap.utils

object Platform:

  def select: PlatformSleep = duration => Thread.sleep(duration.toMillis)
