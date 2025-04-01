package it.unibo.dap.utils

import scala.concurrent.Future

trait Task[T] extends (() => Future[T])
