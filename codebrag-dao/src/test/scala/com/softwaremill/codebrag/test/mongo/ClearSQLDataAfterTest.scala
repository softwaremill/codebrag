package com.softwaremill.codebrag.test.mongo

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.{FlatSpecWithSQL, FlatSpecWithMongo}

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