package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.common.{FixtureTimeClock, Clock}

class MongoHeartbeatStoreSpec extends FlatSpecWithMongo with ShouldMatchers with ClearDataAfterTest {
  var store: MongoHeartbeatStore = _
  var clock: Clock = new FixtureTimeClock(1500)

  override def beforeEach() {
    super.beforeEach()
    store = new MongoHeartbeatStore(clock)
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
    val fixtureClock = new FixtureTimeClock(1000)
    val lastHeartbeat = fixtureClock.currentDateTimeUTC.minusDays(2)
    HeartbeatRecord.createRecord.id(userId).lastHeartbeat(lastHeartbeat.toDate).save

    //when
    store update userId

    //then
    store.get(userId).get.getMillis should be > lastHeartbeat.getMillis
  }

  "get" should "return time of the last heartbeat when one was stored" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId
    val fixtureClock = new FixtureTimeClock(1000)
    val lastHeartbeat = fixtureClock.currentDateTimeUTC.minusDays(2)
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

  "loadAll" should "return empty list if no heartbeats are stored" taggedAs RequiresDb in {
    store.loadAll should equal(List.empty)
  }

  it should "return all stored heartbeats" taggedAs RequiresDb in {
    //given
    val User1 = ObjectIdTestUtils.oid(1)
    val User2 = ObjectIdTestUtils.oid(2)
    val User3 = ObjectIdTestUtils.oid(3)
    store.update(User1)
    store.update(User2)
    store.update(User3)

    //when
    val heartbeats = store.loadAll()

    //then
    heartbeats should have size 3
  }
}
