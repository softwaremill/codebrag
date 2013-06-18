package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import org.joda.time.DateTime
import pl.softwaremill.common.util.time.FixtureTimeClock
import ObjectIdTestUtils._
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoFollowupDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  var followupDao: MongoFollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _

  val Commit = CommitInfoAssembler.randomCommit.get
  val FollowupTargetUserId = oid(12)
  val DifferentUserId1 = oid(15)
  val DifferentUserId2 = oid(14)

  val CommenterName = "John"
  val CommentId = oid(20)
  val OtherCommentId = oid(30)

  override def beforeEach() {
    super.beforeEach()
    followupDao = new MongoFollowupDAO
    commitInfoDao = new MongoCommitInfoDAO
    commitInfoDao.storeCommit(Commit)
  }

  it should "create new follow up for user and thread if one doesn't exist" taggedAs(RequiresDb) in {
    // given
    val now = DateTime.now()
    val followup = Followup(CommentId, FollowupTargetUserId, now, CommenterName, ThreadDetails(Commit.id))

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allRecords = FollowupRecord.findAll
    allRecords should have size(1)
    val followupFound = allRecords.head
    followupFound.user_id.get should equal(FollowupTargetUserId)
    followupFound.commit.get.id.get should equal(Commit.id)
    followupFound.reactionId.get should equal(CommentId)
    followupFound.date.get should equal(now.toDate)
  }

  it should "update existing follow up creation date and last comment data when followup for thread and user exists" taggedAs(RequiresDb) in {
    followupDao.createOrUpdateExisting(Followup(CommentId, FollowupTargetUserId, DateTime.now, CommenterName, ThreadDetails(Commit.id)))

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val newCommentId = oid(200)
    val newCommentAuthorName = "Mary"
    val updatedFollowup = Followup(newCommentId, FollowupTargetUserId, newDate, newCommentAuthorName, ThreadDetails(Commit.id))
    followupDao.createOrUpdateExisting(updatedFollowup)


    // then
    val followup = FollowupRecord.findAll.head
    followup.reactionId.get should equal(newCommentId)
    followup.date.get should equal(newDate.toDate)
    followup.lastCommenterName.get should equal(newCommentAuthorName)
  }

  it should "update follow up only for current thread" taggedAs(RequiresDb) in {
    val baseDate = DateTime.now
    followupDao.createOrUpdateExisting(Followup(CommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id)))
    followupDao.createOrUpdateExisting(Followup(OtherCommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id, Some(20), Some("file.txt"))))

    // when
    val newDate = new FixtureTimeClock(23213213).currentDateTime()
    val newCommentId = oid(123)
    val updatedFollowup = Followup(newCommentId, FollowupTargetUserId, newDate, CommenterName, ThreadDetails(Commit.id, Some(20), Some("file.txt")))
    followupDao.createOrUpdateExisting(updatedFollowup)


    // then
    val followups = FollowupRecord.where(_.reactionId eqs newCommentId).fetch()
    followups.size should be(1)
    followups.head.date.get should equal(newDate.toDate)
    followups.head.threadId.get.lineNumber.get should equal(Some(20))
    followups.head.threadId.get.fileName.get should equal(Some("file.txt"))
  }

  it should "create new follow up for new inline comments thread" taggedAs(RequiresDb) in {
    val baseDate = DateTime.now
    followupDao.createOrUpdateExisting(Followup(CommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id)))
    followupDao.createOrUpdateExisting(Followup(OtherCommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id, Some(20), Some("file.txt"))))

    // when
    val newCommentId = oid(123)
    val newFollowup = Followup(newCommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id, Some(30), Some("file.txt")))
    followupDao.createOrUpdateExisting(newFollowup)

    // then
    FollowupRecord.count should be(3)
    val followups = FollowupRecord.where(_.threadId.subselect(_.fileName) eqs "file.txt").and(_.threadId.subselect(_.lineNumber) eqs 30).fetch()
    followups.size should be(1)
  }

  it should "create new follow up for entire commit comments thread if one doesn't exist" taggedAs(RequiresDb) in {
    val baseDate = DateTime.now
    followupDao.createOrUpdateExisting(Followup(CommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id, Some(20), Some("file.txt"))))

    // when
    val newCommentId = oid(123)
    val newFollowup = Followup(newCommentId, FollowupTargetUserId, baseDate, CommenterName, ThreadDetails(Commit.id))
    followupDao.createOrUpdateExisting(newFollowup)

    // then
    FollowupRecord.count should be(2)
  }

  it should "delete follow-up for single thread" taggedAs(RequiresDb) in {
    followupDao.createOrUpdateExisting(Followup(Commit.id, FollowupTargetUserId, DateTime.now, CommenterName, ThreadDetails(Commit.id, Some(20), Some("file.txt"))))
    val followupToRemove = Followup(oid(123), Commit.id, FollowupTargetUserId, DateTime.now, CommenterName, ThreadDetails(Commit.id))
    followupDao.createOrUpdateExisting(followupToRemove)
    followupDao.createOrUpdateExisting(Followup(Commit.id, FollowupTargetUserId, DateTime.now, CommenterName, ThreadDetails(Commit.id, Some(23), Some("test.txt"))))

    // when
    followupDao.delete(followupToRemove.id.get)

    // then
    val followupsLeft = FollowupRecord.select(_.threadId.subselect(_.fileName)).fetch()
    followupsLeft.toSet should equal(Set(Some("file.txt"), Some("test.txt")))
  }

  it should "not delete follow-ups of other users" taggedAs(RequiresDb) in {
    followupDao.createOrUpdateExisting(Followup(Commit.id, DifferentUserId1, DateTime.now, CommenterName, ThreadDetails(Commit.id)))
    val followupToRemove = Followup(oid(123), Commit.id, FollowupTargetUserId, DateTime.now, CommenterName, ThreadDetails(Commit.id))
    followupDao.createOrUpdateExisting(followupToRemove)
    followupDao.createOrUpdateExisting(Followup(Commit.id, DifferentUserId2, DateTime.now, CommenterName, ThreadDetails(Commit.id)))

    // when
    followupDao.delete(followupToRemove.id.get)

    // then
    val storedUserIds = FollowupRecord.findAll.map(_.user_id.get)
    storedUserIds should equal (List(DifferentUserId1, DifferentUserId2))
  }
}
