package it.unibo.dap.dsl

object MSetDSL:

  export it.unibo.dap.utils.MSet
  export it.unibo.dap.dsl.MSetDSL.given

  extension [T](a: T)
    def |(b: T): List[T] = List[T](a, b)

  extension [T](xs: List[T])
    def |(x: T): List[T] = xs :+ x

  given listSetConversion[T]: Conversion[List[T], MSet[T]] = MSet.ofList(_)
  given elemSetConversion[T]: Conversion[T, MSet[T]] = MSet.apply(_)
