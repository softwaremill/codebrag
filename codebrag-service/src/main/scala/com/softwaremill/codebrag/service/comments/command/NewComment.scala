package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

abstract class NewComment(val commitId: ObjectId, val authorId: ObjectId, val message: String)

case class NewEntireCommitComment(override val commitId: ObjectId, override val authorId: ObjectId, override val message: String) extends  NewComment(commitId, authorId, message)

case class NewInlineCommitComment(comment: NewEntireCommitComment, fileName: String, lineNumber: Int) extends NewComment(comment.commitId, comment.authorId, comment.message)
