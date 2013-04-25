package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

trait CommentListFinder {

  def commentsForCommit(commitId: ObjectId): CommentsView

}
