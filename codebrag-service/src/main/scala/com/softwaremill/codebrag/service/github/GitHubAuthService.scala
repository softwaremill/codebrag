package com.softwaremill.codebrag.service.github

import dispatch._
import org.json4s.jackson.JsonMethods._
import org.eclipse.egit.github.core.client.GitHubClient
import org.json4s.DefaultFormats
import org.eclipse.egit.github.core.service.UserService

class GitHubAuthService {
  implicit val formats = DefaultFormats

  private val GitHubLogin = "https://github.com/login/oauth/access_token"

  def getAccessToken(code: String): AccessToken = {
    val svc = dispatch.url(GitHubLogin) << authData(code)
    svc.setHeader("Accept", "application/json").setHeader("Content-Type", "application/json")
    val response = Http(svc OK as.String)()
    parse(response).extract[AccessToken]
  }

  private def authData(code: String) = s"""{"client_id":"${CodebragGitHub.ClientId}", "client_secret":"${CodebragGitHub.Secret}", "code":"$code"}"""

  def loadUserData(accessToken: AccessToken) = {
    val client = new GitHubClient
    client.setOAuth2Token(accessToken.access_token)
    val userService = new UserService(client)
    val user = userService.getUser
    GitHubUser(user.getLogin, user.getName)
  }
}

case class AccessToken(access_token: String, token_type: String)

case class GitHubUser(login: String, name: String)

object CodebragGitHub {
  val ClientId = "5bd745ba65be4fdfaeee"
  val Secret = "fbb3f249e1e26805c7d7be2accc01d9b1de07db4"
}