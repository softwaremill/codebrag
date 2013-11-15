package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class InternalUser(id: ObjectId, name: String)

object InternalUser {

  val WelcomeFollowupsAuthorName = "Codebrag"

  def apply(name: String) = {
    new InternalUser(new ObjectId, name)
  }

}
