package com.softwaremill.codebrag.usecases

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO
import com.softwaremill.codebrag.domain.UserRepoDetails
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validateable

case class UpdateUserBrowsingContextForm(userId: ObjectId, repoName: String, branchName: String) extends Validateable {

  import com.softwaremill.scalaval.Validation.rule

  override def validate = {
    val repo = rule("repoName")(repoName.nonEmpty, "Repo name cannot be empty")
    val branch=  rule("branchName")(branchName.nonEmpty, "Branch name cannot be empty")
    com.softwaremill.scalaval.Validation.validate(repo, branch)
  }

}

class UpdateUserBrowsingContextUseCase(userRepoDetailsDao: UserRepoDetailsDAO)(implicit clock: Clock) extends Logging {

  def execute(form: UpdateUserBrowsingContextForm) = {
    form.validate.whenOk {
      val details = userRepoDetailsDao.find(form.userId, form.repoName) match {
        case Some(repoDetails) => repoDetails.copy(branchName = form.branchName, default = true)
        case None => UserRepoDetails(form.userId, form.repoName, form.branchName, clock.nowUtc, default = true)
      }
      userRepoDetailsDao.save(details)
    }
  }

}
