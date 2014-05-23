package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserSettings
import com.softwaremill.codebrag.domain.builder.UserAssembler

object UserJsonBuilder {

  def someUser = UserJson(UserAssembler.randomUser.withBasicAuth("user", "pass").withFullName("User").get)

}
