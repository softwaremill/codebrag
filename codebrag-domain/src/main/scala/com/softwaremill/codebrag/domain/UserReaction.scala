package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId
import scala.Enumeration

abstract sealed class UserReaction(val id: ObjectId, val commitId: ObjectId, val authorId: ObjectId, val postingTime: DateTime, val fileName: Option[String] = None, val lineNumber: Option[Int] = None) {

  def threadId = ThreadDetails(commitId, lineNumber, fileName)

  def reactionType: UserReactionTypeEnum.Value

}

object UserReactionTypeEnum extends Enumeration {
  type UserReactionTypeEnum = Value
  val Like = Value("Like")
  val Comment = Value("Comment")
}

case class Comment(
                      override val id: ObjectId,
                      override val commitId: ObjectId,
                      override val authorId: ObjectId,
                      override val postingTime: DateTime,
                      message: String,
                      override val fileName: Option[String] = None,
                      override val lineNumber: Option[Int] = None) extends UserReaction(id, commitId, authorId, postingTime, fileName, lineNumber) {

  override val reactionType = UserReactionTypeEnum.Comment

}

case class Like(
                    override val id: ObjectId,
                    override val commitId: ObjectId,
                    override val authorId: ObjectId,
                    override val postingTime: DateTime,
                    override val fileName: Option[String] = None,
                    override val lineNumber: Option[Int] = None) extends UserReaction(id, commitId, authorId, postingTime, fileName, lineNumber) {

  override val reactionType = UserReactionTypeEnum.Like

}

case class ThreadDetails(commitId: ObjectId, lineNumber: Option[Int] = None, fileName: Option[String] = None)

object ThreadDetails {

  def inline(commitId: ObjectId, lineNumber: Int, fileName: String) = {
    new ThreadDetails(commitId, Some(lineNumber), Some(fileName))
  }
}



