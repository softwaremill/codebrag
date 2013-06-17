package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

abstract sealed class UserReaction(val id: ObjectId, val commitId: ObjectId, val authorId: ObjectId, val postingTime: DateTime, fileName: Option[String] = None, lineNumber: Option[Int] = None) {

  def threadId = ThreadDetails(commitId, lineNumber, fileName)

}

case class Comment(
                      override val id: ObjectId,
                      override val commitId: ObjectId,
                      override val authorId: ObjectId,
                      override val postingTime: DateTime,
                      message: String,
                      fileName: Option[String] = None,
                      lineNumber: Option[Int] = None) extends UserReaction(id, commitId, authorId, postingTime, fileName, lineNumber) {
}

case class Like(
                    override val id: ObjectId,
                    override val commitId: ObjectId,
                    override val authorId: ObjectId,
                    override val postingTime: DateTime,
                    fileName: Option[String] = None,
                    lineNumber: Option[Int] = None) extends UserReaction(id, commitId, authorId, postingTime, fileName, lineNumber) {
}

case class ThreadDetails(commitId: ObjectId, lineNumber: Option[Int] = None, fileName: Option[String] = None)



