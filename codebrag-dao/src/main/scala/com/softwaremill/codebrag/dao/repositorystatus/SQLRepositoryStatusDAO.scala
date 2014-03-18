package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.RepositoryStatus

class SQLRepositoryStatusDAO(database: SQLDatabase) extends RepositoryStatusDAO {
  import database.driver.simple._
  import database._

  def updateRepoStatus(newStatus: RepositoryStatus) {
    db.withTransaction { implicit session =>
      repoStatuses.filter(_.repositoryName === newStatus.repositoryName).firstOption match {
        case None => repoStatuses += newStatus
        case Some(existing) => repoStatuses
          .filter(_.repositoryName === newStatus.repositoryName)
          .map(r => (r.ready, r.error))
          .update(newStatus.ready, newStatus.error)
      }
    }
  }

  def getRepoStatus(repoName: String): Option[RepositoryStatus] = db.withTransaction { implicit session =>
    repoStatuses.filter(_.repositoryName === repoName).firstOption
  }

  private class RepoStatuses(tag: Tag) extends Table[RepositoryStatus](tag, "repository_statuses") {
    def repositoryName = column[String]("repository_name", O.PrimaryKey)
    def ready          = column[Boolean]("ready")
    def error          = column[Option[String]]("error")

    def * = (repositoryName, ready, error) <> (RepositoryStatus.tupled, RepositoryStatus.unapply)
  }

  private val repoStatuses = TableQuery[RepoStatuses]
}
