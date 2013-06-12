package com.softwaremill.codebrag.domain.builder

import com.softwaremill.codebrag.domain.{Authentication, User}
import org.bson.types.ObjectId

class UserAssembler(var user: User) {

  def withFullName(name: String) = {
    user = user.copy(name = name)
    this
  }

  def get = user
}

object UserAssembler {
  def randomUser = new UserAssembler(createRandomUser())

  private def createRandomUser() = {
    User(new ObjectId, Authentication("Basic", "Sofokles", "sofokles", "token", "salt"), "Sofokles Mill", "sofo@sml.com", "token", "http://avatar.com/1.jpg")
  }
}
