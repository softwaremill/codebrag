package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

trait CommentListFinder {

  @deprecated("Will be removed on inline comments finish")
  def findAllForCommit(commentId: ObjectId): CommentListDTO

  def commentsForCommit(commitId: ObjectId): CommentsView

}
