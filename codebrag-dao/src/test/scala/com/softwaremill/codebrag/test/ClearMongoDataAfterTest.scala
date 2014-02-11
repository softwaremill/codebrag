package com.softwaremill.codebrag.test

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.test.FlatSpecWithMongo

trait ClearMongoDataAfterTest extends BeforeAndAfterEach {
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