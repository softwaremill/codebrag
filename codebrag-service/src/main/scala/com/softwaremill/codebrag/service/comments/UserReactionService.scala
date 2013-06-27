package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO, LikeDAO, CommitCommentDAO}
import com.softwaremill.codebrag.domain.{CommitInfo, UserReaction, Like, Comment}
import pl.softwaremill.common.util.time.Clock
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.{IncomingUserReaction, IncomingLike, IncomingComment}
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.domain.reactions.CommitLiked
import com.typesafe.scalalogging.slf4j.Logging

class UserReactionService(commentDao: CommitCommentDAO, likeDao: LikeDAO, likeValidator: LikeValidator, eventBus: EventBus)(implicit clock: Clock) {

  def storeLike(like: IncomingLike): Either[String, Like] = {
    val likeDomainObject = Like(new ObjectId, like.commitId, like.authorId, clock.currentDateTimeUTC(), like.fileName, like.lineNumber)
    val valid = likeValidator.isLikeValid(likeDomainObject)
    valid.right.map(_ => {
      save(likeDomainObject)
      likeDomainObject
    })
  }

  def storeComment(comment: IncomingComment): Comment = {
    val commentDomainObject = Comment(new ObjectId, comment.commitId, comment.authorId, clock.currentDateTimeUTC(), comment.message, comment.fileName, comment.lineNumber)
    save(commentDomainObject)
    commentDomainObject
  }

  private def save(reaction: UserReaction) {
    reaction match {
      case comment: Comment => commentDao.save(comment)
      case like: Like => {
        likeDao.save(like)
        eventBus.publish(CommitLiked(like))
      }
    }
  }

}