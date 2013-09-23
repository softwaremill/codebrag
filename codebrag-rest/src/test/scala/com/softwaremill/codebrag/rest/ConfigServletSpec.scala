package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.CodebragServletSpec

import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach


class ConfigServletSpec extends CodebragServletSpec with BeforeAndAfterEach  {

  override def beforeEach {
    val config = new CodebragConfig {
      def rootConfig = ConfigFactory.load("test.conf")
    }
    addServlet(new ConfigServlet(config), "/*")
  }

  "GET /" should "return demo flag" in {
    get("/") {
      status should be(200)
      body should be("{\"demo\":true}")
    }
  }

}
