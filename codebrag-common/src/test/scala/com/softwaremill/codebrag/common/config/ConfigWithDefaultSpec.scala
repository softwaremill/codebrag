package com.softwaremill.codebrag.common.config

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.{ConfigParseOptions, ConfigResolveOptions, ConfigFactory}

class ConfigWithDefaultSpec extends FlatSpec with ShouldMatchers {

  case class Spec[T](path: String, default: T, expectedValue: T, get: (String, T) => T)

  val configName: String = "test.conf"
  val config = new ConfigWithDefault {
    def rootConfig = {
      ConfigFactory.load(configName, ConfigParseOptions.defaults.setAllowMissing(false), ConfigResolveOptions.defaults)
    }
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

  val optionalStrings = List(
    new Spec("codebrag.optionalStringDefined", None, Some("defined"), config.getOptionalString),
    new Spec("codebrag.optionalStringUndefined", None, None, config.getOptionalString)
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

  for (spec <- optionalStrings) {
    doTest(spec)
  }


  def doTest[T](spec: Spec[T]) {
    s"Value from from $configName (path:${spec.path})" should s"be ${spec.expectedValue} (with default as ${spec.default})" in {
      //given (spec)

      //when
      val actual = spec.get(spec.path, spec.default)

      //then
      actual should be(spec.expectedValue)
    }
  }

}
