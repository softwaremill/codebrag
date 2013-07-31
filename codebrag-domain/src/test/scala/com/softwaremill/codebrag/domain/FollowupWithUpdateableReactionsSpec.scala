package com.softwaremill.codebrag.domain

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler}
import org.joda.time.DateTime

class FollowupWithUpdateableReactionsSpec extends FlatSpec with ShouldMatchers {

  val commitId = new ObjectId
  val userId = new ObjectId
  val followupId = new ObjectId
  val thread = ThreadDetails(commitId)
  val baseDate = DateTime.now

  val comment = CommentAssembler.commentFor(commitId).withDate(baseDate).get
  val like = LikeAssembler.likeFor(commitId).withDate(baseDate.plusHours(1)).get

  it should "remove given reaction from followup" in {
    // given
    val followup = FollowupWithUpdateableReactions(followupId, userId, thread, like, List(comment, like))

    // when
    val modifiedFollowup = followup.removeReaction(like.id)

    // then
    modifiedFollowup.lastReaction should be(comment)
    modifiedFollowup.allReactions should be(List(comment))
  }

  it should "tell if empty if last reaction was removed" in {
    // given
    val followup = FollowupWithUpdateableReactions(followupId, userId, thread, like, List(comment, like))

    // when
    val modifiedFollowup = followup.removeReaction(like.id).removeReaction(comment.id)

    // then
    modifiedFollowup.isEmpty should be(true)
    modifiedFollowup.lastReaction should be(null)
    modifiedFollowup.allReactions should be('empty)
  }

  it should "set last reaction to the newest one when any reaction is removed " in {
    // given
    val veryFirstComment = CommentAssembler.commentFor(commitId).withDate(baseDate.minusHours(3)).get
    val followup = FollowupWithUpdateableReactions(followupId, userId, thread, like, List(veryFirstComment, comment, like))

    // when
    val modifiedFollowup = followup.removeReaction(veryFirstComment.id)

    // then
    modifiedFollowup.lastReaction should be(like)
    modifiedFollowup.allReactions.toSet should be(Set(like, comment))
  }

}
