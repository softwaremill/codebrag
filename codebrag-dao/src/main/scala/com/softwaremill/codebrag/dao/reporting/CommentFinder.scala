package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.CommentsView

trait CommentFinder {

  def commentsForCommit(commitId: ObjectId): CommentsView

}
