package com.softwaremill.codebrag.domain

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId

class CommitInfoSpec extends FlatSpec with MockitoSugar with ShouldMatchers {

  val FixtureDate = new DateTime
  val FixtureCommentId = new ObjectId("507f191e810c19729de860ea")
  val FixtureCommitId = new ObjectId("507f191e810c19729de860eb")
  val EmptyParentsList = List.empty
  val EmptyCommentsList = List.empty
  val EmptyFilesList = List.empty

  behavior of "CommitInfo"
  it should "add a new comment" in {
    //given
    val commitInfo = CommitInfo(FixtureCommitId, "1", "msg", "authorName", "authorName", FixtureDate, EmptyParentsList, EmptyCommentsList, EmptyFilesList)
    val newComment = CommitComment(FixtureCommentId, "new comment", "bob", FixtureDate)

    //when
    val resultInfo = commitInfo.addComment(newComment)

    //then
    resultInfo should equal(
      CommitInfo(FixtureCommitId, "1", "msg", "authorName", "authorName", FixtureDate, EmptyParentsList, List(newComment), EmptyFilesList)
    )
  }

}
