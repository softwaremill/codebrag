package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User, EntireCommitComment}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.builders.CommitCommentAssembler

class MongoCommentListFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  val commentDao = new MongoCommitCommentDAO
  var commentListFinder: MongoCommentListFinder = _

  val reviewedCommitId = 2
  val otherCommitId = 3
  val comment1Id = 0
  val comment2Id = 1

  val comment1Time = new DateTime().minusHours(2)
  val comment2Time = new DateTime().minusHours(1)
  val userSofoklesId = 1
  val userRobertId = 2

  override def beforeEach() {
    CommentRecord.drop
    UserRecord.drop
    commentListFinder = new MongoCommentListFinder
  }

  it should "be empty if there are no comments for a commit" in {
    // given
    val commitWithNoCommentsId = 1

    // when
    val commentList = commentListFinder.commentsForCommit(oid(commitWithNoCommentsId))

    // then
    commentList.comments should be('empty)
  }

  it should "contain only comments for whole commit when no inline comments present for commit" in {
    // given
    val noInlineCommentsCommitId = oid(1)
    val firstComment = CommitCommentAssembler.commentForCommitId(noInlineCommentsCommitId).get
    val secondComment = CommitCommentAssembler.commentForCommitId(noInlineCommentsCommitId).get
    commentDao.save(firstComment)
    commentDao.save(secondComment)

    // when
    val commentsView = commentListFinder.commentsForCommit(noInlineCommentsCommitId)

    // then
    commentsView.inlineComments should be('empty)
    commentsView.comments.size should be(2)
    val firstCommentView = SingleCommentView(firstComment.id.toString, "???", firstComment.message, firstComment.postingTime.toDate)
    val secondCommentView = SingleCommentView(secondComment.id.toString, "???", secondComment.message, secondComment.postingTime.toDate)
    commentsView.comments.toSet should be(Set(firstCommentView, secondCommentView))
  }






  it should "load empty list if there's no review for a commit" in {
    // given
    val dummyCommitId = 1

    // when
    val commentList = commentListFinder.findAllForCommit(oid(dummyCommitId))

    // then
    commentList.comments should be('empty)
  }

  it should "load comments in descending order" in {
    // given
    val storedComments = List(
      EntireCommitComment(oid(comment1Id), oid(reviewedCommitId), oid(userSofoklesId), "nice!", comment1Time),
      EntireCommitComment(oid(comment2Id), oid(reviewedCommitId), oid(userRobertId), "indeed", comment2Time),
      EntireCommitComment(new ObjectId, oid(otherCommitId), oid(userRobertId), "wat!", new DateTime())
    )
    storedComments.foreach(new MongoCommitCommentDAO().save(_))
    new MongoUserDAO().add(userWithLogin(userSofoklesId, "sofokles"))
    new MongoUserDAO().add(userWithLogin(userRobertId, "robert"))

    // when
    val commentList = commentListFinder.findAllForCommit(oid(reviewedCommitId))

    // then
    commentList.comments.size should equal(2)
    commentList.comments(0) should equal(CommentListItemDTO(oid(comment1Id).toString, "sofokles", "nice!", comment1Time.toDate))
    commentList.comments(1) should equal(CommentListItemDTO(oid(comment2Id).toString, "robert", "indeed", comment2Time.toDate))
  }

  it should "set unknown user name if he does not exist" in {
    // given
    val comment = EntireCommitComment(oid(comment1Id), oid(reviewedCommitId), oid(userRobertId), "good one", comment1Time)
    new MongoCommitCommentDAO().save(comment)
    // when
    val commentList = commentListFinder.findAllForCommit(oid(reviewedCommitId))

    // then
    commentList.comments.size should equal(1)
    commentList.comments(0) should equal(CommentListItemDTO(oid(comment1Id).toString, "Unknown user", "good one", comment1Time.toDate))
  }


  def userWithLogin(idDigit: Int, login: String) = {
    val authentication: Authentication = Authentication("Basic", login, login, "token", "salt")
    User(oid(idDigit), authentication, login, login + "@sml.com", "token")
  }
}
