package com.softwaremill.codebrag.rest

import org.scalatra.{Forbidden, ScalatraServlet, SeeOther}
import com.softwaremill.codebrag.service.user.{NewUserAdder, GitHubAuthService, Authenticator}
import com.softwaremill.codebrag.domain.{User, Authentication}
import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.auth.AuthenticationSupport
import com.softwaremill.codebrag.service.config.{CodebragConfig, GithubConfig}
import com.softwaremill.codebrag.dao.user.UserDAO
import org.bson.types.ObjectId


class GithubAuthorizationServlet(val authenticator: Authenticator,
                           ghAuthService: GitHubAuthService,
                           userDao: UserDAO,
                           newUserAdder: NewUserAdder,
                           config: GithubConfig with CodebragConfig)
  extends ScalatraServlet with AuthenticationSupport with Logging with CodebragErrorHandler {

  private val TempUserLogin = "tmpLogin"
  private val TempUserPass = "tmpPassword"
  private val RedirectToUrlParam = "redirectTo"

  get("/authenticate") {
    request.getSession.put(RedirectToUrlParam, params.getOrElse(RedirectToUrlParam, "/commits"))
    val clientId = Option(config.githubClientId) getOrElse (throw new IllegalStateException("No GitHub Client Id found, check your application.conf"))
    SeeOther(s"https://github.com/login/oauth/authorize?client_id=$clientId")
  }

  def stopIfDeniedOnGithub {
    if (params.get("code").isEmpty) {
      invalidateAndRedirect
    }
  }

  private def invalidateAndRedirect {
    request.getSession.invalidate()
    redirect(contextPath)
  }

  get("/auth_callback") {
    stopIfDeniedOnGithub
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
        val newUser = User(new ObjectId, auth, user.name, user.email, UUID.randomUUID().toString)
        newUserAdder.add(newUser)
      }
    }
    request.setAttribute(TempUserLogin, user.login)
    request.setAttribute(TempUserPass, accessToken.access_token)
    logger.debug("Authenticating")
    authenticate() match {
      case Some(u) => {
        logger.debug("Authentication done")
        val redirectTo = request.getSession.getOrElse(RedirectToUrlParam, "") match {
          case "/" => "/commits"
          case other => other
        }
        val redirectPath: String = s"$contextPath/#$redirectTo"
        logger.debug(s"Redirect path: $redirectPath")
        request.getSession.removeAttribute(RedirectToUrlParam)
        SeeOther(redirectPath)
      }
      case None => {
        request.getSession.invalidate()
        Forbidden
      }
    }
  }

  override protected def login: String = {
    getKeyFromRequest(TempUserLogin)
  }

  private def getKeyFromRequest(key: String): String = {
    request.getAttribute(key).asInstanceOf[String]
  }

  override protected def password: String = {
    getKeyFromRequest(TempUserPass)
  }

  override protected def rememberMe: Boolean = true
}



