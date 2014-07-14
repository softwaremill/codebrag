package com.softwaremill.codebrag.dao.finders.reaction

import com.softwaremill.codebrag.domain.{PartialUserDetails, UserReaction}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.finders.views.{ReactionsView, ReactionView}

trait UserReactionToViewMapper {

  protected def userDAO: UserDAO

  protected def mapInlineReactionsToView(reactions: List[UserReaction], domainToView: (UserReaction, PartialUserDetails) => ReactionView): Map[String, Map[String, ReactionsView]] = {
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

  protected def mapCommitReactionsToView(reactions: List[UserReaction], domainToView: (UserReaction, PartialUserDetails) => ReactionView): ReactionsView = {
    val usersCached = findAllUsersIn(reactions)
    val reactionsByType = reactions.map(reaction => {
      val authorData = findUserDetails(usersCached, reaction.authorId)
      domainToView(reaction, authorData)
    }).groupBy(_.reactionType)
    ReactionsView(reactionsByType.get("comment"), reactionsByType.get("like"))
  }

  private def findAllUsersIn(reactions: List[UserReaction]) = {
    userDAO.findPartialUserDetails(reactions.map(_.authorId))
  }

  private def findUserDetails(commenters: Iterable[PartialUserDetails], commenterId: ObjectId) = {
    commenters.find(_.id == commenterId) match {
      case Some(author) => author
      case None => PartialUserDetails(new ObjectId(), "Unknown author", "", "")
    }
  }
}
