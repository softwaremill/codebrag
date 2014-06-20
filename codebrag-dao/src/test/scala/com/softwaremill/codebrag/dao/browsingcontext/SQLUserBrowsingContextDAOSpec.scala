package com.softwaremill.codebrag.dao.browsingcontext

import com.softwaremill.codebrag.test.FlatSpecWithSQL
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.UserBrowsingContext
import com.softwaremill.codebrag.domain.builder.UserAssembler

class SQLUserBrowsingContextDAOSpec extends FlatSpecWithSQL with ShouldMatchers {

  val contextDao = new SQLUserBrowsingContextDAO(sqlDatabase)

  val Bob = UserAssembler.randomUser.get
  val Alice = UserAssembler.randomUser.get

  it should "save context when one doesn't exist for user and repo" in {
    // given
    val codebragContext = UserBrowsingContext(Bob.id, "codebrag", "master")
    val bootzookaDefaultContext = UserBrowsingContext(Bob.id, "bootzooka", "feature", default = true)

    // when
    contextDao.save(codebragContext)
    contextDao.save(bootzookaDefaultContext)

    // then
    val Some(codebrag) = contextDao.find(Bob.id, "codebrag")
    val Some(bootzooka) = contextDao.find(Bob.id, "bootzooka")
    codebrag should be(codebragContext)
    bootzooka should be(bootzookaDefaultContext)
  }

  it should "make new context default when saved with default = true" in {
    // given
    val defaultContext = UserBrowsingContext(Bob.id, "bootzooka", "feature", default = true)
    contextDao.save(defaultContext)

    // when
    val newDefaultContext = UserBrowsingContext(Bob.id, "codebrag", "master", default = true)
    contextDao.save(newDefaultContext)

    // then
    val Some(oldDefault) = contextDao.find(Bob.id, "bootzooka")
    val Some(newDefault) = contextDao.find(Bob.id, "codebrag")
    oldDefault.default should be(false)
    newDefault.default should be(true)
  }

  it should "find default context for user" in {
    // given
    val nonDefaultContext = UserBrowsingContext(Bob.id, "codebrag", "bugfix")
    contextDao.save(nonDefaultContext)
    val defaultContext = UserBrowsingContext(Bob.id, "bootzooka", "feature", default = true)
    contextDao.save(defaultContext)

    // when
    val Some(result) = contextDao.findDefault(Bob.id)

    // then
    result should be(defaultContext)
  }

  it should "save separate contexts for different user" in {
    // given
    val bobContext = UserBrowsingContext(Bob.id, "bootzooka", "feature", default = true)
    val aliceContext = UserBrowsingContext(Alice.id, "bootzooka", "master", default = true)
    contextDao.save(bobContext)
    contextDao.save(aliceContext)

    // when
    val Some(bobResult) = contextDao.findDefault(Bob.id)
    val Some(aliceResult) = contextDao.findDefault(Alice.id)

    // then
    bobResult should be(bobContext)
    aliceResult should be(aliceContext)
  }

  it should "update context for user when context exists" in {
    // given
    val context = UserBrowsingContext(Bob.id, "bootzooka", "feature", default = true)
    contextDao.save(context)

    // when
    val updatedContext = context.copy(branchName = "bugfix")
    contextDao.save(updatedContext)

    // then
    val Some(result) = contextDao.find(Bob.id, "bootzooka")
    result should be(updatedContext)
  }

}
