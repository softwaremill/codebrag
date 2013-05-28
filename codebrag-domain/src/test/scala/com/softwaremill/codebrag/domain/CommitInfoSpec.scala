package com.softwaremill.codebrag.domain

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.bson.types.ObjectId

class CommitInfoSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers  {

  it should "create review tasks for all users except commit author" in {
    // given
    val commitAuthor = User(new ObjectId, Authentication.basic("user", "pass"), "John Doe", "john@doe.com", "123", "avatarUrl")
    val otherUser = User(new ObjectId, Authentication.basic("user2", "pass2"), "Mary Smith", "mary@smith.com", "456", "avatarUrl")
    val commit = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get

    // when
    val reviewTasks = commit.createReviewTasksFor(List(commitAuthor, otherUser))

    // then
    reviewTasks.find(_.userId == commitAuthor.id) should be(None)
  }

}
