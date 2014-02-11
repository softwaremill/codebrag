package com.softwaremill.codebrag.test

import org.scalatest.BeforeAndAfterEach

trait ClearSQLDataAfterTest extends BeforeAndAfterEach {
  this: FlatSpecWithSQL =>

  override protected def afterEach() {
    try {
      clearData()
    } catch {
      case e: Exception => e.printStackTrace()
    }

    super.afterEach()
  }
}