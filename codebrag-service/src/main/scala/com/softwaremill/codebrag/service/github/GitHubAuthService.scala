package com.softwaremill.codebrag.service.github

import dispatch._
import org.json4s.jackson.JsonMethods._
import org.eclipse.egit.github.core.client.GitHubClient
import org.json4s.DefaultFormats
import org.eclipse.egit.github.core.service.UserService
import org.eclipse.egit.github.core.User
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.service.config.CodebragConfiguration

class GitHubAuthService {
  implicit val formats = DefaultFormats

  private val GitHubLogin = "https://github.com/login/oauth/access_token"

  def getAccessToken(code: String): AccessToken = {
    val svc = dispatch.url(GitHubLogin) << authData(code)
    svc.setHeader("Accept", "application/json").setHeader("Content-Type", "application/json")
    val response = Http(svc OK as.String)()
    parse(response).extract[AccessToken]
  }

  private def authData(code: String) = {
    val clientId = Option(CodebragConfiguration.githubClientId) getOrElse (throw new IllegalStateException("No GitHub Client Id found, check your application.conf"))
    val clientSecret = Option(CodebragConfiguration.githubClientSecret) getOrElse (throw new IllegalStateException("No GitHub Client Secret found, check your application.conf"))
    s"""{"client_id":"$clientId", "client_secret":"$clientSecret", "code":"$code"}"""
  }

  def loadUserData(accessToken: AccessToken) = {
    val client = new GitHubClient().setOAuth2Token(accessToken.access_token)
    val userService = new UserService(client)
    val user = userService.getUser
    GitHubUser(user.getLogin, user.getName, readEmail(user, userService))
  }

  def readEmail(user: User, service: UserService) = {
    if (user.getEmail != null) {
      user.getEmail
    } else {
      service.getEmails.headOption getOrElse (throw new IllegalArgumentException("User doesn't have email address"))
    }
  }
}

case class AccessToken(access_token: String, token_type: String)

case class GitHubUser(login: String, name: String, email: String)