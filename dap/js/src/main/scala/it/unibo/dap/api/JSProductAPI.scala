package it.unibo.dap.api

import scala.scalajs.js.JSConverters.{ JSRichIterableOnce, JSRichOption }
import scala.scalajs.concurrent.JSExecutionContext
import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.{ JSExportAll, JSExportTopLevel }
import scala.scalajs.js

object JSProductAPI extends ProductApi:
  override val interface: ProductInterface = JSInterface

  @JSExportTopLevel("DAP")
  @JSExportAll
  object JSInterface extends ProductInterface with ADTs:
    override given ExecutionContext = JSExecutionContext.queue

    override type IString = String
    override given Iso[IString, String] = Iso(identity, identity)

    override type IOption[T] = js.UndefOr[T]
    override given [T] => Iso[IOption[T], Option[T]] = Iso(_.toOption, _.orUndefined)

    override type ISeq[T] = js.Array[T]
    override given iseqc[T]: Conversion[ISeq[T], Seq[T]] = _.toSeq
    override given iseqcc[T]: Conversion[Seq[T], ISeq[T]] = _.toJSArray

    override type IFunction1[T1, R] = js.Function1[T1, R]
    override given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R] = _.apply

    override type IFunction2[T1, T2, R] = js.Function2[T1, T2, R]
    override given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R] = _.apply
  end JSInterface
end JSProductAPI
