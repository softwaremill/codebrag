package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{UserComment, CommentBase, InlineCommitComment, EntireCommitComment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: UserComment)

  def findCommentsForCommit(commitId: ObjectId): List[UserComment]

  def findAllCommentsInThreadWith(comment: UserComment): List[UserComment]

}
