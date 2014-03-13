package com.softwaremill.codebrag.service.commits.branches

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging

class ReviewedCommitsCache extends Logging {

  // TODO: add backend to persist reviewed commits
  // TODO: constraint cache size (need to figure out which commits to remove from cache)

  private val commitsPerUser = new scala.collection.mutable.HashMap[ObjectId, Set[CommitCacheEntry]]
  
  def markCommitsAsReviewedBy(userId: ObjectId, commits: Set[CommitCacheEntry]) {
    logger.debug("THIS IS COMPLETELY IN-MEMORY IMPL. ADD BACKEND TO PERSIST STUFF")
    logger.debug(s"Adding ${commits.size} commits as reviewed by user ${userId}")
    val modifiedCommits = commitsPerUser.get(userId) match {
      case Some(reviewedCommits) => reviewedCommits ++ commits
      case None => commits
    }
    commitsPerUser.put(userId, modifiedCommits)
    logger.debug(s"Reviewed commits count for user ${userId}: ${reviewedByUser(userId).size}")
  }

  def reviewedByUser(userId: ObjectId) = {
    commitsPerUser.getOrElse(userId, Set.empty)
  }

}
