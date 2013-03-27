package com.softwaremill.codebrag.domain

import org.scalatest.{GivenWhenThen, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId

class CommitInfoSpec extends FlatSpec with MockitoSugar with GivenWhenThen with ShouldMatchers {

  val FixtureDate = new DateTime
  val FixtureCommentId = new ObjectId("507f191e810c19729de860ea")
  val FixtureCommitId = new ObjectId("507f191e810c19729de860eb")

  behavior of "CommitInfo"
    it should "add a new comment" in {
      Given("empty commit info")
      val commitInfo = CommitInfo(FixtureCommitId, "1", "msg", "authorName", "authorName", FixtureDate, List.empty, List.empty)
      val newComment = CommitComment(FixtureCommentId, "new comment", "bob", FixtureDate)
      When("add new comment")
      val resultInfo = commitInfo.addComment(newComment)
      Then("result commit should contain new comment")
      resultInfo should equal (CommitInfo(FixtureCommitId, "1", "msg", "authorName", "authorName", FixtureDate, List.empty, List(
                               CommitComment(FixtureCommentId, "new comment", "bob", FixtureDate))
      ))
    }

}
