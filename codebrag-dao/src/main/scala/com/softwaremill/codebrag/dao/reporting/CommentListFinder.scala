package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

trait CommentListFinder {
  def findAllForCommit(commentId: ObjectId): CommentListDTO
}
