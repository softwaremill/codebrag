package com.softwaremill.codebrag.service.config

import com.typesafe.config.{ConfigValueType, Config}
import com.softwaremill.codebrag.common.config.ConfigWithDefault

case class PossibleRepoCredentials(repoName: String, userName: Option[String], password: Option[String], passphrase: Option[String])

trait MultiRepoConfig extends ConfigWithDefault {

  def rootConfig: Config

  // directory containing cloned repo dir
  val repositoriesRoot = getString("codebrag.repos-root", "./repos")

  lazy val repositoriesConfig = {
    if(rootConfig.hasPath(rootRepoPath)) {
      import scala.collection.JavaConversions._
      rootConfig.getObject(rootRepoPath)
        .filter({ case(repoName, config) => config.valueType() == ConfigValueType.OBJECT })
        .map({ case(repoName, config) =>
        config.valueType()
        println(repoName)
        println(config)
        val opt = getOptional(s"$rootRepoPath.$repoName")_
        (repoName, PossibleRepoCredentials(repoName, opt("username"), opt("password"), opt("passphrase")))
      })
    } else {
      Map.empty[String, PossibleRepoCredentials]
    }
  }

  lazy val globalConfig = {
    val opt = getOptional(rootRepoPath)_
    PossibleRepoCredentials("*", opt("username"), opt("password"), opt("repositories.passphrase"))
  }

  private def getOptional(prefix: String)(field: String) = {
    getOptionalString(s"$prefix.$field")
  }

  private lazy val rootRepoPath = if(rootConfig.hasPath("repositories")) "repositories" else "repository"

}