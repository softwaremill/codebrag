package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.builder.UserAssembler

object UserJsonBuilder {

  val someUser = UserJson(UserAssembler.randomUser.withBasicAuth("user", "pass").withFullName("User").get)

}
