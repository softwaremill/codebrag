package com.softwaremill.codebrag.dao.branch

import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserWatchedBranch


class SQLWatchedBranchesDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with BeforeAndAfterEach with ShouldMatchers with Logging {

  val dao = new SQLWatchedBranchesDao(sqlDatabase)

  val Bob = UserAssembler.randomUser.get
  val Alice = UserAssembler.randomUser.get

  it should "create record for observed repo/branch for user" in {
    // given
    val observed = UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master")

    // when
    dao.save(observed)

    // then
    dao.findAll(Bob.id) should be(Set(observed))
  }

  it should "not create duplicated record for user/repo/branch" in {
    // given
    val first = UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master")
    val second = UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master")
    dao.save(first)

    // when
    intercept[Exception] {
      dao.save(second)
    }
  }

  it should "create record for same user but different repo/branch" in {
    // given
    val toSave = Seq(
      UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master"),
      UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "bugfix"),
      UserWatchedBranch(new ObjectId, Bob.id, "bootzooka", "master"),
      UserWatchedBranch(new ObjectId, Bob.id, "bootzooka", "bugfix")
    )

    // when
    toSave.foreach(dao.save)

    // then
    dao.findAll(Bob.id) should be(toSave.toSet)
  }

  it should "remove observed branch by id" in {
    // given
    val first = UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master")
    val second = UserWatchedBranch(new ObjectId, Bob.id, "bootzooka", "bugfix")
    Seq(first, second).foreach(dao.save)

    // when
    dao.delete(first.id)

    // then
    dao.findAll(Bob.id) should be(Set(second))
  }

  it should "find observed branches for given user" in {
    // given
    val aliceBranch = UserWatchedBranch(new ObjectId, Alice.id, "codebrag", "master")
    val bobBranch = UserWatchedBranch(new ObjectId, Bob.id, "bootzooka", "bugfix")
    Seq(aliceBranch, bobBranch).foreach(dao.save)

    // when
    val result = dao.findAll(Alice.id)

    // then
    result should be(Set(aliceBranch))
  }

}
