package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.builders.{LikeAssembler, CommentAssembler}
import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import org.joda.time.DateTime

class MongoFollowupWithUpdateableReactionsDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  val commentDao = new MongoCommitCommentDAO
  val likeDao = new MongoLikeDAO
  val followupDao = new MongoFollowupDAO
  val followupWithReactionsDao = new MongoFollowupWithUpdateableReactionsDAO(commentDao, likeDao)

  val commitId = ObjectIdTestUtils.oid(100)
  val userId = ObjectIdTestUtils.oid(200)

  val baseDate = DateTime.now
  val comment = CommentAssembler.commentFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate).get
  val firstLike = LikeAssembler.likeFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate.plusHours(1)).get
  val secondLike = LikeAssembler.likeFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate.plusHours(2)).get

  it should "load followup with all reactions" in {
    // given
    val followupId = persistReactionsWithFollowup

    // when
    val Some(followup) = followupWithReactionsDao.findById(followupId)

    // then
    followup.allReactions.size should be(3)
    followup.ownerId should be(userId)
    followup.thread should be(ThreadDetails.inline(commitId, 10, "file.txt"))
  }

  it should "loaded followup should have correct last reaction" in {
    // given
    val followupId = persistReactionsWithFollowup

    // when
    val Some(followup) = followupWithReactionsDao.findById(followupId)

    // then
    followup.lastReaction should equal(secondLike)
  }

  private def persistReactionsWithFollowup = {
    commentDao.save(comment)
    followupDao.createOrUpdateExisting(Followup(userId, comment))
    likeDao.save(firstLike)
    followupDao.createOrUpdateExisting(Followup(userId, firstLike))
    likeDao.save(secondLike)
    followupDao.createOrUpdateExisting(Followup(userId, secondLike))
  }

}
