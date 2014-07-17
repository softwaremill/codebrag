package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain.UserAlias
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.sql.SQLDatabase

trait UserAliasDAO {

  def save(alias: UserAlias)

  def remove(aliasId: ObjectId)

  def findAllForUser(userId: ObjectId): Iterable[UserAlias]

}

