package com.softwaremill.codebrag.dao.finders.reaction

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{PartialUserDetails, Like, Comment, UserReaction}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.reaction.{LikeDAO, CommitCommentDAO}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.finders.views.{CommentView, LikeView, CommitReactionsView}

class ReactionFinder(val userDAO: UserDAO, commitCommentDAO: CommitCommentDAO, likeDAO: LikeDAO)
  extends Logging with UserReactionToViewMapper {

  def findLikeById(likeId: ObjectId) = {
    likeDAO.findById(likeId).map { like =>
      userDAO.findById(like.authorId) match {
        case Some(author) => {
          LikeView(like.id.toString, author.name, like.authorId.toString, like.postingTime.toDate, like.fileName, like.lineNumber)
        }
        case None => {
          logger.warn(s"Cannot find author with Id ${like.authorId} for like $likeId")
          LikeView(like.id.toString, "", like.authorId.toString, like.postingTime.toDate, like.fileName, like.lineNumber)
        }
      }
    }
  }

  def findReactionsForCommit(commitId: ObjectId) = {
    def reactionToView(reaction: UserReaction, author: PartialUserDetails) = {
      reaction match {
        case comment: Comment => CommentView(comment.id.toString, author.name, author.id.toString, comment.message, comment.postingTime.toDate, author.avatarUrl)
        case like: Like => LikeView(reaction.id.toString, author.name, author.id.toString, reaction.postingTime.toDate)
      }
    }

    val comments = commitCommentDAO.findCommentsForCommits(commitId)
    val (inlineComments, entireComments) = comments.partition(c => c.fileName.isDefined && c.lineNumber.isDefined)

    val likes = likeDAO.findLikesForCommits(commitId)
    val (inlineLikes, entireLikes) = likes.partition(l => l.fileName.isDefined && l.lineNumber.isDefined)

    val inlineReactionsView = mapInlineReactionsToView(inlineComments ++ inlineLikes, reactionToView)
    val entireReactionsView = mapCommitReactionsToView(entireComments ++ entireLikes, reactionToView)

    CommitReactionsView(entireReactionsView, inlineReactionsView)
  }
}
