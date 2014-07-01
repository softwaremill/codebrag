package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.{CommitInfo, UserLike}
import org.bson.types.ObjectId

class SQLCommitInfoDAO(val database: SQLDatabase) extends CommitInfoDAO with SQLCommitInfoSchema {
  import database.driver.simple._
  import database._

  def hasCommits = db.withTransaction { implicit session => Query(commitInfos.length).first > 0 }

  def storeCommit(commit: CommitInfo) = {
    val sci = SQLCommitInfo(new ObjectId, commit.repoName, commit.sha, commit.message, commit.authorName, commit.authorEmail,
      commit.committerName, commit.committerEmail, commit.authorDate, commit.commitDate)
    val parents = commit.parents.map(p => SQLCommitInfoParent(sci.id, p))

    db.withTransaction { implicit session =>
      commitInfos += sci
      commitInfosParents ++= parents
    }
    sci.toCommitInfo(parents)
  }

  def findBySha(repoName: String, sha: String) = findOneWhere( commit => (commit.repoName === repoName) && (commit.sha === sha))

  def findByShaList(repoName: String, shaList: List[String]) = db.withTransaction { implicit session =>
    commitInfos
      .filter( c => (c.sha inSet shaList.toSet) && (c.repoName === repoName))
      .sortBy(c => (c.commitDate.asc, c.authorDate.asc))
      .list()
      .map(_.toPartialCommitDetails)
  }

  def findByCommitId(commitId: ObjectId) = findOneWhere(_.id === commitId)

  def findAllSha(repoName: String) = db.withTransaction { implicit session => commitInfos.where(_.repoName === repoName).map(_.sha).list().toSet }

  def findAllIds() = db.withTransaction { implicit session =>
    commitInfos.map(c => (c.id, c.commitDate, c.authorDate)).sortBy(t => (t._2.asc, t._3.asc)).list().map(_._1)
  }

  def findLastSha(repoName: String) = db.withTransaction { implicit session =>
    commitInfos.where(_.repoName === repoName).map { ci => (ci.sha, ci.commitDate, ci.authorDate) }
      .sortBy(d => (d._2.desc, d._3.desc))
      .take(1)
      .firstOption
      .map(_._1)
  }

  def findLastCommitsNotAuthoredByUser[T](repoName: String, user: T, count: Int)(implicit userLike: UserLike[T]) =
    findMultiWhere { commitInfos
      .where(_.repoName === repoName)
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


  private def findOneWhere(condition: CommitInfos => Column[Boolean]): Option[CommitInfo] = db.withTransaction { implicit session =>
    val q = for {
      ci <- commitInfos if condition(ci)
    } yield ci

    q.firstOption.map { sci =>
      val parents = commitInfosParents.filter(_.commitInfoId === sci.id).list()

      sci.toCommitInfo(parents)
    }
  }

  private def findMultiWhere(commitsQuery: Query[CommitInfos, SQLCommitInfo]) = db.withTransaction { implicit session =>
    val commits = commitsQuery.list()
    val commitIds = commits.map(_.id).toSet

    val parents = commitInfosParents.filter(_.commitInfoId inSet commitIds).list().groupBy(_.commitId)

    commits.map(c => c.toCommitInfo(parents.getOrElse(c.id, Nil)))
  }

  private def orderByDatesDesc(ci: CommitInfos) = (ci.commitDate.desc, ci.authorDate.desc)
}
