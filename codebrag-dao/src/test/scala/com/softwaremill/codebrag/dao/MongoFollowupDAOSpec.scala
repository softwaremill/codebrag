package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Followup
import org.joda.time.DateTime
import pl.softwaremill.common.util.time.FixtureTimeClock
import ObjectIdTestUtils._
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler

class MongoFollowupDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var followupDao: MongoFollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _

  val Commit = CommitInfoAssembler.randomCommit.get
  val FollowupTargetUserId = oid(12)
  val DifferentUserId1 = oid(15)
  val DifferentUserId2 = oid(14)

  override def beforeEach() {
    followupDao = new MongoFollowupDAO
    commitInfoDao = new MongoCommitInfoDAO
    CommitInfoRecord.drop
    FollowupRecord.drop
    commitInfoDao.storeCommit(Commit)
  }

  behavior of "MongoFollowupDAO"

  it should "create new follow up for user and commit if one doesn't exist" in {
    // given
    val now = DateTime.now()
    val followup = Followup(Commit.id, FollowupTargetUserId, now)

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allRecords = FollowupRecord.findAll
    allRecords should have size(1)
    val followupFound = allRecords.head
    followupFound.user_id.get should equal(FollowupTargetUserId)
    followupFound.commit.get.id.get should equal(Commit.id)
    followupFound.date.get should equal(now.toDate)
  }

  it should "update existing follow up creation date when one exists for user and commit" in {
    followupDao.createOrUpdateExisting(Followup(Commit.id, FollowupTargetUserId, DateTime.now()));

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val updatedFollowup = Followup(Commit.id, FollowupTargetUserId, newDate)
    followupDao.createOrUpdateExisting(updatedFollowup)


    // then
    val followup = FollowupRecord.findAll.head
    followup.date.get should equal(newDate.toDate)
  }

  it should "delete a single follow-up from storage" in {
    followupDao.createOrUpdateExisting(Followup(Commit.id, FollowupTargetUserId, DateTime.now()));

    // when
    followupDao.delete(Commit.id, FollowupTargetUserId)

    // then
    FollowupRecord.findAll should be('empty)
  }


  it should "not delete follow-ups of other users" in {
    followupDao.createOrUpdateExisting(Followup(Commit.id, DifferentUserId1, DateTime.now()));
    followupDao.createOrUpdateExisting(Followup(Commit.id, FollowupTargetUserId, DateTime.now()));
    followupDao.createOrUpdateExisting(Followup(Commit.id, DifferentUserId2, DateTime.now()));

    // when
    followupDao.delete(Commit.id, FollowupTargetUserId)

    // then
    val storedUserIds = FollowupRecord.findAll.map(_.user_id.get)
    storedUserIds should equal (List(DifferentUserId1, DifferentUserId2))
  }
}
