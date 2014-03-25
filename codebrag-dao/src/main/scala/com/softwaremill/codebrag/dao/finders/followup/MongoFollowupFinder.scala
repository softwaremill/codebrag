package com.softwaremill.codebrag.dao.finders.followup

import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import net.liftweb.mongodb.record.MongoRecord
import com.softwaremill.codebrag.dao.user.{LikeRecord, CommentRecord, UserReactionRecord, UserRecord}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoRecord
import com.softwaremill.codebrag.dao.followup.FollowupRecord
import com.softwaremill.codebrag.dao.finders.views._
import com.softwaremill.codebrag.dao.finders.views.SingleFollowupView
import com.softwaremill.codebrag.dao.finders.views.FollowupsByCommitListView
import scala.Some
import com.softwaremill.codebrag.dao.finders.views.FollowupsByCommitView
import com.softwaremill.codebrag.dao.finders.views.FollowupCommitView
import org.joda.time.DateTime

class MongoFollowupFinder extends FollowupFinder {

  def countFollowupsForUserSince(date: DateTime, userId: ObjectId) = 0

  def countFollowupsForUser(userId: ObjectId) = 0

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView = {
    val followupRecords = findUserFollowups(userId)
    val lastReactions = findLastReactionsForFollowups(followupRecords)
    val reactionAuthors = findReactionAuthors(lastReactions)
    val commits = findCommitsForFollowups(followupRecords)

    val followupsGroupedByCommit = followupRecords.groupBy(_.threadId.get.commitId.get)

    val sortFollowupsForCommitByDate = (f1: FollowupReactionsView, f2: FollowupReactionsView) => f1.lastReaction.date.after(f2.lastReaction.date)

    val followupsForCommits = followupsGroupedByCommit.map {
      case (commitId, followups) =>
        val followupsForCommitViews = followups.map(followupToReactionsView(_, lastReactions, reactionAuthors)).sortWith(sortFollowupsForCommitByDate)
        val commit = commits(commitId)
        val commitView = FollowupCommitView(commit.id.get.toString, commit.sha.get, commit.authorName.get, commit.message.get, commit.authorDate.get)
        FollowupsByCommitView(commitView, followupsForCommitViews)
    }
    FollowupsByCommitListView(sortFollowupGroupsByNewest(followupsForCommits))
  }

  private def findCommitsForFollowups(followupRecords: List[FollowupRecord]) = {
    val commitsIds = followupRecords.map(_.threadId.get.commitId.get)
    CommitInfoRecord.where(_.id in commitsIds).fetch().map(commit => (commit.id.get, commit)).toMap
  }

  private def findReactionAuthors(lastReactions: Map[ObjectId, UserReactionRecord[_]]) = {
    val reactionAuthorsIds = lastReactions.map(_._2.authorId.get)
    UserRecord.where(_.id in reactionAuthorsIds).fetch().map(author => (author.id.get, author)).toMap
  }

  private def findUserFollowups(userId: ObjectId): List[FollowupRecord] = {
    FollowupRecord.where(_.receivingUserId eqs userId).fetch()
  }

  private def findLastReactionsForFollowups(followupRecords: List[FollowupRecord]) = {
    val lastReactionsIds = followupRecords.map(_.lastReaction.get.reactionId.get)
    val lastLikesReactions = LikeRecord.where(_.id in lastReactionsIds).fetch()
    val lastCommentsReactions = CommentRecord.where(_.id in lastReactionsIds).fetch()
    (lastLikesReactions ++ lastCommentsReactions).map(reaction => (reaction.id.get, reaction)).toMap[ObjectId, UserReactionRecord[_]]
  }

  private def sortFollowupGroupsByNewest(followupsForCommits: Iterable[FollowupsByCommitView]): List[FollowupsByCommitView] = {
    val followupsGroupsSorted = followupsForCommits.toList.sortWith((f1, f2) => {
      val firstMaxDate = f1.followups.maxBy(_.lastReaction.date).lastReaction.date
      val secondMaxDate = f2.followups.maxBy(_.lastReaction.date).lastReaction.date
      firstMaxDate.after(secondMaxDate)
    })
    followupsGroupsSorted
  }

  private def followupToReactionsView(followup: FollowupRecord, lastReactions: Map[ObjectId, UserReactionRecord[_]], reactionAuthors: Map[ObjectId, UserRecord]): FollowupReactionsView = {
    val reaction = lastReactions(followup.lastReaction.get.reactionId.get)
    val author = reactionAuthors(reaction.authorId.get)
    val lastReactionView = buildLastReactionView(reaction, author)
    val allReactions = followup.reactions.get.map(reactionId => reactionId.toString)
    FollowupReactionsView(followup.id.toString, lastReactionView, allReactions)
  }

  private def recordsToFollowupView(commit: CommitInfoRecord, reaction: UserReactionRecord[_], author: UserRecord, followup: FollowupRecord): SingleFollowupView = {
    val commitView = FollowupCommitView(commit.id.get.toString, commit.sha.get, commit.authorName.get, commit.message.get, commit.authorDate.get)
    val lastReactionView = buildLastReactionView(reaction, author)
    val followupView = SingleFollowupView(followup.id.get.toString, reaction.date.get, commitView, lastReactionView)
    followupView
  }

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId) = {
    FollowupRecord.where(_.receivingUserId eqs userId).and(_.id eqs followupId).get() match {
      case Some(followup) => {
        val reaction = findLastReaction(followup.lastReaction.get.reactionId.get)
        val Some(author) = UserRecord.where(_.id eqs reaction.authorId.get).get()
        val Some(commit) = CommitInfoRecord.where(_.id eqs followup.threadId.get.commitId.get).get()
        Right(recordsToFollowupView(commit, reaction, author, followup))
      }
      case None => Left("No such followup")
    }
  }

  private def findLastReaction(id: ObjectId): UserReactionRecord[_] = {
    val commentOpt = CommentRecord.where(_.id eqs id).get()
    commentOpt match {
      case Some(comment) => comment
      case None => {
        LikeRecord.where(_.id eqs id).get.get
      }
    }
  }

  private def buildLastReactionView[T <: MongoRecord[T]](reaction: UserReactionRecord[_], author: UserRecord): FollowupLastReactionView = {
    reaction.asInstanceOf[MongoRecord[T]] match {
      case comment: CommentRecord => FollowupLastCommentView(comment.id.get.toString, author.name.get, comment.date.get, author.userSettings.get.avatarUrl.get, comment.message.get)
      case like: LikeRecord => FollowupLastLikeView(like.id.get.toString, author.name.get, like.date.get, author.userSettings.get.avatarUrl.get)
    }
  }

}
