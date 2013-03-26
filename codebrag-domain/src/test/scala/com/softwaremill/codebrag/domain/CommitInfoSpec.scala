package com.softwaremill.codebrag.domain

import org.scalatest.{GivenWhenThen, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers

class CommitInfoSpec extends FlatSpec with MockitoSugar with GivenWhenThen with ShouldMatchers {

  val FixtureDate = new DateTime

  behavior of "CommitInfo"
    it should "add a new comment" in {
      Given("empty commit info")
      val commitInfo = CommitInfo("1", "msg", "authorName", "authorName", FixtureDate, List.empty, List.empty)
      val newComment = CommitComment("comment-id", "new comment", "bob", FixtureDate)
      When("add new comment")
      val resultInfo = commitInfo.addComment(newComment)
      Then("result commit should contain new comment")
      resultInfo should equal (CommitInfo("1", "msg", "authorName", "authorName", FixtureDate, List.empty, List(
                               CommitComment("comment-id", "new comment", "bob", FixtureDate))
      ))
    }

}
