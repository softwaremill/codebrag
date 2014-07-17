package com.softwaremill.codebrag.dao.user

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.UserAlias
import org.bson.types.ObjectId

class SQLUserAliasDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with BeforeAndAfterEach with ShouldMatchers with Logging {

  val aliasDao = new SQLUserAliasDAO(sqlDatabase)

  val BobId = new ObjectId
  val AliceId = new ObjectId

  it should "save new alias for user" in {
    // given
    val alias = UserAlias(new ObjectId, BobId, "email@codebrag.com")

    // when
    aliasDao.save(alias)

    // then
    val found = aliasDao.findAllForUser(BobId)
    found should be(List(alias))
  }

  it should "delete existing alias" in {
    // given
    val alias = UserAlias(new ObjectId, BobId, "email@codebrag.com")
    aliasDao.save(alias)

    // when
    aliasDao.remove(alias.id)

    // then
    val found = aliasDao.findByAlias(alias.alias)
    found should be('empty)
  }

  it should "find existing alias" in {
    // given
    val alias = UserAlias(new ObjectId, BobId, "email@codebrag.com")
    aliasDao.save(alias)

    // when
    val Some(result) = aliasDao.findByAlias(alias.alias)

    // then
    result should be(alias)
  }

  it should "not save new alias if one exists for any user" in {
    // given
    val email = "same@email.com"
    val bobAlias = UserAlias(new ObjectId, BobId, email)
    aliasDao.save(bobAlias)

    // when
    val aliceAlias = UserAlias(new ObjectId, AliceId, email)
    intercept[Exception] {
      aliasDao.save(aliceAlias)
    }
  }
}
