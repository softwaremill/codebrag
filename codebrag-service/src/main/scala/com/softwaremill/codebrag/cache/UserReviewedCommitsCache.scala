package com.softwaremill.codebrag.cache

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO

class UserReviewedCommitsCache(userDao: UserDAO, reviewedCommitsDao: ReviewedCommitsDAO) extends Logging {

  private val userEntries = new scala.collection.mutable.HashMap[ObjectId, UserReviewedCommitsCacheEntry]

  def addNewUserEntry(newUserEntry: UserReviewedCommitsCacheEntry) {
    userDao.setToReviewStartDate(newUserEntry.userId, newUserEntry.toReviewStartDate)
    userEntries.put(newUserEntry.userId, newUserEntry)
  }

  def getUserEntry(userId: ObjectId): UserReviewedCommitsCacheEntry = {
    userEntries.get(userId) match {
      case Some(entry) => entry
      case None => {
        lazyInitCacheForUser(userId)
        userEntries.get(userId).getOrElse(throw new IllegalStateException(s"Cannot find reviewed commits data for user ${userId}"))
      }
    }
  }

  def markCommitAsReviewed(reviewedCommit: ReviewedCommit) {
    storeReviewedCommit(reviewedCommit)
    addToCache(reviewedCommit)
  }

  def loadUserDataToCache(userId: ObjectId) {
    getUserEntry(userId)
  }

  def usersWhoReviewed(sha: String) = {
    userEntries.flatMap { entry =>
      entry._2.commits.find(_.sha == sha) match {
        case Some(found) => Some(entry._1)
        case None => None
      }
    }.toSet
  }

  def initialize() {
    logger.debug("Initializing cache of reviewed commits for registered users")
    userDao.findAll().foreach(user => lazyInitCacheForUser(user.id))
  }

  private def lazyInitCacheForUser(userId: ObjectId) {
    logger.debug(s"Not found cache entry for user ${userId}, trying to load reviewed commits from DB and put in cache")
    userDao.findById(userId).foreach { user =>
      user.settings.toReviewStartDate match {
        case Some(date) => {
          val commits = reviewedCommitsDao.allReviewedByUser(userId)
          val entry = UserReviewedCommitsCacheEntry(userId, commits, date)
          userEntries.put(userId, entry)
          logger.debug(s"User ${userId}, has ${commits.size} commits reviewed and start date set to ${date}")
        }
        case None => throw new IllegalStateException(s"User ${userId} doesn't have toReviewStartDate set in DB")
      }
    }
  }

  private def storeReviewedCommit(reviewedCommit: ReviewedCommit) {
    reviewedCommitsDao.storeReviewedCommit(reviewedCommit)
  }

  private def addToCache(reviewedCommit: ReviewedCommit) {
    userEntries.get(reviewedCommit.userId) match {
      case Some(userEntry) => {
        val updatedEntry = userEntry.copy(commits = userEntry.commits + reviewedCommit)
        userEntries.put(reviewedCommit.userId, updatedEntry)
        logger.debug(s"Reviewed commits count for user ${reviewedCommit.userId}: ${updatedEntry.commits.size}")
      }
      case None => logger.error(s"Could not find user ${reviewedCommit.userId} entry in cache")
    }
  }

}