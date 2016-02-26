package com.softwaremill.codebrag.dao.finders.followup

import com.softwaremill.codebrag.common.Joda
import com.softwaremill.codebrag.dao.followup.SQLFollowupSchema
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reaction.SQLReactionSchema
import com.softwaremill.codebrag.domain.{PartialUserDetails, UserReaction, Like, Comment}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.SQLCommitInfoSchema
import com.softwaremill.codebrag.dao.finders.views._
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.dao.finders.views.FollowupsByCommitListView
import com.softwaremill.codebrag.dao.finders.views.FollowupReactionsView
import com.softwaremill.codebrag.dao.finders.views.FollowupsByCommitView
import com.softwaremill.codebrag.dao.finders.views.FollowupCommitView
import org.joda.time.DateTime

class SQLFollowupFinder(val database: SQLDatabase, userDAO: UserDAO) extends FollowupFinder with SQLFollowupSchema
  with SQLReactionSchema with SQLCommitInfoSchema {
                                            
  import database.driver.simple._
  import database._
  import Joda._
  
  def countFollowupsForUser(userId: ObjectId) = db.withTransaction { implicit session =>
    Query(followups.where(_.receivingUserId === userId).length).first()
  }

  def countFollowupsForUserSince(date: DateTime, userId: ObjectId) = db.withTransaction { implicit session =>
    Query(followups.where(f => f.receivingUserId === userId && f.lastReactionCreatedDate > date).length).first()
  }

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView = db.withTransaction { implicit session =>
    val followups = findUserFollowups(userId)
    val followupReactions = findFollowupReactions(followups)
    val lastReactions = findLastReactionsForFollowups(followups)
    val reactionAuthors = findReactionAuthors(lastReactions)
    val commits = findCommitsForFollowups(followups)

    val followupsGroupedByCommit = followups.groupBy(_.threadCommitId)

    val sortFollowupsForCommitByDate = (f1: FollowupReactionsView, f2: FollowupReactionsView) => f1.lastReaction.date.isAfter(f2.lastReaction.date)

    val followupsForCommits = followupsGroupedByCommit.map {
      case (commitId, commitFollowups) =>
        val followupsForCommitViews = commitFollowups
          .map(f => followupToReactionsView(f, followupReactions.getOrElse(f.id, Nil), lastReactions, reactionAuthors))
          .sortWith(sortFollowupsForCommitByDate)
        val commit = commits(commitId)
        val commitView = FollowupCommitView(commit.id.toString, commit.sha, commit.repoName, commit.authorName, commit.message, commit.authorDate)
        FollowupsByCommitView(commitView, followupsForCommitViews)
    }
    FollowupsByCommitListView(sortFollowupGroupsByNewest(followupsForCommits))
  }

  private def findCommitsForFollowups(followups: List[SQLFollowup])(implicit session: Session) = {
    val commitsIds = followups.map(_.threadCommitId).toSet
    commitInfos.filter(_.id inSet commitsIds).list().map(commit => commit.id -> commit).toMap
  }

  private def findFollowupReactions(followups: List[SQLFollowup])(implicit session: Session) = {
    followupsReactions
      .filter(_.followupId inSet followups.map(_.id).toSet)
      .list()
      .groupBy(_._1)
      .map { case (k, v) => k -> v.map(_._2) }
      .toMap
  }

  private def findReactionAuthors(lastReactions: Map[ObjectId, UserReaction])(implicit session: Session) = {
    val reactionAuthorsIds = lastReactions.map(_._2.authorId).toSet
    userDAO.findPartialUserDetails(reactionAuthorsIds).map(pud => pud.id -> pud).toMap
  }

  private def findUserFollowups(userId: ObjectId)(implicit session: Session): List[SQLFollowup] = {
    followups.filter(_.receivingUserId === userId).list()
  }

  private def findLastReactionsForFollowups(followups: List[SQLFollowup])(implicit session: Session) = {
    val lastReactionsIds = followups.map(_.lastReactionId).toSet
    val lastLikesReactions = likes.filter(_.id inSet lastReactionsIds).list()
    val lastCommentsReactions = comments.filter(_.id inSet lastReactionsIds).list()
    (lastLikesReactions ++ lastCommentsReactions).map(reaction => (reaction.id, reaction)).toMap[ObjectId, UserReaction]
  }

  private def sortFollowupGroupsByNewest(followupsForCommits: Iterable[FollowupsByCommitView]): List[FollowupsByCommitView] = {
    val followupsGroupsSorted = followupsForCommits.toList.sortWith((f1, f2) => {
      val firstMaxDate = f1.followups.maxBy(_.lastReaction.date).lastReaction.date
      val secondMaxDate = f2.followups.maxBy(_.lastReaction.date).lastReaction.date
      firstMaxDate.isAfter(secondMaxDate)
    })
    followupsGroupsSorted
  }

  private def followupToReactionsView(followup: SQLFollowup, followupReactions: List[ObjectId], lastReactions: Map[ObjectId, UserReaction], reactionAuthors: Map[ObjectId, PartialUserDetails]): FollowupReactionsView = {
    val reaction = lastReactions(followup.lastReactionId)
    val author = reactionAuthors(reaction.authorId)
    val lastReactionView = buildLastReactionView(reaction, author)
    val allReactions = followupReactions.map(_.toString)
    FollowupReactionsView(followup.id.toString, lastReactionView, allReactions)
  }

  private def recordsToFollowupView(commit: SQLCommitInfo, reaction: UserReaction, author: PartialUserDetails, followup: SQLFollowup): SingleFollowupView = {
    val commitView = FollowupCommitView(commit.id.toString, commit.sha, commit.repoName, commit.authorName, commit.message, commit.authorDate)
    val lastReactionView = buildLastReactionView(reaction, author)
    val followupView = SingleFollowupView(followup.id.toString, reaction.postingTime, commitView, lastReactionView)
    followupView
  }

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId) = db.withTransaction { implicit session =>
    val r = for {
      followup <- followups.filter(f => f.receivingUserId === userId && f.id === followupId).firstOption
      reaction <- findLastReaction(followup.lastReactionId)
      author <- userDAO.findPartialUserDetails(List(reaction.authorId)).headOption
      commit <- commitInfos.filter(_.id === followup.threadCommitId).firstOption()
    } yield recordsToFollowupView(commit, reaction, author, followup)

    r.fold[Either[String, SingleFollowupView]](Left("No such followup"))(Right(_))
  }

  private def findLastReaction(id: ObjectId)(implicit session: Session): Option[UserReaction] = {
    val commentOpt = comments.filter(_.id === id).firstOption()
    commentOpt.orElse(likes.where(_.id === id).firstOption())
  }

  private def buildLastReactionView(reaction: UserReaction, author: PartialUserDetails): FollowupLastReactionView = {
    reaction match {
      case comment: Comment => FollowupLastCommentView(comment.id.toString, author.name, comment.postingTime, author.avatarUrl, comment.message)
      case like: Like => FollowupLastLikeView(like.id.toString, author.name, like.postingTime, author.avatarUrl)
    }
  }
 def findAllFollowupsByCommitForDashboard(): FollowupsByCommitListView = db.withTransaction { implicit session =>
    val followups = findAllFollowups()
    val followupReactions = findFollowupReactions(followups)
    val lastReactions = findLastReactionsForFollowups(followups)
    val reactionAuthors = findReactionAuthors(lastReactions)
    val commits = findCommitsForFollowups(followups)

    val followupsGroupedByCommit = followups.groupBy(_.threadCommitId)

    val sortFollowupsForCommitByDate = (f1: FollowupReactionsView, f2: FollowupReactionsView) => f1.lastReaction.date.isAfter(f2.lastReaction.date)

    val followupsForCommits = followupsGroupedByCommit.map {
      case (commitId, commitFollowups) =>
        val followupsForCommitViews = commitFollowups
          .map(f => followupToReactionsView(f, followupReactions.getOrElse(f.id, Nil), lastReactions, reactionAuthors))
          .sortWith(sortFollowupsForCommitByDate)
        val commit = commits(commitId)
        val commitView = FollowupCommitView(commit.id.toString, commit.sha, commit.repoName, commit.authorName, commit.message, commit.authorDate)
        FollowupsByCommitView(commitView, followupsForCommitViews)
    }
    FollowupsByCommitListView(sortFollowupGroupsByNewest(followupsForCommits))
  }
  private def findAllFollowups()(implicit session: Session): List[SQLFollowup] = {
    followups.list()
  }
    def findFollowupforDashboard(followupId: ObjectId) = db.withTransaction { implicit session =>
    val r = for {
      followup <- followups.filter(f => f.id === followupId).firstOption
      reaction <- findLastReaction(followup.lastReactionId)
      author <- userDAO.findPartialUserDetails(List(reaction.authorId)).headOption
      commit <- commitInfos.filter(_.id === followup.threadCommitId).firstOption()
    } yield recordsToFollowupView(commit, reaction, author, followup)

    r.fold[Either[String, SingleFollowupView]](Left("No such followup"))(Right(_))
  }
}
