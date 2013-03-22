package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.github.GitHubAuthService
import org.scalatra.SeeOther
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator


class GithubAuthorizationServlet(val authenticator:Authenticator, ghAuthService:GitHubAuthService, userDao:UserDAO) extends JsonServletWithAuthentication {

  var tmpLogin:String = _
  var tmpPassword:String = _

  get("/auth_callback") {
    val code = params.get("code").get
    val accessToken = ghAuthService.getAccessToken(code)
    val user = ghAuthService.loadUserData(accessToken)
    userDao.findByEmail(user.email) match {
      case Some(u) =>
      case None =>
    }
    tmpLogin = user.login
    tmpPassword = accessToken.access_token
    authenticate()
    SeeOther("/")
  }

  override protected def login: String = tmpLogin

  override protected def password: String = tmpPassword
}



