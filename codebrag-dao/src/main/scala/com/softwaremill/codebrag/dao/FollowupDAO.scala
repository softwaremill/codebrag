package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Followup
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait FollowupDAO {

  def findById(followupId: ObjectId): Option[Followup]

  def createOrUpdateExisting(followup: Followup): ObjectId

  def delete(followupId: ObjectId)

  def countSince(date: DateTime, userId: ObjectId): Long

}
