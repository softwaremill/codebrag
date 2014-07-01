package com.softwaremill.codebrag.usecases

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO
import com.softwaremill.codebrag.domain.UserRepoDetails
import com.softwaremill.codebrag.common.Clock

case class UpdateUserBrowsingContextForm(userId: ObjectId, repoName: String, branchName: String)

class UpdateUserBrowsingContextUseCase(userRepoDetailsDao: UserRepoDetailsDAO)(implicit clock: Clock) extends Logging {

  def execute(form: UpdateUserBrowsingContextForm) = {
    val details = userRepoDetailsDao.find(form.userId, form.repoName) match {
      case Some(repoDetails) => repoDetails.copy(branchName = form.branchName, default = true)
      case None => UserRepoDetails(form.userId, form.repoName, form.branchName, clock.nowUtc, default = true)
    }
    userRepoDetailsDao.save(details)
  }

}
