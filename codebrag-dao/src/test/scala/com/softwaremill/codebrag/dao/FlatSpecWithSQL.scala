package com.softwaremill.codebrag.dao

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import scala.slick.jdbc.JdbcBackend.Database
import com.softwaremill.codebrag.dao.sql.{WithSQLSchema, SQLDatabase}

trait FlatSpecWithSQL extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  private val connectionString = "jdbc:h2:mem:cb_test" + this.getClass.getSimpleName + ";DB_CLOSE_DELAY=-1"
  private val db = Database.forURL(connectionString, driver="org.h2.Driver")
  val sqlDatabase = SQLDatabase(db, scala.slick.driver.H2Driver)

  def withSchemas: Iterable[WithSQLSchema]

  override protected def beforeAll() {
    super.beforeAll()

    db.withSession { implicit session =>
      withSchemas.foreach { _.schema.create }
    }
  }

  def clearData() {
    db.withSession { implicit session =>
      withSchemas.foreach { withSchema =>
        withSchema.schema.drop
        withSchema.schema.create
      }
    }
  }

  override protected def afterAll() {
    super.afterAll()

    db.withSession { implicit session =>
      withSchemas.foreach { _.schema.drop }
    }
  }
}