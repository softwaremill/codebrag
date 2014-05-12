package com.softwaremill.codebrag.dao.sql

import com.softwaremill.codebrag.dao.{SQLDaos, DaoConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.common.{Clock, RealTimeClock}
import org.bson.types.ObjectId
import scala.collection.mutable
import com.typesafe.scalalogging.slf4j.Logging
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import org.joda.time.{DateTimeComparator, DateTime}
import java.util.Comparator
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO
import com.softwaremill.codebrag.domain.{CommitAuthorClassification, ReviewedCommit}

object MigrateV1_2ToV2_0 extends App with DetermineToReviewStartDates with CleanpCommitDuplicates with MarkCommitsAsReviewed {

  val config = new DaoConfig {
    def rootConfig = ConfigFactory.load()
  }

  val sqlDb = SQLDatabase.createEmbedded(config)
  val sqlDaos = new SQLDaos {
    def clock = RealTimeClock
    def sqlDatabase = sqlDb
  }

  override def commitInfoDao = sqlDaos.commitInfoDao
  override def clock = sqlDaos.clock
  override def commitDao = sqlDaos.commitInfoDao
  override def taskDao = sqlDaos.commitReviewTaskDao
  override def userDao = sqlDaos.userDao
  override def reviewedCommitsDao = sqlDaos.reviewedCommitsDao

  logger.debug("Codebrag v1.2 to v2.0 database migration")
  cleanupCommitDuplicates(sqlDb)
  setupToReviewStartDates(sqlDb)
  markAsReviewed(sqlDb)
  logger.debug("Codebrag v1.2 to v2.0 database migration - Done")

}

trait DetermineToReviewStartDates extends Logging {

  def userDao: UserDAO
  def taskDao: CommitReviewTaskDAO
  def commitDao: CommitInfoDAO
  def clock: Clock

  val DaysToMoveDateBack = 30

  implicit val comparator = Ordering.comparatorToOrdering(DateTimeComparator.getInstance.asInstanceOf[Comparator[DateTime]])

  def setupToReviewStartDates(sqlDb: SQLDatabase) = {
    logger.debug(s"Setting to review start dates for users")
    sqlDb.db.withDynSession {
      (Q.u + "ALTER TABLE \"users_settings\" ADD COLUMN IF NOT EXISTS \"to_review_start_date\" TIMESTAMP").execute
    }

    userDao.findAll().foreach {user =>
      val commits = taskDao.commitsPendingReviewFor(user.id).flatMap(commitDao.findByCommitId)
      val commitsSorted = commits.toList.sortBy(_.commitDate)
      val dateForUser = commitsSorted match {
        case head :: _ => head.commitDate
        case Nil => clock.nowUtc.minusDays(DaysToMoveDateBack)
      }
      logger.debug(s"Setting date for ${user.name} to ${dateForUser}")
      userDao.setToReviewStartDate(user.id, dateForUser)
    }
    logger.debug(s"Setting to review start dates for users - Done")
  }

}

trait MarkCommitsAsReviewed extends Logging {

  def userDao: UserDAO
  def taskDao: CommitReviewTaskDAO
  def commitDao: CommitInfoDAO
  def reviewedCommitsDao: ReviewedCommitsDAO
  def clock: Clock

  def markAsReviewed(sqlDb: SQLDatabase) {
    logger.debug(s"Migrate reviewed commits")
    sqlDb.db.withDynSession {
      (Q.u + "CREATE TABLE IF NOT EXISTS \"reviewed_commits\" (\"user_id\" VARCHAR NOT NULL, \"sha\" VARCHAR NOT NULL, \"review_date\" TIMESTAMP NOT NULL)").execute()
      (Q.u + "ALTER TABLE \"reviewed_commits\" ADD CONSTRAINT IF NOT EXISTS \"reviewed_commits_id\" PRIMARY KEY(\"user_id\", \"sha\")").execute()
    }
    userDao.findAll().foreach { user =>
      logger.debug(s"Migrate reviewed commits for user ${user.name}")
      val reviewTasksPending = taskDao.commitsPendingReviewFor(user.id)
      commitDao.findAllIds()
        .filterNot(reviewTasksPending.contains)
        .flatMap(commitDao.findByCommitId)
        .filterNot(CommitAuthorClassification.commitAuthoredByUser(_, user))
        .map( c=> ReviewedCommit.apply(c.sha, user.id, clock.nowUtc))
        .foreach(storeReviewedCommit)
      logger.debug(s"Migrate reviewed commits for user ${user.name} - Done")
    }
    logger.debug(s"Migrate reviewed commits - Done")
  }

  private def storeReviewedCommit(commit: ReviewedCommit) {
    try {
      reviewedCommitsDao.storeReviewedCommit(commit)
    } catch {
      case e: Exception => logger.warn(s"Looks like commit ${commit.sha} was already reviewed by user. Skipping.")
    }
  }

}

trait CleanpCommitDuplicates extends Logging {

  def commitInfoDao: CommitInfoDAO

  def cleanupCommitDuplicates(sqlDb: SQLDatabase) = {
    logger.debug(s"Cleaning up duplicated commits")

    def delete(id: ObjectId) {
      (Q.u + "delete from \"commit_infos_parents\" where \"commit_info_id\" = " +? id.toString).execute
      (Q.u + "delete from \"commit_infos\" where \"id\" = " +? id.toString).execute
      (Q.u + "delete from \"comments\" where \"commit_id\" = " +? id.toString).execute
      (Q.u + "delete from \"likes\" where \"commit_id\" = " +? id.toString).execute
      (Q.u + "delete from \"commit_review_tasks\" where \"commit_id\" = " +? id.toString).execute
      (Q.u + "delete from \"followups\" where \"thread_commit_id\" = " +? id.toString).execute
    }

    sqlDb.db.withDynSession {
      logger.debug("Deleting duplicated commits")
      findDuplicates().foreach(delete)
      logger.debug("Deleting duplicated commits - done")
    }

  }


  private def findDuplicates() = {
    var seenSha = new scala.collection.mutable.HashSet[String]
    var idsToRemove = new mutable.HashSet[ObjectId]
    commitInfoDao.findAllIds().foreach(determineIfDuplicate)
    idsToRemove
    def determineIfDuplicate(commitId: ObjectId) {
      val commit = commitInfoDao.findByCommitId(commitId).get
      if (seenSha.contains(commit.sha)) {
        idsToRemove.add(commit.id)
      } else {
        seenSha.add(commit.sha)
      }
    }
    logger.debug(s"About to remove ${idsToRemove.size} redundant commits")
    idsToRemove
  }

}