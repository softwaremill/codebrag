package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

@deprecated("will be remove in favor of NewComment class hierarchy to support inline comments")
case class AddComment(commitId: ObjectId, authorId: ObjectId, message: String)



// WIP version supporting inline comments

abstract class NewComment(val commitId: ObjectId, val authorId: ObjectId, val message: String)

case class NewWholeCommitComment(override val commitId: ObjectId, override val authorId: ObjectId, override val message: String) extends  NewComment(commitId, authorId, message)

case class NewInlineComment(comment: NewWholeCommitComment, fileName: String, lineNumber: Int) extends NewComment(comment.commitId, comment.authorId, comment.message)
