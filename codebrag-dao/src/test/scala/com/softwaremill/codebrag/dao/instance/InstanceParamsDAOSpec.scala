package com.softwaremill.codebrag.dao.instance

import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.domain.InstanceId
import org.scalatest.matchers.ShouldMatchers

class InstanceParamsDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ShouldMatchers {

  val dao = new InstanceParamsDAO(sqlDatabase)

  it should "save and read instance param (e.g. instance id) if not exists" in {
    // given
    val instanceIdParam = InstanceId("abcd1234").toInstanceParam

    // when
    dao.save(instanceIdParam)
    val Some(fetched) = dao.findByKey(InstanceId.Key)

    // then
    fetched should be(instanceIdParam)
  }

  it should "update and read instance param (e.g. instance id) if one exists" in {
    // given
    val instanceIdParam = InstanceId("abcd1234").toInstanceParam
    dao.save(instanceIdParam)
    val newInstanceIdParam = InstanceId("xyz123").toInstanceParam

    // when
    dao.save(newInstanceIdParam)
    val Some(fetched) = dao.findByKey(InstanceId.Key)

    // then
    fetched should be(newInstanceIdParam)
  }

}
