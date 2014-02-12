package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo, UserLike}
import org.bson.types.ObjectId
import scala.slick.driver.JdbcProfile

class SQLCommitInfoDAO(database: SQLDatabase) extends CommitInfoDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def hasCommits = db.withTransaction { implicit session => Query(commitInfos.length).first > 0 }

  def storeCommit(commit: CommitInfo) = {
    val sci = SQLCommitInfo(commit.id, commit.sha, commit.message, commit.authorName, commit.authorEmail,
      commit.committerName, commit.committerEmail, commit.authorDate, commit.commitDate)
    val files = commit.files.map(f => SQLCommitInfoFile(commit.id, f.filename, f.status, f.patch))
    val parents = commit.parents.map(p => SQLCommitInfoParent(commit.id, p))

    db.withTransaction { implicit session =>
      commitInfos += sci
      commitInfosFiles ++= files
      commitInfosParents ++= parents
    }
  }

  def findBySha(sha: String) = findOneWhere(_.sha === sha)

  def findByCommitId(commitId: ObjectId) = findOneWhere(_.id === commitId)

  def findAllSha() = db.withTransaction { implicit session => commitInfos.map(_.sha).list().toSet }

  def findLastSha() = db.withTransaction { implicit session =>
    commitInfos.map { ci => (ci.sha, ci.commitDate, ci.authorDate) }
      .sortBy(d => (d._2.desc, d._3.desc))
      .take(1)
      .firstOption
      .map(_._1)
  }

  def findLastCommitsNotAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]) =
    findMultiWhere { commitInfos
      .filter(ci => ci.authorName =!= userLike.userFullName(user) && ci.authorEmail =!= userLike.userEmail(user))
      .sortBy(orderByDatesDesc)
      .take(count)
    }

  def findLastCommitsAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]) =
    findMultiWhere { commitInfos
      .filter(ci => ci.authorName === userLike.userFullName(user) || ci.authorEmail === userLike.userEmail(user))
      .sortBy(orderByDatesDesc)
      .take(count)
    }

  def findLastCommitsAuthoredByUserSince[T](user: T, date: DateTime)(implicit userLike: UserLike[T]) =
    findMultiWhere { commitInfos
      .filter { ci =>
        (ci.authorName === userLike.userFullName(user) || ci.authorEmail === userLike.userEmail(user)) &&
          ci.authorDate >= date
      }
      .sortBy(_.authorDate.asc)
    }

  def findPartialCommitInfo(ids: List[ObjectId]) = db.withTransaction { implicit session =>
    commitInfos
      .filter(_.id inSet ids.toSet)
      .sortBy(c => (c.commitDate.asc, c.authorDate.asc))
      .list()
      .map(_.toPartialCommitDetails)
  }

  private case class SQLCommitInfo(
    id: ObjectId, sha: String, message: String, authorName: String, authorEmail: String,
    committerName: String, committerEmail: String, authorDate: DateTime, commitDate: DateTime) {

    def toCommitInfo(sfs: List[SQLCommitInfoFile], sps: List[SQLCommitInfoParent]) = CommitInfo(
      id, sha, message, authorName, authorEmail, committerName, committerEmail, authorDate, commitDate,
      sps.map(_.parent), sfs.map(f => CommitFileInfo(f.filename, f.status, f.patch))
    )

    def toPartialCommitDetails = PartialCommitInfo(id, sha, message, authorName, authorEmail, authorDate.toDate)
  }

  private case class SQLCommitInfoFile(commitId: ObjectId, filename: String, status: String, patch: String)

  private case class SQLCommitInfoParent(commitId: ObjectId, parent: String)

  private class CommitInfos(tag: Tag) extends Table[SQLCommitInfo](tag, "commit_infos") {
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

  private val commitInfos = TableQuery[CommitInfos]

  private class CommitInfosFiles(tag: Tag) extends Table[SQLCommitInfoFile](tag, "commit_infos_files") {
    def commitInfoId = column[ObjectId]("commit_info_id")
    def filename = column[String]("filename")
    def status = column[String]("status")
    def patch = column[String]("patch")

    def commitInfo = foreignKey("COMMIT_INFO_FILES_FK", commitInfoId, commitInfos)(_.id)

    def * = (commitInfoId, filename, status, patch) <> (SQLCommitInfoFile.tupled, SQLCommitInfoFile.unapply)
  }

  private val commitInfosFiles = TableQuery[CommitInfosFiles]

  private class CommitInfosParents(tag: Tag) extends Table[SQLCommitInfoParent](tag, "commit_infos_parents") {
    def commitInfoId = column[ObjectId]("commit_info_id")
    def parent = column[String]("parent")

    def commitInfo = foreignKey("COMMIT_INFO_PARENTS_FK", commitInfoId, commitInfos)(_.id)

    def * = (commitInfoId, parent) <> (SQLCommitInfoParent.tupled, SQLCommitInfoParent.unapply)
  }

  private val commitInfosParents = TableQuery[CommitInfosParents]

  private def findOneWhere(condition: CommitInfos => Column[Boolean]): Option[CommitInfo] = db.withTransaction { implicit session =>
    val q = for {
      ci <- commitInfos if condition(ci)
    } yield ci

    q.firstOption.map { sci =>
      val files = commitInfosFiles.filter(_.commitInfoId === sci.id).list()
      val parents = commitInfosParents.filter(_.commitInfoId === sci.id).list()

      sci.toCommitInfo(files, parents)
    }
  }

  private def findMultiWhere(commitsQuery: Query[CommitInfos, SQLCommitInfo]) = db.withTransaction { implicit session =>
    val commits = commitsQuery.list()
    val commitIds = commits.map(_.id).toSet

    val files = commitInfosFiles.filter(_.commitInfoId inSet commitIds).list().groupBy(_.commitId)
    val parents = commitInfosParents.filter(_.commitInfoId inSet commitIds).list().groupBy(_.commitId)

    commits.map(c => c.toCommitInfo(files.getOrElse(c.id, Nil), parents.getOrElse(c.id, Nil)))
  }

  private def orderByDatesDesc(ci: CommitInfos) = (ci.commitDate.desc, ci.authorDate.desc)

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(commitInfos.ddl, commitInfosFiles.ddl, commitInfosParents.ddl)
}
