package com.softwaremill.codebrag.service.commits.branches

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO

class ReviewedCommitsCache(userDao: UserDAO, reviewedCommitsDao: ReviewedCommitsDAO) extends Logging {

  private val reviewedCommitsPerUser = new scala.collection.mutable.HashMap[ObjectId, Set[ReviewedCommit]]
  private val userToReviewStartDates = new mutable.HashMap[ObjectId, DateTime]

  def setToReviewStartDateForUser(date: DateTime, userId: ObjectId) {
    userDao.setToReviewStartDate(userId, date)
    userToReviewStartDates.put(userId, date)
  }

  def getToReviewStartDateForUser(userId: ObjectId): DateTime = {
    userToReviewStartDates.get(userId) match {
      case Some(date) => date
      case None => {
        lazyInitCacheForUser(userId)
        getToReviewStartDateForUser(userId)
      }
    }
  }

  def reviewedByUser(userId: ObjectId): Set[ReviewedCommit] = {
    reviewedCommitsPerUser.get(userId) match {
      case Some(found) => found
      case None => {
        lazyInitCacheForUser(userId)
        reviewedByUser(userId)
      }
    }
  }

  def markCommitAsReviewed(reviewedCommit: ReviewedCommit) {
    storeReviewedCommit(reviewedCommit)
    addToCache(reviewedCommit)
  }

  def loadUserReviewedCommitsToCache(userId: ObjectId) = lazyInitCacheForUser(userId)

  private def lazyInitCacheForUser(userId: ObjectId) {
    logger.debug(s"Not found cache entry for user ${userId}, trying to load reviewed commits from DB and put in cache")
    val fetched = reviewedCommitsDao.allReviewedByUser(userId)
    reviewedCommitsPerUser.put(userId, fetched)
    logger.debug(s"User ${userId}, has ${fetched.size} commits reviewed")
    userDao.findById(userId).foreach { user =>
      user.settings.toReviewStartDate match {
        case Some(date) => {
          userToReviewStartDates.put(userId, date)
          logger.debug(s"User ${userId}, has start date set to ${date}")
        }
        case None => throw new IllegalStateException(s"User ${userId} doesn't have toReviewStartDate set in DB")
      }
    }
  }

  private def storeReviewedCommit(reviewedCommit: ReviewedCommit) {
    reviewedCommitsDao.storeReviewedCommit(reviewedCommit)
  }

  private def addToCache(reviewedCommit: ReviewedCommit) {
    val modifiedCommits = reviewedCommitsPerUser.get(reviewedCommit.userId) match {
      case Some(reviewedCommits) => reviewedCommits + reviewedCommit
      case None => Set(reviewedCommit)
    }
    reviewedCommitsPerUser.put(reviewedCommit.userId, modifiedCommits)
    logger.debug(s"Reviewed commits count for user ${reviewedCommit.userId}: ${modifiedCommits.size}")
  }

}