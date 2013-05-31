package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

object UserJsonBuilder {

  def someUser() = {
    UserJson(new ObjectId().toString, "user", "User", "user@email.com", "123abc", "avatarUrl")
  }
}
