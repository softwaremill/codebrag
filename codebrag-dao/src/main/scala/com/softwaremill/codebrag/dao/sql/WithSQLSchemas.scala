package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile

trait WithSQLSchemas {
  def schemas: Iterable[JdbcProfile#DDLInvoker]
}
