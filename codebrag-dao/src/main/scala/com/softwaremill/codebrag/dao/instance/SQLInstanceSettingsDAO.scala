package com.softwaremill.codebrag.dao.instance

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.InstanceParam
import com.typesafe.scalalogging.slf4j.Logging

class SQLInstanceSettingsDAO(database: SQLDatabase) extends Logging {

  import database.driver.simple._
  import database._

  def save(instanceParam: InstanceParam) = db.withTransaction { implicit session =>
    val result = instanceParams.filter(_.key is instanceParam.key)
    result.firstOption match {
      case Some(r) => {
        logger.debug(s"Param with key ${instanceParam.key} found. Updating to ${instanceParam.value}")
        result.update(instanceParam)
      }
      case None => instanceParams += instanceParam
    }
  }

  def findByKey(key: String): Option[InstanceParam] = db.withTransaction { implicit session =>
    instanceParams.filter(_.key is key).firstOption
  }

  private class InstanceParams(tag: Tag) extends Table[InstanceParam](tag, "settings") {
    def key = column[String]("key", O.PrimaryKey)
    def value  = column[String]("value")

    def * = (key, value) <> (InstanceParam.tupled, InstanceParam.unapply)
  }

  private val instanceParams = TableQuery[InstanceParams]

}
