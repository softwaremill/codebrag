package com.softwaremill.codebrag.service.config

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory

class ConfigWithDefaultSpec extends FlatSpec with ShouldMatchers {

  class Spec[T](val path: String, val default: T, val expectedValue: T,val get: (String, T) => T) {

  }

  val config = new ConfigWithDefault {
    def rootConfig = ConfigFactory.load()
  }

  val booleans = List(
    new Spec("codebrag.booleanTrue", false, true, config.getBoolean),
    new Spec("codebrag.booleanFalse", true, false, config.getBoolean),
    new Spec("codebrag.booleanNonExists", false, false, config.getBoolean),
    new Spec("codebrag.booleanNonExists", true, true, config.getBoolean)
  )
  val ints = List(
    new Spec("codebrag.int10", 0, 10, config.getInt),
    new Spec("codebrag.int0", 10, 0, config.getInt),
    new Spec("codebrag.intNotExist", 10, 10, config.getInt)
  )
  val strings = List(
    new Spec("codebrag.stringTest", "wrong", "test", config.getString),
    new Spec("codebrag.stringEmpty", "wrong", "", config.getString),
    new Spec("codebrag.stringNotExists", "defaultString", "defaultString", config.getString)
  )

  for (spec <- booleans) {
    doTest(spec)
  }

  for (spec <- ints) {
    doTest(spec)
  }

  for (spec <- strings) {
    doTest(spec)
  }


  def doTest[T](spec: Spec[T]) {
    it should s"value load from application.conf (path:${spec.path}) be ${spec.expectedValue}" in {
      //given (spec)

      //when
      val actual = spec.get(spec.path, spec.default)

      //then
      actual should be(spec.expectedValue)
    }
  }
}
