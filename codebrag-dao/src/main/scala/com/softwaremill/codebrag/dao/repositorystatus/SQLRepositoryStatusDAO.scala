package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import scala.slick.driver.JdbcProfile
import com.softwaremill.codebrag.domain.RepositoryStatus

class SQLRepositoryStatusDAO(database: SQLDatabase) extends RepositoryStatusDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def updateRepoStatus(newStatus: RepositoryStatus) {
    db.withTransaction { implicit session =>
      repoStatuses.filter(_.repositoryName === newStatus.repositoryName).firstOption match {
        case None => repoStatuses += newStatus
        case Some(existing) => repoStatuses
          .filter(_.repositoryName === newStatus.repositoryName)
          .map(r => (r.headId, r.ready, r.error))
          .update(newStatus.headId.orElse(existing.headId), newStatus.ready, newStatus.error)
      }
    }
  }

  def get(repoName: String) = db.withTransaction { implicit session =>
    repoStatuses.filter(_.repositoryName === repoName).map(_.headId).firstOption.flatten
  }

  def getRepoStatus(repoName: String): Option[RepositoryStatus] = db.withTransaction { implicit session =>
    repoStatuses.filter(_.repositoryName === repoName).firstOption
  }

  private class RepoStatuses(tag: Tag) extends Table[RepositoryStatus](tag, "repository_statuses") {
    def repositoryName = column[String]("repository_name", O.PrimaryKey)
    def headId         = column[Option[String]]("head_id")
    def ready          = column[Boolean]("ready")
    def error          = column[Option[String]]("error")

    def * = (repositoryName, headId, ready, error) <> (RepositoryStatus.tupled, RepositoryStatus.unapply)
  }

  private val repoStatuses = TableQuery[RepoStatuses]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(repoStatuses.ddl)
}
