package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import org.scalatra
import com.softwaremill.codebrag.usecases.branches.{RemoveBranchFromObserved, NewObservedBranch, AddBranchToObserved}

class UserObservedBranchesEndpoint(
  val authenticator: Authenticator, 
  addBranchToObserved: AddBranchToObserved,
  removeBranchFromObserved: RemoveBranchFromObserved) extends JsonFilterWithAuthentication {

  post("/:userId/observed-branches") {
    haltIfNotCurrentUser(userIdParam)
    val form = NewObservedBranch(extractReq[String]("repoName"), extractReq[String]("branchName"))
    addBranchToObserved.execute(user.id, form) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(observedBranch) => observedBranch
    }
  }

  delete("/:userId/observed-branches/:observedId") {
    haltIfNotCurrentUser(userIdParam)
    val branchId = new ObjectId(extractReqUrlParam("observedId"))
    removeBranchFromObserved.execute(user.id, branchId) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(_) => scalatra.Ok()
    }
  }

  private def userIdParam = new ObjectId(params("userId"))

}

object UserObservedBranchesEndpoint {
  val MappingPath = "users"
}