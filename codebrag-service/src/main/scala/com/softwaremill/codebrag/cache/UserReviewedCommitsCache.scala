package com.softwaremill.codebrag.cache

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.{UserRepoDetails, ReviewedCommit}
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO

class UserReviewedCommitsCache(userDao: UserDAO, reviewedCommitsDao: ReviewedCommitsDAO, userRepoDetailsDao: UserRepoDetailsDAO) extends Logging {

  private val userEntries = new ConcurrentHashMap[UserReviewedRepoCommitsCacheKey, UserReviewedCommitsCacheEntry]

  def initializeEmptyCacheFor(userRepo: UserRepoDetails) {
    val key = UserReviewedRepoCommitsCacheKey(userRepo.userId, userRepo.repoName)
    val value = UserReviewedCommitsCacheEntry(userRepo.userId, userRepo.repoName, Set.empty, userRepo.toReviewSince)
    userEntries.put(key, value)
  }

  def getEntry(userId: ObjectId, repoName: String): UserReviewedCommitsCacheEntry = {
    val key = UserReviewedRepoCommitsCacheKey(userId, repoName)
    Option(userEntries.get(key)) match {
      case Some(entry) => entry
      case None => {
        loadCacheEntryForKey(key)
        userEntries.get(key)
      }
    }
  }

  def markCommitAsReviewed(reviewedCommit: ReviewedCommit) {
    reviewedCommitsDao.storeReviewedCommit(reviewedCommit)
    addToCache(reviewedCommit)
  }

  def initializeUserDefaultCache(userId: ObjectId, repoName: String) {
    getEntry(userId, repoName)
  }

  def usersWhoReviewed(repoName: String, sha: String) = {
    userEntries.filter(_._1.repoName == repoName).flatMap { entry =>
      entry._2.commits.find(_.sha == sha) match {
        case Some(found) => Some(entry._1)
        case None => None
      }
    }.map(_.userId).toSet
  }

  def reviewedByUser(sha: String, repoName: String, userId: ObjectId) = {
    getEntry(userId, repoName).commits.map(_.sha).contains(sha)
  }

  private def loadCacheEntryForKey(key: UserReviewedRepoCommitsCacheKey) {
    logger.debug(s"Not found cache entry for $key, trying to load reviewed commits from DB and put in cache")
    userRepoDetailsDao.find(key.userId, key.repoName) match {
      case Some(details) => loadCacheEntryForUserRepo(details)
      case None => throw new IllegalStateException(s"No repository details found: $key")
    }
  }

  private def loadCacheEntryForUserRepo(userRepoDetails: UserRepoDetails) {
    val commits = reviewedCommitsDao.allReviewedByUser(userRepoDetails.userId, userRepoDetails.repoName)
    val key = UserReviewedRepoCommitsCacheKey(userRepoDetails.userId, userRepoDetails.repoName)
    val entry = UserReviewedCommitsCacheEntry(userRepoDetails.userId, userRepoDetails.repoName, commits, userRepoDetails.toReviewSince)
    userEntries.put(key, entry)
    logger.debug(s"User ${key.userId}, has ${commits.size} commits reviewed in repo ${key.repoName} and start date set to ${entry.toReviewStartDate}")
  }

  private def addToCache(reviewedCommit: ReviewedCommit) {
    val key = UserReviewedRepoCommitsCacheKey(reviewedCommit.userId, reviewedCommit.repoName)
    Option(userEntries.get(key)) match {
      case Some(userEntry) => {
        val updatedEntry = userEntry.copy(commits = userEntry.commits + reviewedCommit)
        userEntries.put(key, updatedEntry)
        logger.debug(s"Reviewed commits count for $key: ${updatedEntry.commits.size}")
      }
      case None => logger.error(s"Could not find $key entry in cache")
    }
  }

}