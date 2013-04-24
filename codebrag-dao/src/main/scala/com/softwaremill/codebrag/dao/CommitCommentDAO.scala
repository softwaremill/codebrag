package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{CommentBase, InlineCommitComment, EntireCommitComment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: CommentBase)

  def findCommentsForEntireCommit(commitId: ObjectId): List[EntireCommitComment]

  def findInlineCommentsForCommit(commitId: ObjectId): List[InlineCommitComment]

  def findAllCommentsInThreadWith(comment: CommentBase): List[CommentBase]

}
