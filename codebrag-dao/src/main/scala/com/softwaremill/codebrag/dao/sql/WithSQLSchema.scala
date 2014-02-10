package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile

trait WithSQLSchema {
  def schema: JdbcProfile#DDLInvoker
}
