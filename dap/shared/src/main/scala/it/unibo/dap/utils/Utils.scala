package it.unibo.dap.utils

extension [L, R](e: Either[L, R]) def unit: Either[L, Unit] = e.map(_ => ())

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

extension [T](f: Future[T]) def unit(using ExecutionContext): Future[Unit] = f.map(_ => ())
