package it.unibo.dap.api

import scala.scalajs.js.JSConverters.{ JSRichIterableOnce, JSRichOption }
import scala.scalajs.concurrent.JSExecutionContext
import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.{ JSExportAll, JSExportTopLevel }
import scala.scalajs.js

object JSProductAPI extends ProductApi:
  override val interface: ProductInterface = JSInterface

  @JSExportTopLevel("DAPApi")
  @JSExportAll
  object JSInterface extends ProductInterface with ADTs:
    override given ExecutionContext = JSExecutionContext.queue

    override type IOption[T] = js.UndefOr[T]
    override given [T] => Iso[IOption[T], Option[T]] = Iso(_.toOption, _.orUndefined)

    override type ISeq[T] = js.Array[T]
    override given [T] => Iso[ISeq[T], Seq[T]] = Iso(_.toSeq, _.toJSArray)

    override type IFunction1[T1, R] = js.Function1[T1, R]
    override given [T1, R] => Conversion[IFunction1[T1, R], T1 => R] = jsf => jsf(_)

    override type IFunction2[T1, T2, R] = js.Function2[T1, T2, R]
    override given [T1, T2, R] => Conversion[IFunction2[T1, T2, R], (T1, T2) => R] = jsf => jsf(_, _)
  end JSInterface
end JSProductAPI
