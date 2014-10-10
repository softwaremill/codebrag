package com.softwaremill.codebrag.usecases.notifications

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.domain.UserWatchedBranch
import org.joda.time.DateTime

case class RepoBranchNotificationView(repoName: String, branchName: String, commits: Int)
case class UserNotificationsView(followups: Int, repos: Set[RepoBranchNotificationView]) {
  def nonEmpty = followups > 0 || repos.nonEmpty
  def commits = repos.foldLeft(0)(_ + _.commits)
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
  
  def executeSince(date: DateTime, userId: ObjectId) = {
    heartbeat.update(userId)
    val n = findNotificationsSince(date, userId)
    n.copy(repos = n.repos.filter(_.commits > 0))
  }

  private def findNotifications(userId: ObjectId): UserNotificationsView = {
    val followupsCount = followupFinder.countFollowupsForUser(userId)
    val branchCommitsCount = watchedBranchesDao.findAll(userId).map(findBranchCommitsToReview)
    UserNotificationsView(followupsCount.toInt, branchCommitsCount)
  }

  private def findNotificationsSince(date: DateTime, userId: ObjectId): UserNotificationsView = {
    val followupsCount = followupFinder.countFollowupsForUserSince(date, userId)
    val branchCommitsCount = watchedBranchesDao.findAll(userId).map(wb => findBranchCommitsToReviewSince(date, wb))
    UserNotificationsView(followupsCount.toInt, branchCommitsCount)
  }

  private def findBranchCommitsToReview(wb: UserWatchedBranch) = {
    val bc = UserBrowsingContext(wb.userId, wb.repoName, wb.branchName)
    RepoBranchNotificationView(bc.repoName, bc.branchName, toReviewCommitsFinder.count(bc).toInt)
  }

  private def findBranchCommitsToReviewSince(date: DateTime, wb: UserWatchedBranch) = {
    val bc = UserBrowsingContext(wb.userId, wb.repoName, wb.branchName)
    RepoBranchNotificationView(bc.repoName, bc.branchName, toReviewCommitsFinder.countSince(date, bc).toInt)
  }
}