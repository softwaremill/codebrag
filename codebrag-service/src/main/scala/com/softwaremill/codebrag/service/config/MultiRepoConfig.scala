package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.common.config.ConfigWithDefault

case class RepoCredentials(repoName: String, userName: Option[String], password: Option[String], passphrase: Option[String])

trait MultiRepoConfig extends ConfigWithDefault {

  def rootConfig: Config

  // directory containing cloned repo dir
  val repositoriesRoot = getString("codebrag.repos-root", "./repos")

  lazy val repositoriesConfig = {
    import scala.collection.JavaConversions._
    rootConfig.getObject("repositories").map({ case(repoName, config) =>
      val opt = getOptional(repoName)_
      (repoName, RepoCredentials(repoName, opt("username"), opt("password"), opt("passphrase")))
    })
  }


  // brigde section to seamlessly use new config
  lazy val username = singleRepoConfig.userName
  lazy val password = singleRepoConfig.password
  lazy val passphrase = singleRepoConfig.passphrase

  private lazy val singleRepoConfig = {
    if(repositoriesConfig.size > 1) throw new IllegalStateException("More than one repo configured. Hold your horses, sir!")
    repositoriesConfig.headOption.map(_._2).getOrElse(RepoCredentials("temp", None, None, None))
  }

  private def getOptional(repoName: String)(field: String) = getOptionalString(s"repos.$repoName.$field")

}