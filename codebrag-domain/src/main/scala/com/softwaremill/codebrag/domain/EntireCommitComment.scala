package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

abstract sealed class CommentBase(val id: ObjectId, val commitId: ObjectId, val authorId: ObjectId, val message: String, val postingTime: DateTime) {

  def thread: CommentsThreadId
}

case class CommentsThreadId(commitId: ObjectId, lineNumber: Option[Int] = None, fileName: Option[String] = None)

case class EntireCommitComment(
                                override val id: ObjectId,
                                override val commitId: ObjectId,
                                override val authorId: ObjectId,
                                override val message: String,
                                override val postingTime: DateTime) extends CommentBase(id, commitId, authorId, message, postingTime) {

  def thread = CommentsThreadId(commitId)

}

case class InlineCommitComment(
                                override val id: ObjectId,
                                override val commitId: ObjectId,
                                override val authorId: ObjectId,
                                override val message: String,
                                override val postingTime: DateTime,
                                fileName: String,
                                lineNumber: Int) extends CommentBase(id, commitId, authorId, message, postingTime) {

  def thread = CommentsThreadId(commitId, Some(lineNumber), Some(fileName))

}
