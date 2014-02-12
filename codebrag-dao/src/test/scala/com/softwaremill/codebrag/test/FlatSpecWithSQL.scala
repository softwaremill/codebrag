package com.softwaremill.codebrag.test

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import scala.slick.jdbc.JdbcBackend.Database
import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}

trait FlatSpecWithSQL extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  private val connectionString = "jdbc:h2:mem:cb_test" + this.getClass.getSimpleName + ";DB_CLOSE_DELAY=-1"
  private val db = Database.forURL(connectionString, driver="org.h2.Driver")
  val sqlDatabase = SQLDatabase(db, scala.slick.driver.H2Driver)

  def withSchemas: Iterable[WithSQLSchemas]
  def schemas = withSchemas.flatMap(_.schemas).toList

  override protected def beforeAll() {
    super.beforeAll()

    createAll()
  }

  def clearData() {
    dropAll()
    createAll()
  }

  override protected def afterAll() {
    super.afterAll()
    dropAll()
  }

  private def dropAll() {
    db.withSession { implicit session =>
      schemas.reverse.foreach { _.drop }
    }
  }

  private def createAll() {
    db.withSession { implicit session =>
      schemas.foreach { _.create }
    }
  }
}