package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.github.GitHubAuthService
import org.scalatra.SeeOther
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.domain.{User, Authentication}
import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging


class GithubAuthorizationServlet(val authenticator:Authenticator, ghAuthService:GitHubAuthService, userDao:UserDAO) extends JsonServletWithAuthentication with Logging {

  var tmpLogin:String = _
  var tmpPassword:String = _

  get("/auth_callback") {
    val code = params.get("code").get
    val accessToken = ghAuthService.getAccessToken(code)
    val user = ghAuthService.loadUserData(accessToken)
    val auth: Authentication = Authentication.github(user.login, accessToken.access_token)
    userDao.findByEmail(user.email) match {
      case Some(u) => userDao.changeAuthentication(u.id, auth)
      case None => userDao.add(User(auth, user.name, user.email, UUID.randomUUID().toString))
    }
    tmpLogin = user.login
    tmpPassword = accessToken.access_token
    authenticate()
    val redirectPath: String = s"$contextPath/#/commits"
    logger.debug(s"Redirect path: $redirectPath")
    SeeOther(redirectPath)
  }

  override protected def login: String = tmpLogin

  override protected def password: String = tmpPassword

  override protected def rememberMe: Boolean = true
}



