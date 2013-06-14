package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User}
import org.joda.time.DateTime
import com.softwaremill.codebrag.builders.CommentAssembler
import com.softwaremill.codebrag.dao.reporting.views.SingleCommentView
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoCommentFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with CommentListFinderVerifyHelpers {

  val userDao = new MongoUserDAO
  val commentDao = new MongoCommitCommentDAO
  var commentListFinder: MongoCommentFinder = _

  val CommitId = oid(1)

  val John = User(oid(2), Authentication.basic("john", "pass"), "John", "john@doe.com", "123abc", "http://john.doe.com/avatar")
  val Mary = User(oid(3), Authentication.basic("mary", "pass"), "Mary", "mary@smith.com", "123abc", "http://mary.com/avatar")

  val StoredCommitComments = List(
    CommentAssembler.commentFor(CommitId).withAuthorId(John.id).withMessage("Monster class").get,
    CommentAssembler.commentFor(CommitId).withAuthorId(Mary.id).withMessage("Fix it ASAP").get
  )

  val StoredInlineComments = List(
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Cool thing").withAuthorId(John.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Indeed").withAuthorId(Mary.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Possible NPE?").withAuthorId(Mary.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Nope").withAuthorId(John.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 20).withMessage("Refactor that").withAuthorId(John.id).get
  )

  override def beforeEach() {
    super.beforeEach()
    commentListFinder = new MongoCommentFinder(userDao)

    StoredCommitComments.foreach(commentDao.save)
    StoredInlineComments.foreach(commentDao.save)
    List(John, Mary).foreach(userDao.add)
  }

  it should "be empty if there are no comments for a commit" taggedAs (RequiresDb) in {
    // given
    val commitWithNoCommentsId = oid(20)

    // when
    val commentList = commentListFinder.commentsForCommit(commitWithNoCommentsId)

    // then
    commentList.comments should be('empty)
  }

  it should "contain comments for whole commit" taggedAs (RequiresDb) in {
    // given
    val firstComment = StoredCommitComments(0)
    val secondComment = StoredCommitComments(1)

    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    commentMessagesWithAuthorsFor(commentsView.comments) should be(Set(("Monster class", "John"), ("Fix it ASAP", "Mary")))
  }

  it should "contain inline comments grouped by file and line" taggedAs (RequiresDb) in {
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

  it should "have comments ordered by date starting from the oldest" taggedAs (RequiresDb) in {
    // given
    val baseDate = DateTime.now
    val commentBase = CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Exception.scala", 10)
    val inlineComments = List(
      commentBase.withMessage("You'd better refactor that").withAuthorId(John.id).postedAt(baseDate.plusHours(1)).get,
      commentBase.withMessage("Man, it's Monday").withAuthorId(Mary.id).postedAt(baseDate.plusHours(2)).get
    )
    inlineComments.foreach(commentDao.save)

    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    val fileComments = commentsView.inlineComments
    val fileComments2 = commentsView.inlineComments
    orderedCommentMessagesFor(fileComments, "Exception.scala", 10) should be(List("You'd better refactor that", "Man, it's Monday"))
  }

  it should "return author avatar in comment" in {
    // given
    val johnComment = StoredCommitComments(0)

    // when
    val commentsView = commentListFinder.commentsForCommit(CommitId)

    // then
    commentsView.comments(0).authorAvatarUrl should equal(John.avatarUrl)
  }

  it should "return empty string as author avatar if author not registered in codebrag" in {
    // given
    val emptyAvatarUrl = ""
    val dummyCommitId = ObjectIdTestUtils.oid(123123)
    val commentFromNonexistingUser = CommentAssembler.commentFor(dummyCommitId).withAuthorId(ObjectIdTestUtils.oid(1111111)).get
    commentDao.save(commentFromNonexistingUser)

    // when
    val commentsView = commentListFinder.commentsForCommit(dummyCommitId)

    // then
    commentsView.comments(0).authorAvatarUrl should equal(emptyAvatarUrl)
  }

}


trait CommentListFinderVerifyHelpers {

  def lineCommentsFor(fileComments: Map[String, Map[Int, List[SingleCommentView]]], fileName: String) = {
    fileComments(fileName)
  }

  def commentLineNumbersFor(fileComments: Map[String, Map[Int, List[SingleCommentView]]], fileName: String) = {
    lineCommentsFor(fileComments, fileName).map(_._1).toSet
  }

  def commentMessagesWithAuthorsFor(fileComments: Map[String, Map[Int, List[SingleCommentView]]], fileName: String, lineNumber: Int) = {
    lineCommentsFor(fileComments, fileName)(lineNumber).map(comment => (comment.message, comment.authorName)).toSet
  }

  def commentMessagesWithAuthorsFor(comments: List[SingleCommentView]) = {
    comments.map(comment => (comment.message, comment.authorName)).toSet
  }

  def orderedCommentMessagesFor(fileComments: Map[String, Map[Int, List[SingleCommentView]]], fileName: String, lineNumber: Int) = {
    lineCommentsFor(fileComments, fileName)(lineNumber).map(comment => comment.message)
  }

}

