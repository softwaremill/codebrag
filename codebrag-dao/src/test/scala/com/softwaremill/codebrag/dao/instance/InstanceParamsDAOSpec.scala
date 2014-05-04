package com.softwaremill.codebrag.dao.instance

import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.domain.InstanceLicence
import org.scalatest.matchers.ShouldMatchers

class InstanceParamsDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ShouldMatchers {

  val dao = new InstanceParamsDAO(sqlDatabase)

  it should "save and read instance param (e.g. licence key) if not exists" in {
    // given
    val licenceKeyParam = InstanceLicence("abcd1234").toInstanceParam

    // when
    dao.save(licenceKeyParam)
    val Some(fetched) = dao.findByKey(InstanceLicence.Key)

    // then
    fetched should be(licenceKeyParam)
  }

  it should "update and read instance param (e.g. licence key) if one exists" in {
    // given
    val licenceKeyParam = InstanceLicence("abcd1234").toInstanceParam
    dao.save(licenceKeyParam)
    val newLicenceKeyParam = InstanceLicence("xyz123").toInstanceParam

    // when
    dao.save(newLicenceKeyParam)
    val Some(fetched) = dao.findByKey(InstanceLicence.Key)

    // then
    fetched should be(newLicenceKeyParam)
  }

}
