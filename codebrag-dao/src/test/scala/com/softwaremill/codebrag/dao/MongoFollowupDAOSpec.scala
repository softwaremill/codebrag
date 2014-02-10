package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Followup
import org.joda.time.DateTime
import ObjectIdTestUtils._
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.test.mongo.ClearMongoDataAfterTest
import org.bson.types.ObjectId

class MongoFollowupDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {

  var followupDao: MongoFollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _

  val Commit = CommitInfoAssembler.randomCommit.get
  val FollowupTargetUserId = oid(12)
  val DifferentUserId1 = oid(15)
  val DifferentUserId2 = oid(14)

  val CommenterName = "John"
  val CommentAuthorId = new ObjectId
  val LikerName = "Mary"
  val CommentId = oid(20)
  val OtherCommentId = oid(30)
  val LikeId = oid(40)

  override def beforeEach() {
    super.beforeEach()
    followupDao = new MongoFollowupDAO
    commitInfoDao = new MongoCommitInfoDAO
    commitInfoDao.storeCommit(Commit)
  }

  it should "create new follow-up for user and commit if one doesn't exist" taggedAs (RequiresDb) in {
    // given
    val comment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withAuthorId(CommentAuthorId).get
    val followup = Followup(FollowupTargetUserId, comment)

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allRecords = FollowupRecord.findAll
    allRecords should have size (1)
    val followupFound = allRecords.head
    followupFound.lastReaction.get.reactionId.get should equal(CommentId)
    followupFound.receivingUserId.get should equal(FollowupTargetUserId)
  }

  it should "create new inline follow-up if one doesn't exist" taggedAs (RequiresDb) in {
    // given
    val now = DateTime.now()
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withAuthorId(CommentAuthorId).withFileNameAndLineNumber("file.txt", 20).get
    val followup = Followup(FollowupTargetUserId, inlineComment)

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allRecords = FollowupRecord.findAll
    allRecords should have size (1)
    val followupFound = allRecords.head
    followupFound.lastReaction.get.reactionId.get should equal(CommentId)
    followupFound.receivingUserId.get should equal(FollowupTargetUserId)
    followupFound.threadId.get.fileName.get should equal(followup.reaction.fileName)
    followupFound.threadId.get.lineNumber.get should equal(followup.reaction.lineNumber)
  }

  it should "update existing follow-up with reaction data when one already exits" taggedAs (RequiresDb) in {
    // given
    val comment = CommentAssembler.commentFor(Commit.id).withId(CommentId).get
    val createdFollowup = Followup(FollowupTargetUserId, comment)
    val createdFollowupId = followupDao.createOrUpdateExisting(createdFollowup)

    // when
    val newCommentId = oid(200)
    val newComment = CommentAssembler.commentFor(Commit.id).withId(newCommentId).get
    val updatedFollowup = Followup(FollowupTargetUserId, newComment)
    followupDao.createOrUpdateExisting(updatedFollowup)

    // then
    val updated = FollowupRecord.findAll.head
    updated.lastReaction.get.reactionId.get should equal(newCommentId)
    updated.id.get should equal(createdFollowupId)
  }

  it should "update follow up only for current thread" taggedAs (RequiresDb) in {
    // given
    val commentForCommit = CommentAssembler.commentFor(Commit.id).withId(CommentId).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commentForCommit))
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(OtherCommentId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val newCommentId = oid(123)
    val secondInlineComment = CommentAssembler.commentFor(Commit.id).withFileNameAndLineNumber("file.txt", 20).withId(newCommentId).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, secondInlineComment))


    // then
    val followups = FollowupRecord.where(_.lastReaction.subfield(_.reactionId) eqs newCommentId).fetch()
    followups.size should be(1)
    followups.head.threadId.get.lineNumber.get should equal(Some(20))
    followups.head.threadId.get.fileName.get should equal(Some("file.txt"))
  }

  it should "create new follow up for new inline comments thread" taggedAs (RequiresDb) in {
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(OtherCommentId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val newCommentId = oid(123)
    val commentForAnotherLine = CommentAssembler.commentFor(Commit.id).withId(newCommentId).withFileNameAndLineNumber("file.txt", 30).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commentForAnotherLine))

    // then
    FollowupRecord.count should be(2)
    val followups = FollowupRecord.where(_.threadId.subselect(_.fileName) eqs "file.txt").and(_.threadId.subselect(_.lineNumber) eqs 30).fetch()
    followups.size should be(1)
  }

  it should "create new follow up for entire commit comments thread if one doesn't exist" taggedAs (RequiresDb) in {
    val inlineComment = CommentAssembler.commentFor(Commit.id).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val newCommentId = oid(123)
    val commitComment = CommentAssembler.commentFor(Commit.id).withId(newCommentId).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commitComment))

    // then
    FollowupRecord.count should be(2)
  }

  it should "delete follow-up for single thread" taggedAs (RequiresDb) in {
    // given
    val commitComment = CommentAssembler.commentFor(Commit.id).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commitComment))
    val inlineComment = CommentAssembler.commentFor(Commit.id).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))
    val anotherInlineComment = CommentAssembler.commentFor(Commit.id).withFileNameAndLineNumber("file.txt", 30).get
    val toRemoveId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, anotherInlineComment))

    // when
    followupDao.delete(toRemoveId)

    // then
    val followupsLeft = FollowupRecord.select(_.threadId.subselect(_.lineNumber)).fetch()
    followupsLeft.toSet should equal(Set(Some(20), None))
  }

  it should "not delete follow-ups of other users" taggedAs (RequiresDb) in {
    // given
    val commitComment = CommentAssembler.commentFor(Commit.id).get
    followupDao.createOrUpdateExisting(Followup(DifferentUserId1, commitComment))
    followupDao.createOrUpdateExisting(Followup(DifferentUserId2, commitComment))
    val toRemoveId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commitComment))

    // when
    followupDao.delete(toRemoveId)

    // then
    val storedUserIds = FollowupRecord.findAll.map(_.receivingUserId.get)
    storedUserIds should equal(List(DifferentUserId1, DifferentUserId2))
  }

  it should "update followup for like when comment followup exists for file/line" in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withFileNameAndLineNumber("file.txt", 20).get
    val followupId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val inlineLike = LikeAssembler.likeFor(Commit.id).withId(LikeId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineLike))

    // then
    val followups = FollowupRecord.findAll
    followups.length should be(1)
    val updated = followups.head
    updated.lastReaction.get.reactionId.get should equal(inlineLike.id)
    updated.id.get should equal(followupId)
  }

  it should "add reaction to followup's reactions list when new folllowup is created" in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withFileNameAndLineNumber("file.txt", 20).get

    // when
    val followupId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // then
    val Some(found) = FollowupRecord.where(_.id eqs followupId).get()
    found.reactions.get should equal(List(CommentId))
  }

  it should "add reaction to followup's reactions list when folllowup is updated" in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withFileNameAndLineNumber("file.txt", 20).get
    val followupId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val inlineLike = LikeAssembler.likeFor(Commit.id).withId(LikeId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineLike))

    // then
    val Some(found) = FollowupRecord.where(_.id eqs followupId).get()
    found.reactions.get should equal(List(CommentId, LikeId))
  }

}
