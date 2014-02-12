package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.domain.{FollowupWithNoReactions, FollowupWithReactions}
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging

trait FollowupWithReactionsDAO extends Logging {

  def findById(followupId: ObjectId): Option[Either[FollowupWithNoReactions, FollowupWithReactions]]

  def findAllContainingReaction(reactionId: ObjectId): List[Either[FollowupWithNoReactions, FollowupWithReactions]]

  def update(followup: FollowupWithReactions)
}


