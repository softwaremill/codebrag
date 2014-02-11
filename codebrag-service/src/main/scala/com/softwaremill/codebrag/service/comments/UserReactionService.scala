package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{LikeDAO}
import com.softwaremill.codebrag.domain.{UserReaction, Like, Comment}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.{IncomingLike, IncomingComment}
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.domain.reactions.{UnlikeEvent, LikeEvent}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.reaction.CommitCommentDAO

class UserReactionService(commentDao: CommitCommentDAO,
                          likeDao: LikeDAO,
                          likeValidator: LikeValidator,
                          eventBus: EventBus)(implicit clock: Clock) extends Logging {

  def storeLike(like: IncomingLike): Either[String, Like] = {
    val likeDomainObject = Like(new ObjectId, like.commitId, like.authorId, clock.nowUtc, like.fileName, like.lineNumber)
    val valid = likeValidator.isLikeValid(likeDomainObject)
    valid.right.map(_ => {
      save(likeDomainObject)
      likeDomainObject
    })
  }

  def storeComment(comment: IncomingComment): Comment = {
    val commentDomainObject = Comment(new ObjectId, comment.commitId, comment.authorId, clock.nowUtc, comment.message, comment.fileName, comment.lineNumber)
    save(commentDomainObject)
    commentDomainObject
  }

  def removeLike(likeId: ObjectId) = {
    likeDao.findById(likeId) match {
      case Some(like) => {
        eventBus.publish(UnlikeEvent(like))
        likeDao.remove(like.id)
      }
      case None => logger.warn(s"Like [$likeId] already removed!")
    }
  }

  private def save(reaction: UserReaction) {
    reaction match {
      case comment: Comment => commentDao.save(comment)
      case like: Like => {
        likeDao.save(like)
        eventBus.publish(LikeEvent(like))
      }
    }
  }

}