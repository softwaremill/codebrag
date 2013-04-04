package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Followup
import org.joda.time.DateTime
import pl.softwaremill.common.util.time.FixtureTimeClock

class MongoFollowupDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var followupDAO: MongoFollowupDAO = _

  val Commit = CommitInfoBuilder.createRandomCommit()
  val FollowupTargetUserId = ObjectIdTestUtils.oid(12)

  override def beforeEach() {
    followupDAO = new MongoFollowupDAO
    FollowupRecord.drop
  }

  behavior of "MongoFollowupDAO"

  it should "create new follow up for user and commit if one doesn't exist" in {
    // given
    val now = DateTime.now()
    val followup = Followup(Commit, FollowupTargetUserId, now)

    // when
    followupDAO.createOrUpdateExisting(followup)

    // then
    val allRecords = FollowupRecord.findAll
    allRecords should have size(1)
    val followupFound = allRecords.head
    followupFound.user_id.get should equal(FollowupTargetUserId)
    followupFound.commit.get.id.get should equal(Commit.id)
    followupFound.date.get should equal(now.toDate)
  }

  it should "update existing follow up creation date when one exists for user and commit" in {
    followupDAO.createOrUpdateExisting(Followup(Commit, FollowupTargetUserId, DateTime.now()));

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val updatedFollowup = Followup(Commit, FollowupTargetUserId, newDate)
    followupDAO.createOrUpdateExisting(updatedFollowup)


    // then
    val followup = FollowupRecord.findAll.head
    followup.date.get should equal(newDate.toDate)
  }

}
