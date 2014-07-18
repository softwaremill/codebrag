package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain.UserAlias
import org.bson.types.ObjectId

trait UserAliasDAO {

  def save(alias: UserAlias)

  def remove(aliasId: ObjectId)

  def findAllForUser(userId: ObjectId): Iterable[UserAlias]

  def findByAlias(alias: String): Option[UserAlias]

  def findById(aliasId: ObjectId): Option[UserAlias]

}

