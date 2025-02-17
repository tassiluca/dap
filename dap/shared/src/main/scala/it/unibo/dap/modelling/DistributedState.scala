package it.unibo.dap.modelling

trait DistributedState[T]:
  def local: T
  def msg: T
  def update(msg: T): DistributedState[T]
