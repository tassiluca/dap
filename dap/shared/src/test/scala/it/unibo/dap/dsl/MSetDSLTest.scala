package it.unibo.dap.dsl

import it.unibo.dap.modelling.dsl.MSetDSL.{ *, given }

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MSetDSLTest extends AnyFlatSpec with Matchers:

  "A multiset" should "correctly be created using DSL from a simple value" in:
    val mset: MSet[String] = "a"
    mset.asMap shouldBe Map("a" -> 1)

  "A multiset" should "correctly be created using DSL from a list of values" in:
    val mset: MSet[String] = "a" | "a" | "b" | "c"
    mset.asMap shouldBe Map("a" -> 2, "b" -> 1, "c" -> 1)
