package it.unibo.dap.api

/** Root base trait with building blocks for crafting a platform-independent API. */
trait PlatformIndependentAPI:

  export it.unibo.dap.utils.Iso
  export it.unibo.dap.utils.Iso.{ *, given }

  /** This trait defines the platform-independent types, named `IType`s, that can be used,
    * along with the Scala conversions, to build the agnostic API.
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
    given [T] => Iso[ISeq[T], Seq[T]] = compiletime.deferred

    type IFunction1[T1, R]
    given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R]

    type IFunction2[T1, T2, R]
    given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R]

  end PlatformIndependentTypes
end PlatformIndependentAPI
