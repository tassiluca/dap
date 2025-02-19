package it.unibo.dap.utils

object Time:

  /** Facility to track time. Just embed the computation in the [[input]]. */
  def timed[A](input: => A): A =
    val t0 = java.lang.System.nanoTime
    try input
    finally scribe.info("Timed op (msec): " + (java.lang.System.nanoTime - t0) / 1_000_000)
