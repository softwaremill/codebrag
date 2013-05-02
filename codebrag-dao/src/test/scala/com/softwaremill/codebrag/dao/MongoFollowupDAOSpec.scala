package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{CommentThreadId, ThreadAwareFollowup}
import org.joda.time.DateTime
import pl.softwaremill.common.util.time.FixtureTimeClock
import ObjectIdTestUtils._
import com.foursquare.rogue.LiftRogue._
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

  it should "create new follow up for user and thread if one doesn't exist" in {
    // given
    val now = DateTime.now()
    val followup = ThreadAwareFollowup(Commit.id, FollowupTargetUserId, now, CommentThreadId(Commit.id))

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

  it should "update existing follow up creation date when one exists for user and thread" in {
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, DateTime.now, CommentThreadId(Commit.id)))

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val updatedFollowup = ThreadAwareFollowup(Commit.id, FollowupTargetUserId, newDate, CommentThreadId(Commit.id))
    followupDao.createOrUpdateExisting(updatedFollowup)


    // then
    val followup = FollowupRecord.findAll.head
    followup.date.get should equal(newDate.toDate)
  }

  it should "update follow up only for current thread" in {
    val baseDate = DateTime.now
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, baseDate, CommentThreadId(Commit.id)))
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, baseDate, CommentThreadId(Commit.id, Some(20), Some("file.txt"))))

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val updatedFollowup = ThreadAwareFollowup(Commit.id, FollowupTargetUserId, newDate, CommentThreadId(Commit.id, Some(20), Some("file.txt")))
    followupDao.createOrUpdateExisting(updatedFollowup)


    // then
    val followups = FollowupRecord.where(_.date eqs newDate).fetch()
    followups.size should be(1)
    followups.head.date.get should equal(newDate.toDate)
  }

  it should "create new follow up for thread" in {
    val baseDate = DateTime.now
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, baseDate, CommentThreadId(Commit.id)))
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, baseDate, CommentThreadId(Commit.id, Some(20), Some("file.txt"))))

    // when
    val updatedFollowup = ThreadAwareFollowup(Commit.id, FollowupTargetUserId, baseDate, CommentThreadId(Commit.id, Some(30), Some("file.txt")))
    followupDao.createOrUpdateExisting(updatedFollowup)

    // then
    FollowupRecord.count should be(3)
    val followups = FollowupRecord.where(_.threadId.subselect(_.fileName) eqs "file.txt").and(_.threadId.subselect(_.lineNumber) eqs 30).fetch()
    followups.size should be(1)
  }


  // TODO: removing followups for given threadId only
  ignore should "delete a single follow-up from storage" in {
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, DateTime.now, CommentThreadId(Commit.id)))
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, DateTime.now, CommentThreadId(Commit.id, Some(20), Some("file.txt"))))

    // when
    followupDao.delete(Commit.id, FollowupTargetUserId)

    // then
    FollowupRecord.count should be(1)
  }


  it should "not delete follow-ups of other users" in {
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, DifferentUserId1, DateTime.now, CommentThreadId(Commit.id)))
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, FollowupTargetUserId, DateTime.now, CommentThreadId(Commit.id)))
    followupDao.createOrUpdateExisting(ThreadAwareFollowup(Commit.id, DifferentUserId2, DateTime.now, CommentThreadId(Commit.id)))

    // when
    followupDao.delete(Commit.id, FollowupTargetUserId)

    // then
    val storedUserIds = FollowupRecord.findAll.map(_.user_id.get)
    storedUserIds should equal (List(DifferentUserId1, DifferentUserId2))
  }
}
