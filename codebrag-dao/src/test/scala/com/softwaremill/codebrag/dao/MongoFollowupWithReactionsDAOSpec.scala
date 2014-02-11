package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler}
import com.softwaremill.codebrag.test.{FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.reaction.MongoCommitCommentDAO

class MongoFollowupWithReactionsDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {

  val commentDao = new MongoCommitCommentDAO
  val likeDao = new MongoLikeDAO
  val followupDao = new MongoFollowupDAO
  val followupWithReactionsDao = new MongoFollowupWithReactionsDAO(commentDao, likeDao)

  val commitId = ObjectIdTestUtils.oid(100)
  val userId = ObjectIdTestUtils.oid(200)
  val anotherUserId = ObjectIdTestUtils.oid(300)

  val baseDate = DateTime.now
  val comment = CommentAssembler.commentFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate).get
  val firstLike = LikeAssembler.likeFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate.plusHours(1)).get
  val secondLike = LikeAssembler.likeFor(commitId).withFileNameAndLineNumber("file.txt", 10).withDate(baseDate.plusHours(2)).get

  it should "load followup with all reactions" in {
    // given
    val followupId = persistReactionsWithFollowup

    // when
    val Some(Right(followup)) = followupWithReactionsDao.findById(followupId)

    // then
    followup.allReactions.size should be(3)
    followup.ownerId should be(userId)
    followup.thread should be(ThreadDetails.inline(commitId, 10, "file.txt"))
  }

  it should "loaded followup should have correct last reaction" in {
    // given
    val followupId = persistReactionsWithFollowup

    // when
    val Some(Right(followup)) = followupWithReactionsDao.findById(followupId)

    // then
    followup.lastReaction should equal(secondLike)
  }

  it should "load all followups containing given reaction" in {
    // given
    val userFollowup = persistReactionsWithFollowup
    val anotherUserFollowup = persistFollowupForAnotherUser

    // when
    val allWithReaction = followupWithReactionsDao.findAllContainingReaction(comment.id)

    // then
    allWithReaction.map(_.right.get.followupId).toSet should be(Set(userFollowup, anotherUserFollowup))
  }

  // it handles a moment when like is already removed and followups are fetched in order to modify it accordingly
  it should "load followup even if one has no reactions" in {

  }

  it should "update followup when reaction is removed" in {
    // given
    val followupId = persistReactionsWithFollowup
    val Some(Right(followup)) = followupWithReactionsDao.findById(followupId)

    // when
    likeDao.remove(secondLike.id)
    val Some(modified) = followup.removeReaction(secondLike.id)
    followupWithReactionsDao.update(modified)

    //then
    val Some(Right(fetched)) = followupWithReactionsDao.findById(followupId)
    fetched.allReactions.map(_.id).toSet should be(Set(firstLike.id, comment.id))
    fetched.lastReaction should be(firstLike)
  }


  def persistFollowupForAnotherUser = {
    followupDao.createOrUpdateExisting(Followup(anotherUserId, comment))
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
