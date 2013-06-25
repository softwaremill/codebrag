package com.softwaremill.codebrag.rest.debug

import com.softwaremill.codebrag.rest.JsonServlet
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import net.liftweb.mongodb.record.MongoMetaRecord
import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig}

class DebugServlet(importerFactory: GitHubCommitImportServiceFactory,
                   configuration: CodebragConfig with RepositoryConfig)
  extends JsonServlet with DebugBasicAuthSupport {

  override def login = configuration.debugServicesLogin
  override def password = configuration.debugServicesPassword

  get("/resetAll") {
    basicAuth()
    dropAllDataExceptUsers()
    triggerRepositoryUpdate()
    "Reset successfull."
  }

  def triggerRepositoryUpdate() {
    val importService = importerFactory.createInstance(configuration.codebragSyncUserLogin)
    importService.importRepoCommits(configuration.repositoryOwner, configuration.repositoryName)
  }

  def dropAllDataExceptUsers() {

    val list: List[MongoMetaRecord[_]] = List(
      CommitInfoRecord,
      CommitReviewTaskRecord,
      FollowupRecord,
      CommentRecord,
      LikeRecord
    )
    list.foreach(_.drop)
  }

}

object DebugServlet {
  val MappingPath = "debug"
}
