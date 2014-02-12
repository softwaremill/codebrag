package com.softwaremill.codebrag.dao.reaction

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.CommentAssembler
import CommentAssembler._
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.RequiresDb
import org.scalatest.FlatSpec

trait CommitCommentDAOSpec extends FlatSpec with ShouldMatchers {
  def commentDao: CommitCommentDAO

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)
  val YetAnotherCommitId: ObjectId = oid(666)

  it should "store new comment for entire commit" taggedAs (RequiresDb) in {
    val newComment = CommentAssembler.commentFor(CommitId).get

    // when
    commentDao.save(newComment)
    val comments = commentDao.findCommentsForCommits(newComment.commitId)

    // then
    comments.size should be(1)
    comments.head.message should be(newComment.message)
  }

  it should "store new line comment for commit" taggedAs (RequiresDb) in {
    val lineComment = CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("file.txt", 10).get

    // when
    commentDao.save(lineComment)
    val comments = commentDao.findCommentsForCommits(lineComment.commitId)

    // then
    comments.size should be(1)
    val savedComment = comments.head
    savedComment.message should be(lineComment.message)
    savedComment.fileName should be(lineComment.fileName)
    savedComment.lineNumber should be(lineComment.lineNumber)
  }

  it should "load only comments for commit id" taggedAs (RequiresDb) in {
    // given
    val fixtureCommentList = createCommentsFor(CommitId, 3)
    val additionalComments = createCommentsFor(AnotherCommitId, 5)
    fixtureCommentList.foreach(commentDao.save)
    additionalComments.foreach(commentDao.save)

    // when
    val comments = commentDao.findCommentsForCommits(CommitId)

    // then
    comments should equal(fixtureCommentList)
  }

  it should "load comments for given commits" taggedAs (RequiresDb) in {
    // given
    val fixtureCommentList = createCommentsFor(CommitId, 3)
    val additionalComments = createCommentsFor(AnotherCommitId, 5)
    val yetAnotherComments = createCommentsFor(YetAnotherCommitId, 4)
    fixtureCommentList.foreach(commentDao.save)
    additionalComments.foreach(commentDao.save)
    yetAnotherComments.foreach(commentDao.save)

    // when
    val comments = commentDao.findCommentsForCommits(CommitId, AnotherCommitId)

    // then
    comments should equal(fixtureCommentList ++ additionalComments)
  }

  it should "find inline comments and comments for entire commit" taggedAs (RequiresDb) in {
    // given
    val comment = commentFor(CommitId).get
    val inlineComment = commentFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    commentDao.save(comment)
    commentDao.save(inlineComment)

    // when
    val comments = commentDao.findCommentsForCommits(CommitId)

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
    inlineCommentsRelated.toSet should be(Set(firstInlineComment, secondInlineComment))
    generalCommentsRelated.toSet should be(commitComments.toSet)
  }

  private def createCommentsFor(commitId: ObjectId, howMany: Int): Seq[Comment] = {
    (1 to howMany).map(i => commentFor(commitId).get).toSeq
  }
}

class MongoCommitCommentDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with CommitCommentDAOSpec {
  val commentDao = new MongoCommitCommentDAO()
}

class SQLCommitCommentDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with CommitCommentDAOSpec {
  val commentDao = new SQLCommitCommentDAO(sqlDatabase)

  def withSchemas = List(commentDao)
}