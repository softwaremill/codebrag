package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, Like, Comment}
import org.bson.types.ObjectId

trait CommitCommentDAO {

  def save(comment: Comment)

  def findCommentsForCommit(commitId: ObjectId): List[Comment]

  def findAllCommentsForThread(thread: ThreadDetails): List[Comment]

}