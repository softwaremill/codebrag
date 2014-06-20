package com.softwaremill.codebrag.usecases

import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.browsingcontext.UserBrowsingContextDAO
import com.softwaremill.codebrag.domain.UserBrowsingContext

case class UpdateUserBrowsingContextForm(userId: ObjectId, repoName: String, branchName: String) {
  def toUserBrowsingContext = UserBrowsingContext(userId, repoName, branchName, default = true)
}

class UpdateUserBrowsingContextUseCase(userBrowsingContextDao: UserBrowsingContextDAO) extends Logging {

  def execute(form: UpdateUserBrowsingContextForm) = {
    val context = form.toUserBrowsingContext
    logger.debug(s"Update user browsing context: $context")
    userBrowsingContextDao.save(context)
  }

}
