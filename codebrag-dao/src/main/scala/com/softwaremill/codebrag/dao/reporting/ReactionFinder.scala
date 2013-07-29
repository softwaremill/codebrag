package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{MongoLikeDAO, MongoCommitCommentDAO, UserRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.{Like, UserReaction, Comment}
import com.softwaremill.codebrag.dao.reporting.views._
import scala.Some

class ReactionFinder extends UserReactionToView {

  def findReactionsForCommit(commitId: ObjectId) = {

    def reactionToView(reaction: UserReaction, authorData: AuthorData) = {
      reaction match {
        case comment: Comment => CommentView(comment.id.toString, authorData.authorName, authorData.authorId, comment.message, comment.postingTime.toDate, authorData.avatarUrl)
        case like: Like => LikeView(reaction.id.toString, authorData.authorName, authorData.authorId)
      }
    }

    val comments = new MongoCommitCommentDAO().findCommentsForCommit(commitId)
    val (inlineComments, entireComments) = comments.partition(c => c.fileName.isDefined && c.lineNumber.isDefined)

    val likes = new MongoLikeDAO().findLikesForCommit(commitId)
    val (inlineLikes, entireLikes) = likes.partition(l => l.fileName.isDefined && l.lineNumber.isDefined)

    val inlineReactionsView = mapInlineReactionsToView(inlineComments ++ inlineLikes, reactionToView)
    val entireReactionsView = mapCommitReactionsToView(entireComments ++ entireLikes, reactionToView)

    CommitReactionsView(entireReactionsView, inlineReactionsView)
  }

}

trait UserReactionToView {

  def mapInlineReactionsToView(reactions: List[UserReaction], domainToView: (UserReaction, AuthorData) => ReactionView): Map[String, Map[String, ReactionsView]] = {
    val usersCached = findAllUsersIn(reactions)
    val byFiles = reactions.groupBy(_.fileName.get)
    val byFileAndLineNumber = byFiles.map({
      case (fileName, fileReactions) => (fileName, fileReactions.groupBy(_.lineNumber.get))
    })

    byFileAndLineNumber.map({
      case (fileName, reactionsForFile) =>
        (fileName, reactionsForFile.map({
          case (lineNumber, lineReactions) => {
            val lineReactionsViews = lineReactions.map(reaction => {
              val authorData = findUserDetails(usersCached, reaction.authorId)
              domainToView(reaction, authorData)
            })
            val reactionsByType = lineReactionsViews.groupBy(_.reactionType)
            (lineNumber.toString, ReactionsView(reactionsByType.get("comment"), reactionsByType.get("like")))
          }
        }))
    })
  }

  def mapCommitReactionsToView(reactions: List[UserReaction], domainToView: (UserReaction, AuthorData) => ReactionView): ReactionsView = {
    val usersCached = findAllUsersIn(reactions)
    val reactionsByType = reactions.map(reaction => {
      val authorData = findUserDetails(usersCached, reaction.authorId)
      domainToView(reaction, authorData)
    }).groupBy(_.reactionType)
    ReactionsView(reactionsByType.get("comment"), reactionsByType.get("like"))
  }

  private def findAllUsersIn(reactions: List[UserReaction]): List[(ObjectId, String, String)] = {
    UserRecord.select(_.id, _.name, _.avatarUrl).where(_.id in reactions.map(_.authorId)).fetch()
  }

  private def findUserDetails(commenters: List[(ObjectId, String, String)], commenterId: ObjectId) = {
    commenters.find(_._1 == commenterId) match {
      case Some(author) => AuthorData(author._2, author._1.toString, author._3)
      case None => AuthorData("Unknown author", "", "")
    }
  }

  case class AuthorData(authorName: String, authorId: String, avatarUrl: String)

}


