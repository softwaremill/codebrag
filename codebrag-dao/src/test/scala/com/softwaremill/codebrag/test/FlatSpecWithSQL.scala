package com.softwaremill.codebrag.test

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import scala.slick.jdbc.StaticQuery

trait FlatSpecWithSQL extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  private val connectionString = "jdbc:h2:mem:cb_test" + this.getClass.getSimpleName + ";DB_CLOSE_DELAY=-1"
  val sqlDatabase = SQLDatabase.createEmbedded(connectionString)

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
    sqlDatabase.close()
  }

  private def dropAll() {
    sqlDatabase.db.withSession { implicit session =>
      StaticQuery.updateNA("DROP ALL OBJECTS").execute()
    }
  }

  private def createAll() {
    sqlDatabase.updateSchema()
  }
}