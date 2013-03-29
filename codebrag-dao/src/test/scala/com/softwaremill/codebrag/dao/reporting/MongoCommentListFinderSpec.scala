package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User, CommitComment, CommitReview}
import org.joda.time.DateTime

class MongoCommentListFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  behavior of "MongoCommentListFinder"
  var commentListFinder: MongoCommentListFinder = _
  val reviewedCommitId = 2
  val comment1Id = 0
  val comment2Id = 1

  val comment1Time = new DateTime().minusHours(2)
  val comment2Time = new DateTime().minusHours(1)
  val userSofoklesId = 1
  val userRobertId = 2

  override def beforeEach = {
    CommitReviewRecord.drop
    UserRecord.drop
    commentListFinder = new MongoCommentListFinder
  }

  it should "load empty list if there's no review for a commit" in {
    // given
    val dummyCommitId = 1

    // when
    val commentList = commentListFinder.findAllForCommit(dummyCommitId)

    // then
    commentList.comments should be ('empty)
  }

  it should "load comments in descending order" in {
    // given
    val comment1 = CommitComment(comment1Id, userSofoklesId, "nice!", comment1Time)
    val comment2 = CommitComment(comment2Id, userRobertId, "indeed", comment2Time)
    val review = CommitReview(reviewedCommitId, List(comment2, comment1))
    new MongoCommitReviewDAO().save(review)
    new MongoUserDAO().add(userWithLogin(userSofoklesId, "sofokles"))
    new MongoUserDAO().add(userWithLogin(userRobertId, "robert"))

    // when
    val commentList = commentListFinder.findAllForCommit(reviewedCommitId)

    // then
    commentList.comments.size should equal (2)
    commentList.comments(0) should equal (CommentListItemDTO(comment1Id, "sofokles", "nice!", comment1Time.toDate))
    commentList.comments(1) should equal (CommentListItemDTO(comment2Id, "robert", "indeed", comment2Time.toDate))
  }

  it should "set unknown user name if he does not exist" in {
    // given
    val comment = CommitComment(comment1Id, userRobertId, "good one", comment1Time)
    val review = CommitReview(reviewedCommitId, List(comment))
    new MongoCommitReviewDAO().save(review)

    // when
    val commentList = commentListFinder.findAllForCommit(reviewedCommitId)

    // then
    commentList.comments.size should equal (1)
    commentList.comments(0) should equal (CommentListItemDTO(comment1Id, "Unknown user", "good one", comment1Time.toDate))
  }



  def userWithLogin(idDigit: Int, login: String) = {
    val authentication: Authentication = Authentication("Basic", login, login, "token", "salt")
    User(idDigit, authentication, login, login + "@sml.com", "token")
  }
}
