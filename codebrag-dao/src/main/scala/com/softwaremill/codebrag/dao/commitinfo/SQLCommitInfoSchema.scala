package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.{PartialCommitInfo, CommitInfo}

trait SQLCommitInfoSchema {
  val database: SQLDatabase

  import database.driver.simple._
  import database._

  protected case class SQLCommitInfo(
    id: ObjectId, sha: String, message: String, authorName: String, authorEmail: String,
    committerName: String, committerEmail: String, authorDate: DateTime, commitDate: DateTime) {

    def toCommitInfo(sps: List[SQLCommitInfoParent]) = CommitInfo(
      id, sha, message, authorName, authorEmail, committerName, committerEmail, authorDate, commitDate,
      sps.map(_.parent)
    )

    def toPartialCommitDetails = PartialCommitInfo(id, sha, message, authorName, authorEmail, authorDate)
  }

  protected case class SQLCommitInfoParent(commitId: ObjectId, parent: String)

  protected class CommitInfos(tag: Tag) extends Table[SQLCommitInfo](tag, "commit_infos") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def sha = column[String]("sha")
    def message = column[String]("message")
    def authorName = column[String]("author_name")
    def authorEmail = column[String]("author_email")
    def commiterName = column[String]("commiter_name")
    def commiterEmail = column[String]("commiter_email")
    def authorDate = column[DateTime]("author_date")
    def commitDate = column[DateTime]("commit_date")

    def * = (id, sha, message, authorName, authorEmail, commiterName, commiterEmail, authorDate, commitDate) <>
      (SQLCommitInfo.tupled, SQLCommitInfo.unapply)
  }

  protected val commitInfos = TableQuery[CommitInfos]

  protected class CommitInfosParents(tag: Tag) extends Table[SQLCommitInfoParent](tag, "commit_infos_parents") {
    def commitInfoId = column[ObjectId]("commit_info_id")
    def parent = column[String]("parent")

    def commitInfo = foreignKey("commit_info_parents_fk", commitInfoId, commitInfos)(_.id)

    def * = (commitInfoId, parent) <> (SQLCommitInfoParent.tupled, SQLCommitInfoParent.unapply)
  }

  protected val commitInfosParents = TableQuery[CommitInfosParents]
}
