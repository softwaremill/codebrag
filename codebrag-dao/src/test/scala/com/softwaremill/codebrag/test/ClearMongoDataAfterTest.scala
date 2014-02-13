package com.softwaremill.codebrag.test

import org.scalatest.BeforeAndAfterEach

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