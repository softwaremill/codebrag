package com.softwaremill.codebrag.rest.debug

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.swagger.{SwaggerSupport, Swagger}
import com.softwaremill.codebrag.rest.JsonServletWithAuthentication
import org.scalatra.json.JacksonJsonSupport
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import net.liftweb.mongodb.record.MongoMetaRecord
import com.softwaremill.codebrag.dao._
import scala.Some
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig}

class DebugServlet(val authenticator: Authenticator,
                   val swagger: Swagger,
                   importerFactory: GitHubCommitImportServiceFactory,
                   configuration: CodebragConfig with RepositoryConfig)
  extends JsonServletWithAuthentication with SwaggerSupport with JacksonJsonSupport {

  override protected val applicationName = Some(DebugServlet.MappingPath)
  override protected val applicationDescription = "Debugging services endpoint"

  get("/resetAll") {
    dropAllDataExceptUsers()
    triggerRepositoryUpdate()
    "Reset successfull."
  }

  def triggerRepositoryUpdate() {
    val importService = importerFactory.createInstance(configuration.codebragSyncUserLogin)
    importService.importRepoCommits(configuration.repositoryOwner, configuration.codebragSyncUserLogin)
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
