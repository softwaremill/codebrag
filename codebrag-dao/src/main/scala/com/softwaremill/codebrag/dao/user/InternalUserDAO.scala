package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain.InternalUser

trait InternalUserDAO {
  def createIfNotExists(internalUser: InternalUser): InternalUser
  def findByName(internalUserName: String): Option[InternalUser]
}
