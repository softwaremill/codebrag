package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

abstract sealed class NewComment(val commitId: ObjectId, val authorId: ObjectId, val message: String)

case class NewEntireCommitComment(
                                   override val commitId: ObjectId,
                                   override val authorId: ObjectId,
                                   override val message: String) extends  NewComment(commitId, authorId, message)

case class NewInlineCommitComment(
                                   override val commitId: ObjectId,
                                   override val authorId: ObjectId,
                                   override val message: String,
                                   fileName: String,
                                   lineNumber: Int) extends  NewComment(commitId, authorId, message)


case class IncomingComment(commitId: ObjectId, authorId: ObjectId, message: String, fileName: Option[String], lineNumber: Option[Int])
