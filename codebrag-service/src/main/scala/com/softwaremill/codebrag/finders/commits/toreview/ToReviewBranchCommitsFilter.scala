package com.softwaremill.codebrag.finders.commits.toreview

import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, UserReviewedCommitsCache}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import com.softwaremill.codebrag.domain.User
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitAuthorClassification._

class ToReviewBranchCommitsFilter(reviewedCommitsCache: UserReviewedCommitsCache, config: ReviewProcessConfig) {

   def filterCommitsToReview(branchCommits: List[BranchCommitCacheEntry], user: User, repoName: String) = {
     val userBoundaryDate = reviewedCommitsCache.getEntry(user.id, repoName).toReviewStartDate
     filterCommitsToReviewSince(userBoundaryDate, branchCommits, user, repoName)
   }

   def filterCommitsToReviewSince(date: DateTime, branchCommits: List[BranchCommitCacheEntry], user: User, repoName: String) = {
     branchCommits
       .filterNot(userOrDoneCommits(repoName, _, user))
       .filterNot(commitTooOld(_, date))
       .filter(notYetFullyReviewed(repoName, _))
       .map(_.sha)
       .reverse
   }

   private def userOrDoneCommits(repoName: String, commitEntry: BranchCommitCacheEntry, user: User): Boolean = {
     commitAuthoredByUser(commitEntry, user) || userAlreadyReviewed(user.id, repoName, commitEntry)
   }

   private def commitTooOld(commitEntry: BranchCommitCacheEntry, userBoundaryDate: DateTime): Boolean = {
     commitEntry.commitDate.isBefore(userBoundaryDate)
   }

   private def notYetFullyReviewed(repoName: String, commitEntry: BranchCommitCacheEntry): Boolean = {
     reviewedCommitsCache.usersWhoReviewed(repoName, commitEntry.sha).size < config.requiredReviewersCount
   }

   private def userAlreadyReviewed(userId: ObjectId, repoName: String, commit: BranchCommitCacheEntry): Boolean = {
     val commitsReviewedByUser = reviewedCommitsCache.getEntry(userId, repoName).commits
     commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
   }
}
