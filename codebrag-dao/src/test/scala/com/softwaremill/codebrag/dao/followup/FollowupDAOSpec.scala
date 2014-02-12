package com.softwaremill.codebrag.dao.followup

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.dao.RequiresDb
import org.scalatest.FlatSpec

trait FollowupDAOSpec extends FlatSpec with ShouldMatchers {

  def followupDao: FollowupDAO
  
  case class StoredFollowup(
    id: ObjectId,
    receivingUserId: ObjectId,
    lastReactionId: ObjectId,
    threadFileName: Option[String],
    threadLineNumber: Option[Int],
    reactions: List[ObjectId])
  def findAllStoredFollowups(): List[StoredFollowup]

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

  it should "create new follow-up for user and commit if one doesn't exist" taggedAs RequiresDb in {
    // given
    val comment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withAuthorId(CommentAuthorId).get
    val followup = Followup(FollowupTargetUserId, comment)

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allFollowups = findAllStoredFollowups()
    allFollowups should have size (1)
    val followupFound = allFollowups.head
    followupFound.lastReactionId should equal(CommentId)
    followupFound.receivingUserId should equal(FollowupTargetUserId)
  }

  it should "create new inline follow-up if one doesn't exist" taggedAs RequiresDb in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withAuthorId(CommentAuthorId).withFileNameAndLineNumber("file.txt", 20).get
    val followup = Followup(FollowupTargetUserId, inlineComment)

    // when
    followupDao.createOrUpdateExisting(followup)

    // then
    val allFollowups = findAllStoredFollowups()
    allFollowups should have size (1)
    val followupFound = allFollowups.head
    followupFound.lastReactionId should equal(CommentId)
    followupFound.receivingUserId should equal(FollowupTargetUserId)
    followupFound.threadFileName should equal(followup.reaction.fileName)
    followupFound.threadLineNumber should equal(followup.reaction.lineNumber)
  }

  it should "update existing follow-up with reaction data when one already exits" taggedAs RequiresDb in {
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
    val updated = findAllStoredFollowups().head
    updated.lastReactionId should equal(newCommentId)
    updated.id should equal(createdFollowupId)
  }

  it should "update follow up only for current thread" taggedAs RequiresDb in {
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
    val followups = findAllStoredFollowups().filter(_.lastReactionId == newCommentId)
    followups.size should be(1)
    followups.head.threadLineNumber should equal(Some(20))
    followups.head.threadFileName should equal(Some("file.txt"))
  }

  it should "create new follow up for new inline comments thread" taggedAs RequiresDb in {
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(OtherCommentId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val newCommentId = oid(123)
    val commentForAnotherLine = CommentAssembler.commentFor(Commit.id).withId(newCommentId).withFileNameAndLineNumber("file.txt", 30).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commentForAnotherLine))

    // then
    val allFollowups = findAllStoredFollowups()
    allFollowups.size should be(2)
    val followups = allFollowups.filter(_.threadFileName == Some("file.txt")).filter(_.threadLineNumber == Some(30))
    followups.size should be(1)
  }

  it should "create new follow up for entire commit comments thread if one doesn't exist" taggedAs RequiresDb in {
    val inlineComment = CommentAssembler.commentFor(Commit.id).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val newCommentId = oid(123)
    val commitComment = CommentAssembler.commentFor(Commit.id).withId(newCommentId).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commitComment))

    // then
    findAllStoredFollowups().size should be(2)
  }

  it should "delete follow-up for single thread" taggedAs RequiresDb in {
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
    val followupsLeft = findAllStoredFollowups().map(_.threadLineNumber)
    followupsLeft.toSet should equal(Set(Some(20), None))
  }

  it should "not delete follow-ups of other users" taggedAs RequiresDb in {
    // given
    val commitComment = CommentAssembler.commentFor(Commit.id).get
    followupDao.createOrUpdateExisting(Followup(DifferentUserId1, commitComment))
    followupDao.createOrUpdateExisting(Followup(DifferentUserId2, commitComment))
    val toRemoveId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, commitComment))

    // when
    followupDao.delete(toRemoveId)

    // then
    val storedUserIds = findAllStoredFollowups().map(_.receivingUserId)
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
    val followups = findAllStoredFollowups()
    followups.size should be(1)
    val updated = followups.head
    updated.lastReactionId should equal(inlineLike.id)
    updated.id should equal(followupId)
  }

  it should "add reaction to followup's reactions list when new folllowup is created" in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withFileNameAndLineNumber("file.txt", 20).get

    // when
    val followupId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // then
    val List(found) = findAllStoredFollowups().filter(_.id == followupId)
    found.reactions should equal(List(CommentId))
  }

  it should "add reaction to followup's reactions list when folllowup is updated" in {
    // given
    val inlineComment = CommentAssembler.commentFor(Commit.id).withId(CommentId).withFileNameAndLineNumber("file.txt", 20).get
    val followupId = followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineComment))

    // when
    val inlineLike = LikeAssembler.likeFor(Commit.id).withId(LikeId).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(FollowupTargetUserId, inlineLike))

    // then
    val List(found) = findAllStoredFollowups().filter(_.id == followupId)
    found.reactions should equal(List(CommentId, LikeId))
  }

}

class MongoFollowupDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with FollowupDAOSpec {
  val followupDao = new MongoFollowupDAO()

  def findAllStoredFollowups() = FollowupRecord.findAll.map { f =>
    StoredFollowup(
      f.id.get,
      f.receivingUserId.get,
      f.lastReaction.get.reactionId.get,
      f.threadId.get.fileName.get,
      f.threadId.get.lineNumber.get,
      f.reactions.get
    )
  }
}
