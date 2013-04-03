package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.FollowUp
import org.joda.time.DateTime
import pl.softwaremill.common.util.time.FixtureTimeClock

class MongoFollowUpDAOSpec extends FlatSpecWithRemoteMongo with BeforeAndAfterEach with ShouldMatchers {

  var followUpDAO: MongoFollowUpDAO = _

  val Commit = CommitInfoBuilder.createRandomCommit()
  val FollowUpTargetUserId = ObjectIdTestUtils.oid(12)

  override def beforeEach() {
    followUpDAO = new MongoFollowUpDAO
    FollowUpRecord.drop
  }

  behavior of "MongoFollowUpDAO"

  it should "create new follow up for user and commit if one doesn't exist" in {
    // given
    val now = DateTime.now()
    val followUp = FollowUp(Commit, FollowUpTargetUserId, now)

    // when
    followUpDAO.createOrUpdateExisting(followUp)

    // then
    val allRecords = FollowUpRecord.findAll
    allRecords should have size(1)
    val followUpFound = allRecords.head
    followUpFound.user_id.get should equal(FollowUpTargetUserId)
    followUpFound.commit.get.id.get should equal(Commit.id)
    followUpFound.date.get should equal(now.toDate)
  }

  it should "update existing follow up creation date when one exists for user and commit" in {
    followUpDAO.createOrUpdateExisting(FollowUp(Commit, FollowUpTargetUserId, DateTime.now()));

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val updatedFollowUp = FollowUp(Commit, FollowUpTargetUserId, newDate)
    followUpDAO.createOrUpdateExisting(updatedFollowUp)


    // then
    val followUp = FollowUpRecord.findAll.head
    followUp.date.get should equal(newDate.toDate)
  }

}
