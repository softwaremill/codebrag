package com.softwaremill.codebrag.test.mongo

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.FlatSpecWithMongo

trait  ClearDataAfterTest extends BeforeAndAfterEach {
  this: FlatSpecWithMongo =>

  override protected def afterEach() {
    try {
      clearData()
    } catch {
      case e: Exception => e.printStackTrace()
    }

    super.afterEach()
  }
}