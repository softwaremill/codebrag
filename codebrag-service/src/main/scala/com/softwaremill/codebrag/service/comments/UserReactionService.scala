package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{LikeDAO, CommitCommentDAO}
import com.softwaremill.codebrag.domain.{UserReaction, Like, Comment}
import pl.softwaremill.common.util.time.Clock
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.{IncomingUserReaction, IncomingLike, IncomingComment}

class UserReactionService(commentDao: CommitCommentDAO, likeDao: LikeDAO)(implicit clock: Clock) {

  def storeUserReaction(reaction: IncomingUserReaction): UserReaction = {
    val reactionDomainObject = toDomainObject(reaction)
    save(reactionDomainObject)
    reactionDomainObject
  }

  private def toDomainObject(reaction: IncomingUserReaction): UserReaction with Product with Serializable = {
    reaction match {
      case r: IncomingComment => Comment(new ObjectId, r.commitId, r.authorId, clock.currentDateTimeUTC(), r.message, r.fileName, r.lineNumber)
      case r: IncomingLike => Like(new ObjectId, r.commitId, r.authorId, clock.currentDateTimeUTC(), r.fileName, r.lineNumber)
    }
  }

  private def save(reaction: UserReaction) {
    reaction match {
      case comment: Comment => commentDao.save(comment)
      case like: Like => likeDao.save(like)
    }
  }

}