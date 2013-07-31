package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.builders.CommentAssembler._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.builders.CommentAssembler

class MongoCommitCommentDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  var commentDao: MongoCommitCommentDAO = _

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)

  override def beforeEach() {
    super.beforeEach()
    commentDao = new MongoCommitCommentDAO
  }

  it should "store new comment for entire commit" taggedAs (RequiresDb) in {
    val newComment = CommentAssembler.commentFor(CommitId).get

    // when
    commentDao.save(newComment)
    val comments = CommentRecord.where(_.id eqs newComment.id).and(_.commitId eqs newComment.commitId).fetch()

    // then
    comments.size should be(1)
    comments.head.message.get should be(newComment.message)
  }

  it should "store new line comment for commit" taggedAs (RequiresDb) in {
    val lineComment = CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("file.txt", 10).get

    // when
    commentDao.save(lineComment)
    val comments = CommentRecord.where(_.id eqs lineComment.id).and(_.commitId eqs lineComment.commitId).fetch()

    // then
    comments.size should be(1)
    val savedComment = comments.head
    savedComment.message.get should equal(lineComment.message)
    savedComment.fileName.value should equal(lineComment.fileName)
    savedComment.lineNumber.value should equal(lineComment.lineNumber)
  }

  it should "load only comments for commit id" taggedAs (RequiresDb) in {
    // given
    val fixtureCommentList = createCommentsFor(CommitId, 3)
    val additionalComments = createCommentsFor(AnotherCommitId, 5)
    fixtureCommentList.foreach(commentDao.save(_))
    additionalComments.foreach(commentDao.save(_))

    // when
    val comments = commentDao.findCommentsForCommit(CommitId)

    // then
    comments should equal(fixtureCommentList)
  }

  it should "find inline comments and comments for entire commit" taggedAs (RequiresDb) in {
    // given
    val comment = commentFor(CommitId).get
    val inlineComment = commentFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    commentDao.save(comment)
    commentDao.save(inlineComment)

    // when
    val comments = commentDao.findCommentsForCommit(CommitId)

    // then
    comments.length should be(2)
    comments.map(_.id).toSet should equal(Set(comment.id, inlineComment.id))
  }

    it should "find all comments in thread containing given comment (general or for the same file and line)" taggedAs(RequiresDb) in {
      // given general comments
      val commitComments = createCommentsFor(CommitId, 3)
      commitComments.foreach(commentDao.save)

      // and some inline comments
      val firstInlineComment = commentFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
      val secondInlineComment = commentFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
      val anotherInlineComment = commentFor(CommitId).withFileNameAndLineNumber("file_2.txt", 20).get
      commentDao.save(firstInlineComment)
      commentDao.save(secondInlineComment)
      commentDao.save(anotherInlineComment)

      // when
      val inlineCommentsRelated = commentDao.findAllCommentsForThread(firstInlineComment.threadId)
      val generalCommentsRelated = commentDao.findAllCommentsForThread(commitComments.head.threadId)

      // then
      println(inlineCommentsRelated)
      inlineCommentsRelated.toSet should be(Set(firstInlineComment, secondInlineComment))
      generalCommentsRelated.toSet should be(commitComments.toSet)
    }


  private def createCommentsFor(commitId: ObjectId, howMany: Int): Seq[Comment] = {
    (1 to howMany).map(i => commentFor(commitId).get).toSeq
  }
}
