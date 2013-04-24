package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User, EntireCommitComment}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.builders.CommentAssembler
import org.scalatest.mock.MockitoSugar

class MongoCommentListFinderSpec extends FlatSpecWithRemoteMongo with BeforeAndAfterEach with ShouldMatchers with CommentListFinderVerifyHelpers {

  val userDao = new MongoUserDAO
  val commentDao = new MongoCommitCommentDAO
  var commentListFinder: MongoCommentListFinder = _

  val CommitId = oid(1)

  val John = User(oid(2), Authentication.basic("john", "pass"), "John", "john@doe.com", "123abc")
  val Mary = User(oid(3), Authentication.basic("mary", "pass"), "Mary", "mary@smith.com", "123abc")

  val StoredCommitComments = List(
    CommentAssembler.commitCommentFor(CommitId).withAuthorId(John.id).withMessage("Monster class").get,
    CommentAssembler.commitCommentFor(CommitId).withAuthorId(Mary.id).withMessage("Fix it ASAP").get
  )

  val StoredInlineComments = List(
    CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Cool thing").withAuthorId(John.id).get,
    CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Indeed").withAuthorId(Mary.id).get,
    CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Possible NPE?").withAuthorId(Mary.id).get,
    CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Nope").withAuthorId(John.id).get,
    CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Database.scala", 20).withMessage("Refactor that").withAuthorId(John.id).get
  )

  override def beforeEach() {
    CommentRecord.drop
    UserRecord.drop
    commentListFinder = new MongoCommentListFinder(userDao)

    StoredCommitComments.foreach(commentDao.save)
    StoredInlineComments.foreach(commentDao.save)
    List(John, Mary).foreach(userDao.add)

  }

  it should "be empty if there are no comments for a commit" in {
    // given
    val commitWithNoCommentsId = oid(20)

    // when
    val commentList = commentListFinder.commentsForCommit(commitWithNoCommentsId)

    // then
    commentList.comments should be('empty)
  }

  it should "contain comments for whole commit" in {
    // given
    val firstComment = StoredCommitComments(0)
    val secondComment = StoredCommitComments(1)

    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    commentMessagesWithAuthorsFor(commentsView.comments) should be(Set(("Monster class", "John"), ("Fix it ASAP", "Mary")))
  }

  it should "contain inline comments grouped by file and line" in {
    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    val fileComments = commentsView.inlineComments

    lineCommentsFor(fileComments, "Main.scala").size should be(1)
    commentLineNumbersFor(fileComments, "Main.scala") should be(Set(10))
    commentMessagesWithAuthorsFor(fileComments, "Main.scala", 10) should be(Set(("Cool thing", "John"), ("Indeed", "Mary")))

    lineCommentsFor(fileComments, "Database.scala").size should be(2)
    commentLineNumbersFor(fileComments, "Database.scala") should be(Set(12, 20))
    commentMessagesWithAuthorsFor(fileComments, "Database.scala", 12) should be(Set(("Possible NPE?", "Mary"), ("Nope", "John")))
    commentMessagesWithAuthorsFor(fileComments, "Database.scala", 20) should be(Set(("Refactor that", "John")))
  }

  it should "have comments ordered by date starting from the oldest" in {
    // given
    val baseDate = DateTime.now
    val commentBase = CommentAssembler.inlineCommentFor(CommitId).withFileNameAndLineNumber("Exception.scala", 10)
    val inlineComments = List(
      commentBase.withMessage("You'd better refactor that").withAuthorId(John.id).postedAt(baseDate.plusHours(1)).get,
      commentBase.withMessage("Man, it's Monday").withAuthorId(Mary.id).postedAt(baseDate.plusHours(2)).get
    )
    inlineComments.foreach(commentDao.save)

    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    val fileComments = commentsView.inlineComments
    orderedCommentMessagesFor(fileComments, "Exception.scala", 10) should be(List("You'd better refactor that", "Man, it's Monday"))
  }

}




trait CommentListFinderVerifyHelpers {

  def lineCommentsFor(fileComments: List[FileCommentsView], fileName: String) = {
    fileComments.find(_.fileName == fileName).get.lineComments
  }

  def commentLineNumbersFor(fileComments: List[FileCommentsView], fileName: String) = {
    lineCommentsFor(fileComments, fileName).map(_.lineNumber).toSet
  }

  def commentMessagesWithAuthorsFor(fileComments: List[FileCommentsView], fileName: String, lineNumber: Int) = {
    lineCommentsFor(fileComments, fileName).find(_.lineNumber == lineNumber).get.comments.map(comment => (comment.message, comment.authorName)).toSet
  }

  def commentMessagesWithAuthorsFor(comments: List[SingleCommentView]) = {
    comments.map(comment => (comment.message, comment.authorName)).toSet
  }

  def orderedCommentMessagesFor(fileComments: List[FileCommentsView], fileName: String, lineNumber: Int) = {
    lineCommentsFor(fileComments, fileName).find(_.lineNumber == lineNumber).get.comments.map(comment => comment.message)
  }

}
