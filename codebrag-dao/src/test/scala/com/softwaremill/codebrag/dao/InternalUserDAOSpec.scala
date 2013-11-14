package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.domain.InternalUser
import com.softwaremill.codebrag.domain.builder.UserAssembler

class MongoInternalUserDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with BeforeAndAfterEach {

  var internalUserDao: MongoInternalUserDAO = _

  override def beforeEach() {
    super.beforeEach()
    internalUserDao = new MongoInternalUserDAO
  }

  it should "save internal user if one with the same name doesn't exist" in {
    // given
    val user = InternalUser("codebrag")

    // when
    internalUserDao.createIfNotExists(user)

    // then
    val Some(found) = internalUserDao.findByName(user.name)
    found.id should be(user.id)
    found.name should be(user.name)
  }

  it should "not save internal user if one with the same name already exists" in {
    // given
    val user = InternalUser("codebrag")
    internalUserDao.createIfNotExists(user)

    // when
    val duplicate = InternalUser("codebrag")
    internalUserDao.createIfNotExists(duplicate)

    // then
    InternalUserRecord.count should be(1)
    val Some(found) = internalUserDao.findByName(user.name)
    found.id should be(user.id)
    found.name should be(user.name)
  }

  it should "not consider regular users in search" in {
    // given
    val regularUser = UserAssembler.randomUser.withFullName("codebrag").get
    (new MongoUserDAO).add(regularUser)

    // when
    val found = internalUserDao.findByName("codebrag")

    // then
    found should be(None)
  }
}
