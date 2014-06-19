package com.softwaremill.codebrag.finders.commits.toreview

import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, UserReviewedCommitsCache}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import com.softwaremill.codebrag.domain.User
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitAuthorClassification._

class ToReviewBranchCommitsFilter(reviewedCommitsCache: UserReviewedCommitsCache, config: ReviewProcessConfig) {

   def filterCommitsToReview(branchCommits: List[BranchCommitCacheEntry], user: User) = {
     val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
     branchCommits
       .filterNot(userOrDoneCommits(_, user))
       .takeWhile(commitsAfterUserDate(_, userBoundaryDate))
       .filter(notYetFullyReviewed)
       .map(_.sha)
       .reverse
   }

   private def userOrDoneCommits(commitEntry: BranchCommitCacheEntry, user: User) = {
     commitAuthoredByUser(commitEntry, user) || userAlreadyReviewed(user.id, commitEntry)
   }


   private def commitsAfterUserDate(commitEntry: BranchCommitCacheEntry, userBoundaryDate: DateTime): Boolean = {
     commitEntry.commitDate.isAfter(userBoundaryDate) || commitEntry.commitDate.isEqual(userBoundaryDate)
   }

   private def notYetFullyReviewed(commitEntry: BranchCommitCacheEntry): Boolean = {
     reviewedCommitsCache.usersWhoReviewed(commitEntry.sha).size < config.requiredReviewersCount
   }

   private def userAlreadyReviewed(userId: ObjectId, commit: BranchCommitCacheEntry): Boolean = {
     val commitsReviewedByUser = reviewedCommitsCache.getUserEntry(userId).commits
     commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
   }

 }
