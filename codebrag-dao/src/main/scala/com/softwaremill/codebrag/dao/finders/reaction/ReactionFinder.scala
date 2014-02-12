package com.softwaremill.codebrag.dao.finders.reaction

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitReactionsView, LikeView, NotificationCountersView}

trait ReactionFinder {

  def findReactionsForCommit(commitId: ObjectId): CommitReactionsView

  def findLikeById(likeId: ObjectId): Option[LikeView]

}
