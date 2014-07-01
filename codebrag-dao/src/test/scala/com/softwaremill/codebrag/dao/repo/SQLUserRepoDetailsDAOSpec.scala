package com.softwaremill.codebrag.dao.repo

import com.softwaremill.codebrag.test.FlatSpecWithSQL
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.UserRepoDetails
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.ClockSpec

class SQLUserRepoDetailsDAOSpec extends FlatSpecWithSQL with ShouldMatchers with ClockSpec {

  val contextDao = new SQLUserRepoDetailsDAO(sqlDatabase)

  val Bob = UserAssembler.randomUser.get
  val Alice = UserAssembler.randomUser.get

  it should "save context when one doesn't exist for user and repo" in {
    // given
    val codebragContext = UserRepoDetails(Bob.id, "codebrag", "master", clock.nowUtc)
    val bootzookaDefaultContext = UserRepoDetails(Bob.id, "bootzooka", "feature", clock.nowUtc, default = true)

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
    val defaultContext = UserRepoDetails(Bob.id, "bootzooka", "feature", clock.nowUtc, default = true)
    contextDao.save(defaultContext)

    // when
    val newDefaultContext = UserRepoDetails(Bob.id, "codebrag", "master", clock.nowUtc, default = true)
    contextDao.save(newDefaultContext)

    // then
    val Some(oldDefault) = contextDao.find(Bob.id, "bootzooka")
    val Some(newDefault) = contextDao.find(Bob.id, "codebrag")
    oldDefault.default should be(false)
    newDefault.default should be(true)
  }

  it should "find default context for user" in {
    // given
    val nonDefaultContext = UserRepoDetails(Bob.id, "codebrag", "bugfix", clock.nowUtc)
    contextDao.save(nonDefaultContext)
    val defaultContext = UserRepoDetails(Bob.id, "bootzooka", "feature", clock.nowUtc, default = true)
    contextDao.save(defaultContext)

    // when
    val Some(result) = contextDao.findDefault(Bob.id)

    // then
    result should be(defaultContext)
  }

  it should "save separate contexts for different user" in {
    // given
    val bobContext = UserRepoDetails(Bob.id, "bootzooka", "feature", clock.nowUtc, default = true)
    val aliceContext = UserRepoDetails(Alice.id, "bootzooka", "master", clock.nowUtc, default = true)
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
    val context = UserRepoDetails(Bob.id, "bootzooka", "feature", clock.nowUtc, default = true)
    contextDao.save(context)

    // when
    val updatedContext = context.copy(branchName = "bugfix")
    contextDao.save(updatedContext)

    // then
    val Some(result) = contextDao.find(Bob.id, "bootzooka")
    result should be(updatedContext)
  }

}
