package com.softwaremill.codebrag.dao.finders.reaction

import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.{Like, UserReaction, Comment}
import com.softwaremill.codebrag.dao.reporting.views._
import scala.Some
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.{LikeRecord, UserRecord}
import com.softwaremill.codebrag.dao.reaction.{MongoLikeDAO, MongoCommitCommentDAO}

class MongoReactionFinder extends ReactionFinder with UserReactionToViewMapper with Logging {

  def findReactionsForCommit(commitId: ObjectId) = {

    def reactionToView(reaction: UserReaction, authorData: AuthorData) = {
      reaction match {
        case comment: Comment => CommentView(comment.id.toString, authorData.authorName, authorData.authorId, comment.message, comment.postingTime.toDate, authorData.avatarUrl)
        case like: Like => LikeView(reaction.id.toString, authorData.authorName, authorData.authorId, reaction.postingTime.toDate)
      }
    }

    val comments = new MongoCommitCommentDAO().findCommentsForCommits(commitId)
    val (inlineComments, entireComments) = comments.partition(c => c.fileName.isDefined && c.lineNumber.isDefined)

    val likes = new MongoLikeDAO().findLikesForCommits(commitId)
    val (inlineLikes, entireLikes) = likes.partition(l => l.fileName.isDefined && l.lineNumber.isDefined)

    val inlineReactionsView = mapInlineReactionsToView(inlineComments ++ inlineLikes, reactionToView)
    val entireReactionsView = mapCommitReactionsToView(entireComments ++ entireLikes, reactionToView)

    CommitReactionsView(entireReactionsView, inlineReactionsView)
  }

  def findLikeById(likeId: ObjectId): Option[LikeView] = {
    LikeRecord.where(_.id eqs likeId).get() match {
      case Some(like) => {
        val author = UserRecord.where(_.id eqs like.authorId.get).get()
        val likeView = if(author.isEmpty) {
          logger.warn(s"Cannot find author with Id ${like.authorId.get} for like ${likeId}")
          LikeView(like.id.get.toString, "", like.authorId.get.toString, like.date.get, like.fileName.get, like.lineNumber.get)
        } else {
          LikeView(like.id.get.toString, author.get.name.get, like.authorId.get.toString, like.date.get, like.fileName.get, like.lineNumber.get)
        }
        Some(likeView)
      }
      case None => None
    }
  }

}




