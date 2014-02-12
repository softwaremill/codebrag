package com.softwaremill.codebrag.dao.finders.reaction

import com.softwaremill.codebrag.domain.UserReaction
import com.softwaremill.codebrag.dao.reporting.views.{ReactionsView, ReactionView}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserRecord
import com.foursquare.rogue.LiftRogue._

trait UserReactionToViewMapper {

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

  private def findAllUsersIn(reactions: List[UserReaction]): List[(ObjectId, String, Option[String])] = {
    UserRecord.select(_.id, _.name, _.userSettings.subfield(_.avatarUrl)).where(_.id in reactions.map(_.authorId)).fetch()
    .map {
      case (id, name, avatarOpt) => (id, name, avatarOpt)
    }
  }

  private def findUserDetails(commenters: List[(ObjectId, String, Option[String])], commenterId: ObjectId) = {
    commenters.find(_._1 == commenterId) match {
      case Some(author) => AuthorData(author._2, author._1.toString, author._3)
      case None => AuthorData("Unknown author", "")
    }
  }

  case class AuthorData(authorName: String, authorId: String, avatarUrl: Option[String] = None)

}
