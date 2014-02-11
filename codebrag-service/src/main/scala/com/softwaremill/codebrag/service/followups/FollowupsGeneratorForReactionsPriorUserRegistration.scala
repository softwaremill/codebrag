package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.common.Clock
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.comment.CommitCommentDAO

class FollowupsGeneratorForReactionsPriorUserRegistration(
  commentsDao: CommitCommentDAO,
  likesDao: LikeDAO,
  followupsDao: FollowupDAO,
  commitInfoDao: CommitInfoDAO, codebragConfig: CodebragConfig)(implicit val clock: Clock) extends Logging {

  def recreateFollowupsForPastComments(newUser: NewUserRegistered) {
    logger.debug("Attempting to create followups for comments already placed in user commits")
    val recentUserCommitsIds = findRecentUserCommits(newUser)
    if(recentUserCommitsIds.nonEmpty) {
      val allReactionsOrdered = findAllReactionsToUsersCommitsWithOrder(recentUserCommitsIds)
      logger.debug("Reactions to create followups for: " + allReactionsOrdered.size)
      allReactionsOrdered.foreach { reaction =>
        logger.debug("Replaying followup for reaction" + reaction)
        followupsDao.createOrUpdateExisting(Followup(newUser.id, reaction))
      }
    } else {
      logger.debug("No recent user commits found")
    }
  }

  private def findAllReactionsToUsersCommitsWithOrder(recentUserCommitsIds: List[ObjectId]) = {
    val recentComments = commentsDao.findCommentsForCommits(recentUserCommitsIds:_*)
    val recentLikes = likesDao.findLikesForCommits(recentUserCommitsIds:_*)
    def sortByDate(r1: UserReaction, r2: UserReaction) = r1.postingTime.isBefore(r2.postingTime)
    (recentComments ++ recentLikes).sortWith(sortByDate)
  }

  private def findRecentUserCommits(newUser: NewUserRegistered): List[ObjectId] = {
    val timeRangeForCommits = clock.now.minusDays(codebragConfig.replayFollowupsForPastCommitsTimeInDays)
    commitInfoDao.findLastCommitsAuthoredByUserSince(newUser, timeRangeForCommits).map(_.id)
  }

}