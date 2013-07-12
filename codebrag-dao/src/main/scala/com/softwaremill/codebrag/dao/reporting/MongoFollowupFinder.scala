package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao._
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views._
import com.softwaremill.codebrag.dao.reporting.views.SingleFollowupView
import com.softwaremill.codebrag.dao.reporting.views.FollowupCommitView
import scala.Some

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView = {
    val followupRecords = findUserFollowups(userId)
    val lastReactions = findLastReactionsForFollowups(followupRecords)
    val reactionAuthors = findReactionAuthors(lastReactions)
    val commits = findCommitsForFollowups(followupRecords)

    val followupsGroupedByCommit = followupRecords.groupBy(_.threadId.get.commitId.get)

    val sortFollowupsForCommitByDate = (f1: FollowupReactionsView, f2: FollowupReactionsView) => f1.lastReaction.date.after(f2.lastReaction.date)

    val followupsForCommits = followupsGroupedByCommit.map { case(commitId, followups) =>
      val followupsForCommitViews = followups.map(followupToReactionsView(_, lastReactions, reactionAuthors)).sortWith(sortFollowupsForCommitByDate)
      val commit = commits(commitId)
      val commitView = FollowupCommitView(commit.id.get.toString, commit.authorName.get, commit.message.get, commit.authorDate.get)
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
    val reactionView = FollowupLastReactionView(reaction.id.get.toString, author.name.get, reaction.date.get, Some(author.avatarUrl.get))
    val allReactions = followup.reactions.get.map(reactionId => reactionId.toString)
    FollowupReactionsView(followup.id.toString, reactionView, allReactions)
  }

  private def recordsToFollowupView(commit: CommitInfoRecord, reaction: UserReactionRecord[_], author: UserRecord, followup: FollowupRecord): SingleFollowupView = {
    val commitView = FollowupCommitView(commit.id.get.toString, commit.authorName.get, commit.message.get, commit.authorDate.get)
    val reactionView = FollowupLastReactionView(reaction.id.get.toString, author.name.get, reaction.date.get, Some(author.avatarUrl.get))
    val followupView = SingleFollowupView(followup.id.get.toString, reaction.date.get, commitView, reactionView)
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

}
