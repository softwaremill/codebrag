package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{CommentBase, InlineComment, CommitComment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: CommentBase)

  def findCommentsForEntireCommit(commitId: ObjectId): List[CommitComment]

  def findInlineCommentsForCommit(commitId: ObjectId): List[InlineComment]
}
