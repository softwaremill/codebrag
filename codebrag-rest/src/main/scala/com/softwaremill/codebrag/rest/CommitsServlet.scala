package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.data.UserJson
import swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.dao.CommitInfoDAO

class CommitService

class CommitsServlet(val authenticator: Authenticator, val commitInfoDao: CommitInfoDAO, val swagger: Swagger) extends JsonServletWithAuthentication with CommitsServletSwaggerDefinition {


  get("/") { // for all /commits/*
    halt(404)
  }


  get("/") { // for /commits?type=* only
    haltIfNotAuthenticated
    params.getOrElse("type", pass())
  }

}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}


trait CommitsServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  protected val applicationDescription: String = "Commits information endpoint"

}
