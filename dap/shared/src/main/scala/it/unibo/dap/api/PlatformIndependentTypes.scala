package it.unibo.dap.api

/** Root base trait with building blocks for crafting a platform-independent API. */
trait PlatformIndependentAPI:

  export it.unibo.dap.utils.Iso

  /** This trait defines the platform-independent types, named `IType`s, to be used in the
    * agnostic API along with the required conversions to the Scala types.
    * Actual platform-specific implementations of the API should incarnate these types with
    * the appropriate platform-specific types and provide, for each of them, the required
    * conversions.
    */
  trait PlatformIndependentTypes:

    type IString
    given Iso[IString, String] = compiletime.deferred

    type IOption[T]
    given [T] => Iso[IOption[T], Option[T]] = compiletime.deferred

    type ISeq[T]
    given iseqc[T]: Conversion[ISeq[T], Seq[T]]
    given iseqcc[T]: Conversion[Seq[T], ISeq[T]]

    type IFunction1[T1, R]
    given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R]

    type IFunction2[T1, T2, R]
    given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R]

  end PlatformIndependentTypes
end PlatformIndependentAPI
