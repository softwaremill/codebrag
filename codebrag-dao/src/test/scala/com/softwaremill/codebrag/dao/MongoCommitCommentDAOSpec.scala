package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.EntireCommitComment
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.builders.CommentAssembler._

class MongoCommitCommentDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commentDao: MongoCommitCommentDAO = _

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)

  override def beforeEach() {
    CommentRecord.drop
    commentDao = new MongoCommitCommentDAO
  }

  it should "store new comment for entire commit" in {
    val newComment = commitCommentFor(CommitId).get

    // when
    commentDao.save(newComment)
    val comments = CommentRecord.where(_.id eqs newComment.id).and(_.commitId eqs newComment.commitId).fetch()

    // then
    comments.size should be(1)
    comments.head.message.get should be(newComment.message)
  }

  it should "store new line comment for commit" in {
    val lineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("myfile.txt", 20).get

    // when
    commentDao.save(lineComment)
    val comments = CommentRecord.where(_.id eqs lineComment.id).and(_.commitId eqs lineComment.commitId).fetch()

    // then
    comments.size should be(1)
    comments.head.message.get should equal(lineComment.message)
    comments.head.fileName.valueBox.get should equal(lineComment.fileName)
    comments.head.lineNumber.valueBox.get should equal(lineComment.lineNumber)
  }

  it should "load only comments for commit id" in {
    // given
    val fixtureCommentList = createFewCommentsFor(CommitId, 3)
    val additionalComments = createFewCommentsFor(AnotherCommitId, 5)
    fixtureCommentList.foreach(commentDao.save(_))
    additionalComments.foreach(commentDao.save(_))

    // when
    val comments = commentDao.findCommentsForEntireCommit(CommitId)

    // then
    comments should equal(fixtureCommentList)
  }

  it should "find only comments for entire commit" in {
    // given
    val comment = commitCommentFor(CommitId).get
    val inlineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    commentDao.save(comment)
    commentDao.save(inlineComment)

    // when
    val comments = commentDao.findCommentsForEntireCommit(CommitId)

    // then
    comments.length should be(1)
    comments.head should equal(comment)
  }

  it should "find only inline comments for commit" in {
    // given
    val comment = commitCommentFor(CommitId).get
    val inlineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    commentDao.save(comment)
    commentDao.save(inlineComment)

    // when
    val comments = commentDao.findInlineCommentsForCommit(CommitId)

    // then
    comments.length should be(1)
    comments.head should equal(inlineComment)
  }

  it should "find all comments in thread containing given comment (general or for the same file and line)" in {
    // given general comments
    val commitComments = createFewCommentsFor(CommitId, 3)
    commitComments.foreach(commentDao.save)

    // and some inline comments
    val firstInlineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
    val secondInlineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
    val anotherInlineComment = inlineCommentFor(CommitId).withFileNameAndLineNumber("file_2.txt", 20).get
    commentDao.save(firstInlineComment)
    commentDao.save(secondInlineComment)
    commentDao.save(anotherInlineComment)

    // when
    val inlineCommentsRelated = commentDao.findAllCommentsInThreadWith(firstInlineComment)
    val generalCommentsRelated = commentDao.findAllCommentsInThreadWith(commitComments.head)

    // then
    inlineCommentsRelated.toSet should be(Set(firstInlineComment, secondInlineComment))
    generalCommentsRelated.toSet should be(commitComments.toSet)
  }


  private def createFewCommentsFor(commitId: ObjectId, howMany: Int): Seq[EntireCommitComment] = {
    (1 to howMany).map{i => commitCommentFor(commitId).get}.toSeq
  }

}
