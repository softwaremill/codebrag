package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class InternalUser(id: ObjectId, name: String)

// `extends` to get `tupled`
object InternalUser extends ((ObjectId, String) => InternalUser) {

  val WelcomeFollowupsAuthorName = "Codebrag"

  def apply(name: String) = {
    new InternalUser(new ObjectId, name)
  }
}
