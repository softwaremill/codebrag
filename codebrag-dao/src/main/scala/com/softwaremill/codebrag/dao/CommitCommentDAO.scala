package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{InlineComment, CommitComment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: CommitComment)

  def save(inlineComment: InlineComment)

  def findCommentsForEntireCommit(commitId: ObjectId): List[CommitComment]

  def findInlineCommentsForCommit(commitId: ObjectId): List[InlineComment]
}
