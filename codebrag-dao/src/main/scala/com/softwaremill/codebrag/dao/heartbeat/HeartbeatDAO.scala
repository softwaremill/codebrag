package com.softwaremill.codebrag.dao.heartbeat

import org.bson.types.ObjectId
import org.joda.time.DateTime

trait HeartbeatDAO {
   def update(userId: ObjectId)

   def get(userId: ObjectId): Option[DateTime]

   def loadAll(): List[(ObjectId, DateTime)]
 }
