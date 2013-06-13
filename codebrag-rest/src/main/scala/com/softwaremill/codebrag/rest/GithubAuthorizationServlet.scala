package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.github.GitHubAuthService
import org.scalatra.{Forbidden, ScalatraServlet, SeeOther}
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.user.{Authenticator}
import com.softwaremill.codebrag.domain.{User, Authentication}
import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.auth.AuthenticationSupport
import com.softwaremill.codebrag.service.config.CodebragConfiguration
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.dao.events.NewUserRegistered


class GithubAuthorizationServlet(val authenticator: Authenticator, ghAuthService: GitHubAuthService, userDao: UserDAO, eventBus: EventBus)
  extends ScalatraServlet with AuthenticationSupport with Logging {

  private val TempUserLogin = "tmpLogin"
  private val TempUserPass = "tmpPassword"
  private val RedirectToUrlParam = "redirectTo"

  get("/authenticate") {
    request.getSession().put(RedirectToUrlParam, params.getOrElse(RedirectToUrlParam, "/commits"))
    val clientId = Option(CodebragConfiguration.githubClientId) getOrElse (throw new IllegalStateException("No GitHub Client Id found, check your application.conf"))
    SeeOther(s"https://github.com/login/oauth/authorize?client_id=$clientId&scope=user,repo")
  }

  get("/auth_callback") {
    val code = params.get("code").get
    logger.debug(s"Retrieved code $code")
    val accessToken = ghAuthService.getAccessToken(code)
    logger.debug(s"Retrieved access token $accessToken")
    val user = ghAuthService.loadUserData(accessToken)
    val auth: Authentication = Authentication.github(user.login, accessToken.access_token)
    userDao.findByEmail(user.email) match {
      case Some(u) => {
        logger.debug(s"Changing Authentication for user $u.id")
        userDao.changeAuthentication(u.id, auth)
      }
      case None => {
        logger.debug("Creating new user")
        val newUser = User(new ObjectId, auth, user.name, user.email, UUID.randomUUID().toString, user.avatarUrl)
        userDao.add(newUser)
        eventBus.publish(NewUserRegistered(newUser.id, newUser.authentication.usernameLowerCase, newUser.name, newUser.email))
      }
    }
    request.setAttribute(TempUserLogin, user.login)
    request.setAttribute(TempUserPass, accessToken.access_token)
    logger.debug("Authenticating")
    authenticate() match {
      case Some(u) => {
        logger.debug("Authentication done")
        val redirectTo = request.getSession().getOrElse(RedirectToUrlParam, "")
        val redirectPath: String = s"$contextPath/#$redirectTo"
        logger.debug(s"Redirect path: $redirectPath")
        request.getSession().removeAttribute(RedirectToUrlParam)
        SeeOther(redirectPath)
      }
      case None => {
        request.getSession().invalidate()
        Forbidden
      }
    }
  }

  override protected def login: String = {
    getKeyFromRequest(TempUserLogin)
  }

  private def getKeyFromRequest(key: String): String = {
    val data = request.getAttribute(key).asInstanceOf[String]
    logger.debug(s"Retrieved non-null data for $key")
    data
  }

  override protected def password: String = {
    getKeyFromRequest(TempUserPass)
  }

  override protected def rememberMe: Boolean = true
}



