package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

abstract class CommentBase(val id: ObjectId, val commitId: ObjectId, val authorId: ObjectId, val message: String, val postingTime: DateTime)

case class CommitComment(override val id: ObjectId, override val commitId: ObjectId, override val authorId: ObjectId, override val message: String, override val postingTime: DateTime)
  extends CommentBase(id, commitId, authorId, message, postingTime)

case class InlineComment(commitComment: CommitComment, fileName: String, lineNumber: Int)
  extends CommentBase(commitComment.id, commitComment.commitId, commitComment.authorId, commitComment.message, commitComment.postingTime) {

  require((fileName != null && lineNumber != null),  "Inline comments must have non-empty fileName and lineNumber fields")
}
