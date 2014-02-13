package com.softwaremill.codebrag.dao.user

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.domain.InternalUser
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.test.{FlatSpecWithSQL, FlatSpecWithMongo, ClearSQLDataAfterTest, ClearMongoDataAfterTest}

trait InternalUserDAOSpec extends FlatSpec with ShouldMatchers {

  def userDAO: UserDAO
  def internalUserDAO: InternalUserDAO
  def countInternalUsers(): Long

  it should "save internal user if one with the same name doesn't exist" in {
    // given
    val user = InternalUser("codebrag")

    // when
    internalUserDAO.createIfNotExists(user)

    // then
    val Some(found) = internalUserDAO.findByName(user.name)
    found.id should be(user.id)
    found.name should be(user.name)
  }

  it should "not save internal user if one with the same name already exists" in {
    // given
    val user = InternalUser("codebrag")
    internalUserDAO.createIfNotExists(user)

    // when
    val duplicate = InternalUser("codebrag")
    internalUserDAO.createIfNotExists(duplicate)

    // then
    countInternalUsers() should be(1)
    val Some(found) = internalUserDAO.findByName(user.name)
    found.id should be(user.id)
    found.name should be(user.name)
  }

  it should "not consider regular users in search" in {
    // given
    val regularUser = UserAssembler.randomUser.withFullName("codebrag").get
    userDAO.add(regularUser)

    // when
    val found = internalUserDAO.findByName("codebrag")

    // then
    found should be(None)
  }
}

class MongoInternalUserDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with InternalUserDAOSpec {
  val userDAO = new MongoUserDAO
  val internalUserDAO = new MongoInternalUserDAO

  def countInternalUsers() = InternalUserRecord.count
}

class SQLInternalUserDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with InternalUserDAOSpec {
  val userDAO = new SQLUserDAO(sqlDatabase)
  var internalUserDAO = new SQLInternalUserDAO(sqlDatabase)

  def countInternalUsers() = internalUserDAO.count()
}
