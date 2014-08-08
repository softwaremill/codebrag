package com.softwaremill.codebrag.usecases.notifications

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.domain.UserWatchedBranch

case class RepoBranchNotificationView(repoName: String, branchName: String, commits: Int)
case class UserNotificationsView(followups: Int, repos: Set[RepoBranchNotificationView]) {
  def nonEmpty = followups > 0 || repos.nonEmpty
}

class FindUserNotifications(
  followupFinder: FollowupFinder,
  heartbeat: HeartbeatDAO,
  toReviewCommitsFinder: ToReviewCommitsFinder,
  watchedBranchesDao: WatchedBranchesDao) {

  def execute(userId: ObjectId) = {
    heartbeat.update(userId)
    findNotifications(userId)
  }

  private def findNotifications(userId: ObjectId): UserNotificationsView = {
    val followupsCount = followupFinder.countFollowupsForUser(userId)
    val branchCommitsCount = watchedBranchesDao.findAll(userId).map(findBranchCommitsToReview)
    UserNotificationsView(followupsCount.toInt, branchCommitsCount)
  }

  private def findBranchCommitsToReview(wb: UserWatchedBranch) = {
    val bc = UserBrowsingContext(wb.userId, wb.repoName, wb.branchName)
    RepoBranchNotificationView(bc.repoName, bc.branchName, toReviewCommitsFinder.count(bc).toInt)
  }
}