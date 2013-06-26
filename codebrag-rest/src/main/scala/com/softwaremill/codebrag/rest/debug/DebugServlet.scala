package com.softwaremill.codebrag.rest.debug

import com.softwaremill.codebrag.rest.JsonServlet
import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import net.liftweb.mongodb.record.MongoMetaRecord
import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.foursquare.rogue.LiftRogue._

class DebugServlet(repoDataProducer: RepoDataProducer,
                   commitImportService: CommitImportService,
                   configuration: CodebragConfig)
  extends JsonServlet with DebugBasicAuthSupport {

  override def login = configuration.debugServicesLogin
  override def password = configuration.debugServicesPassword

  get("/resetAll") {
    basicAuth()
    dropAllDataExceptInitialUsers()
    triggerRepositoryUpdate()
    "Reset successfull."
  }

  def triggerRepositoryUpdate() {
    repoDataProducer.createFromConfiguration().foreach(commitImportService.importRepoCommits(_))
  }

  def dropAllDataExceptInitialUsers() {

    val list: List[MongoMetaRecord[_]] = List(
      CommitInfoRecord,
      CommitReviewTaskRecord,
      FollowupRecord,
      CommentRecord,
      LikeRecord
    )
    list.foreach(_.drop)
    deleteUsersExcludingInitial()
  }

  def deleteUsersExcludingInitial() {
    UserRecord.where(_.authentication.subfield(_.provider) eqs "GitHub").
               and(_.authentication.subfield(_.usernameLowerCase) neqs "codebrag").
               bulkDelete_!!!
  }
}

object DebugServlet {
  val MappingPath = "debug"
}
