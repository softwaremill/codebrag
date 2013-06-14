package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

abstract sealed class UserReactionBase(val id: ObjectId, val commitId: ObjectId, val authorId: ObjectId, val postingTime: DateTime) {
  def threadId: ThreadDetails
}

abstract sealed class CommentBase(
                            override val id: ObjectId,
                            override val commitId: ObjectId,
                            override val authorId: ObjectId,
                            val message: String,
                            override val postingTime: DateTime) extends UserReactionBase(id: ObjectId, commitId: ObjectId, authorId: ObjectId, postingTime: DateTime) {

}

case class EntireCommitComment(
                                override val id: ObjectId,
                                override val commitId: ObjectId,
                                override val authorId: ObjectId,
                                override val message: String,
                                override val postingTime: DateTime) extends CommentBase(id, commitId, authorId, message, postingTime) {

  def threadId = ThreadDetails(commitId)

}

case class InlineCommitComment(
                                override val id: ObjectId,
                                override val commitId: ObjectId,
                                override val authorId: ObjectId,
                                override val message: String,
                                override val postingTime: DateTime,
                                fileName: String,
                                lineNumber: Int) extends CommentBase(id, commitId, authorId, message, postingTime) {

  def threadId = ThreadDetails(commitId, Some(lineNumber), Some(fileName))

}

case class ThreadDetails(commitId: ObjectId, lineNumber: Option[Int] = None, fileName: Option[String] = None)
