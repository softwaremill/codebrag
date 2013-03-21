package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.github.GitHubAuthService
import org.scalatra.SeeOther


class GithubAuthorizationServlet(ghAuthService: GitHubAuthService) extends JsonServlet {


  get("/auth_callback") {
    val code = params.get("code").get
    val accessToken = ghAuthService.getAccessToken(code)
    val user = ghAuthService.loadUserData(accessToken)
    SeeOther("/")
  }
}



