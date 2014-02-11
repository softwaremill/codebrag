package com.softwaremill.codebrag.dao.heartbeat

import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}
import com.softwaremill.codebrag.common.{RealTimeClock, FixtureTimeClock, Clock}
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.{RequiresDb, ObjectIdTestUtils}
import org.scalatest.FlatSpec

trait HeartbeatDAOSpec extends FlatSpec with ShouldMatchers {
  def heartbeatDAO(clock: Clock): HeartbeatDAO

  var clock1000 = new FixtureTimeClock(1000)
  var clock1500 = new FixtureTimeClock(1500)

  def heartbeatDAO1500 = heartbeatDAO(clock1500)
  def heartbeatDAO1000 = heartbeatDAO(clock1000)

  "update" should "create a new entry" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId

    //when
    heartbeatDAO1500 update userId

    //then
    heartbeatDAO1500.get(userId).get.getMillis should be < DateTime.now(DateTimeZone.UTC).getMillis
  }

  it should "update an existing entry" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId
    heartbeatDAO1000 update userId

    //when
    heartbeatDAO1500 update userId

    //then
    heartbeatDAO1500.get(userId).get.getMillis should be > 1000L
  }

  "get" should "return time of the last heartbeat when one was stored" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId
    heartbeatDAO1000 update userId

    //when
    val time = heartbeatDAO1000 get userId

    //then
    time.get.toDateTime(DateTimeZone.UTC) should equal (clock1000.nowUtc)
  }

  it should "not return null when no heartbeat for user was found" taggedAs RequiresDb in {
    //given
    val userId = new ObjectId

    //when
    val time = heartbeatDAO1000 get userId

    //then
    time should be (None)
  }

  "loadAll" should "return empty list if no heartbeats are stored" taggedAs RequiresDb in {
    heartbeatDAO1000.loadAll should equal(List.empty)
  }

  it should "return all stored heartbeats" taggedAs RequiresDb in {
    //given
    val User1 = ObjectIdTestUtils.oid(1)
    val User2 = ObjectIdTestUtils.oid(2)
    val User3 = ObjectIdTestUtils.oid(3)
    heartbeatDAO1000.update(User1)
    heartbeatDAO1000.update(User2)
    heartbeatDAO1000.update(User3)

    //when
    val heartbeats = heartbeatDAO1000.loadAll()

    //then
    heartbeats should have size 3
  }
}

class MongoHeartbeatDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with HeartbeatDAOSpec {
  def heartbeatDAO(clock: Clock) = new MongoHeartbeatDAO(clock)
}

class SQLHeartbeatDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with HeartbeatDAOSpec {
  def heartbeatDAO(clock: Clock) = new SQLHeartbeatDAO(sqlDatabase, clock)

  def withSchemas = List(heartbeatDAO(RealTimeClock))
}
