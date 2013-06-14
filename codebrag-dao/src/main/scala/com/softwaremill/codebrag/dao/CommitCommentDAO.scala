package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{UserComment, CommentBase, InlineCommitComment, EntireCommitComment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: CommentBase)

  def findCommentsForEntireCommit(commitId: ObjectId): List[EntireCommitComment]

  def findInlineCommentsForCommit(commitId: ObjectId): List[InlineCommitComment]

  def findAllCommentsInThreadWith(comment: CommentBase): List[CommentBase]

  def save(comment: UserComment)

  def findCommentsForCommit(commitId: ObjectId): List[UserComment]
}
