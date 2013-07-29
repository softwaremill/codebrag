package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{LikeView, NotificationCountersView}

trait ReactionFinder {

  def findReactionsForCommit(commitId: ObjectId)

  def findLikeById(likeId: ObjectId): Option[LikeView]

}
