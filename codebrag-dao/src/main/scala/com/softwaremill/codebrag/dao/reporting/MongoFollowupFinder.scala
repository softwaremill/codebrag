package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao._
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views._
import scala.Some
import com.softwaremill.codebrag.dao.reporting.views.FollowupView
import com.softwaremill.codebrag.dao.reporting.views.FollowupReactionView
import com.softwaremill.codebrag.dao.reporting.views.FollowupCommitView
import scala.Some
import com.softwaremill.codebrag.dao.reporting.views.FollowupListView

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView = {

    val followupRecords = FollowupRecord.where(_.receivingUserId eqs userId).fetch()

    val lastReactionsIds = followupRecords.map(_.lastReaction.get.reactionId.get)

    val lastLikesReactions = LikeRecord.where(_.id in lastReactionsIds).fetch()
    val lastCommentsReactions = CommentRecord.where(_.id in lastReactionsIds).fetch()
    val lastReactions = (lastLikesReactions ++ lastCommentsReactions).map(reaction => (reaction.id.get, reaction)).toMap[ObjectId, UserReactionRecord[_]]

    val reactionAuthorsIds = lastReactions.map(_._2.authorId.get)
    val reactionAuthors = UserRecord.where(_.id in reactionAuthorsIds).fetch().map(author => (author.id.get, author)).toMap

    val commitsIds = followupRecords.map(_.threadId.get.commitId.get)
    val commits = CommitInfoRecord.where(_.id in commitsIds).fetch().map(commit => (commit.id.get, commit)).toMap

    val followupsGroupedByCommit = followupRecords.groupBy(_.threadId.get.commitId.get)

    val followupsForCommits = followupsGroupedByCommit.map { case(commitId, followups) =>

      val followupsForCommitViews = followups.map { followup =>
        val reaction = lastReactions(followup.lastReaction.get.reactionId.get)
        val author = reactionAuthors(reaction.authorId.get)
        val reactionView = FollowupLastReactionView(reaction.id.get.toString, author.name.get, reaction.date.get, Some(author.avatarUrl.get))
        val allReactions = followup.reactions.get.map(reactionId => reactionId.toString)
        FollowupReactions(followup.id.toString, reactionView, allReactions)
      }.sortWith((f1, f2) => f1.lastReaction.date.after(f2.lastReaction.date))

      val commit = commits(commitId)
      val commitView = FollowupCommitView(commit.id.get.toString, commit.authorName.get, commit.message.get, commit.authorDate.get)
      FollowupsByCommitView(commitView, followupsForCommitViews)
    }

    val followupsGroupsSorted = followupsForCommits.toList.sortWith((f1, f2) => {
      val firstMaxDate = f1.followups.maxBy(_.lastReaction.date).lastReaction.date
      val secondMaxDate = f2.followups.maxBy(_.lastReaction.date).lastReaction.date
      firstMaxDate.after(secondMaxDate)
    })
    FollowupsByCommitListView(followupsGroupsSorted)
  }

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView = {

    val followupRecords = FollowupRecord.where(_.receivingUserId eqs userId).fetch()

    val lastReactionsIds = followupRecords.map(_.lastReaction.get.reactionId.get)

    val lastLikesReactions = LikeRecord.where(_.id in lastReactionsIds).fetch()
    val lastCommentsReactions = CommentRecord.where(_.id in lastReactionsIds).fetch()
    val lastReactions = (lastLikesReactions ++ lastCommentsReactions).map(reaction => (reaction.id.get, reaction)).toMap[ObjectId, UserReactionRecord[_]]

    val reactionAuthorsIds = lastReactions.map(_._2.authorId.get)
    val reactionAuthors = UserRecord.where(_.id in reactionAuthorsIds).fetch().map(author => (author.id.get, author)).toMap

    val commitsIds = followupRecords.map(_.threadId.get.commitId.get)
    val commits = CommitInfoRecord.where(_.id in commitsIds).fetch().map(commit => (commit.id.get, commit)).toMap


    val followupsViews = followupRecords.map { followup =>
      val commit = commits(followup.threadId.get.commitId.get)
      val reaction = lastReactions(followup.lastReaction.get.reactionId.get)
      val author = reactionAuthors(reaction.authorId.get)

      val followupView = recordsToFollowupView(commit, reaction, author, followup)

      followupView
    }.sortWith((f1, f2) => f1.date.after(f2.date))

    FollowupListView(followupsViews)
  }


  def recordsToFollowupView(commit: CommitInfoRecord, reaction: UserReactionRecord[_], author: UserRecord, followup: FollowupRecord): FollowupView = {
    val commitView = FollowupCommitView(commit.id.get.toString, commit.authorName.get, commit.message.get, commit.authorDate.get)
    val reactionView = FollowupReactionView(reaction.id.get.toString, author.name.get, Some(author.avatarUrl.get))
    val followupView = FollowupView(followup.id.get.toString, reaction.date.get, commitView, reactionView)
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

  def findLastReaction(id: ObjectId): UserReactionRecord[_] = {
    val commentOpt = CommentRecord.where(_.id eqs id).get()
    commentOpt match {
      case Some(comment) => comment
      case None => {
        LikeRecord.where(_.id eqs id).get.get
      }
    }
  }

}
