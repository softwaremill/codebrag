package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import org.bson.types.ObjectId

trait TeamDAO {
  def add(team: Team)

  def findAll(): List[Team]

  def findById(teamId: ObjectId): Option[Team]

  def findByName(name: String): Option[Team]
  
  def findByUser(userId: ObjectId): List[Team]
  
  def modifyTeam(team: Team)

  def delete(teamId: ObjectId)
  
  def countAll(): Long
}
