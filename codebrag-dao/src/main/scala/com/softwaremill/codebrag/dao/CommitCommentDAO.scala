package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitComment
import org.bson.types.ObjectId

trait CommitCommentDAO {
  def save(comment: CommitComment)

  def findAllForCommit(commitId: ObjectId): List[CommitComment]
}
