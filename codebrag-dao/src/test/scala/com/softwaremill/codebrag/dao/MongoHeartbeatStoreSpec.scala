package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoHeartbeatStoreSpec extends FlatSpecWithMongo with ShouldMatchers with ClearDataAfterTest {
  var store: MongoHeartbeatStore = _

  override def beforeEach() {
    super.beforeEach()
    store = new MongoHeartbeatStore
  }

  "update" should "create a new entry" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId

    //when
    store update userId

    //then
    store.get(userId).get.getMillis should be < DateTime.now(DateTimeZone.UTC).getMillis
  }

  it should "update an existing entry" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId
    val lastHeartbeat = DateTime.now(DateTimeZone.UTC).minusDays(2)
    HeartbeatRecord.createRecord.id(userId).lastHeartbeat(lastHeartbeat.toDate).save

    //when
    store update userId

    //then
    store.get(userId).get.getMillis should be > lastHeartbeat.getMillis
  }

  "get" should "return time of the last heartbeat when one was stored" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId
    val lastHeartbeat = DateTime.now(DateTimeZone.UTC).minusDays(2)
    HeartbeatRecord.createRecord.id(userId).lastHeartbeat(lastHeartbeat.toDate).save

    //when
    val time = store get userId

    //then
    time.get.toDateTime(DateTimeZone.UTC) should equal(lastHeartbeat)
  }

  it should "not return null when no heartbeat for user was found" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId

    //when
    val time = store get userId

    //then
    time should be(None)
  }
}
