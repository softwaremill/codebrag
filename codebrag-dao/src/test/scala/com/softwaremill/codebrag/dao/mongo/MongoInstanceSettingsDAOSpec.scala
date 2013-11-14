package com.softwaremill.codebrag.dao.mongo

import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers

class MongoInstanceSettingsDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  /**
   * Uncomment the line below - simplifies testing in IDE
   * remember that this must point to folder above 'bin/mongod'
   */
  //System.setProperty("mongo.directory", "/opt/local")

  var instanceDao: InstanceSettingsDAO = _

  override def beforeEach() {
    super.beforeEach()
    instanceDao = new MongoInstanceSettingsDAO
  }

  it should "create new instance at first call" taggedAs RequiresDb in {
    // given
    val before = InstanceSettingsRecord.count

    // when
    val result = instanceDao.readOrCreate
    val after = InstanceSettingsRecord.count

    // then
    result.right.get should not be null
    result.right.get should not be ""
    before should be(0)
    after should be(1)
  }

  it should "return already created instance" taggedAs RequiresDb in {
    // given
    val before1 = InstanceSettingsRecord.count
    val before = instanceDao.readOrCreate.right.get
    val before2 = InstanceSettingsRecord.count

    // when
    val result = instanceDao.readOrCreate.right.get
    val after = InstanceSettingsRecord.count

    // then
    before1 should be(0)
    before2 should be(1)
    after should be(1)
    before.uniqueId should equal(result.uniqueId)
  }

  it should "never happened that there be two records" taggedAs RequiresDb in {
    // given
    val before = InstanceSettingsRecord.count
    InstanceSettingsRecord.createRecord.save
    InstanceSettingsRecord.createRecord.save

    // when
    val result = instanceDao.readOrCreate.left.get
    val after = InstanceSettingsRecord.count

    // then
    before should be(0)
    after should be(2)
    result should equal("More than one record exists in collection 'instance_settings'!")
  }

}
