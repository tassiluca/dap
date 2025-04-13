package it.unibo.dap.model.dsl

object MSetDSL:

  export it.unibo.dap.model.MSet
  export MSetDSL.given

  extension [T](a: T) def |(b: T): List[T] = List[T](a, b)

  extension [T](xs: List[T]) def |(x: T): List[T] = xs :+ x

  given listSetConversion[T]: Conversion[List[T], MSet[T]] = MSet.ofList(_)
  given elemSetConversion[T]: Conversion[T, MSet[T]] = MSet.apply(_)
