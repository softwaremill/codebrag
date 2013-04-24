package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{InlineCommitComment, EntireCommitComment}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._

class MongoCommitCommentDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commentDao: MongoCommitCommentDAO = _

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)

  override def beforeEach() {
    CommentRecord.drop
    commentDao = new MongoCommitCommentDAO
  }

  it should "store new comment for entire commit" in {
    val newComment = commentForCommit(CommitId)

    // when
    commentDao.save(newComment)
    val comments = CommentRecord.where(_.id eqs newComment.id).and(_.commitId eqs newComment.commitId).fetch()

    // then
    comments.size should be(1)
    comments.head.message.get should be(newComment.message)
  }

  it should "store new line comment for commit" in {
    val lineComment = InlineCommitComment(commentForCommit(CommitId), "myfile.txt", 20)

    // when
    commentDao.save(lineComment)
    val comments = CommentRecord.where(_.id eqs lineComment.commitComment.id).and(_.commitId eqs lineComment.commitComment.commitId).fetch()

    // then
    comments.size should be(1)
    comments.head.message.get should equal(lineComment.commitComment.message)
    comments.head.fileName.valueBox.get should equal(lineComment.fileName)
    comments.head.lineNumber.valueBox.get should equal(lineComment.lineNumber)
  }

  it should "load only comments for commit id" in {
    // given
    val fixtureCommentList = createCommentsForCommitId(CommitId, 3)
    val additionalComments = createCommentsForCommitId(AnotherCommitId, 5)
    fixtureCommentList.foreach(commentDao.save(_))
    additionalComments.foreach(commentDao.save(_))

    // when
    val comments = commentDao.findCommentsForEntireCommit(CommitId)

    // then
    comments should equal(fixtureCommentList)
  }

  it should "find only comments for entire commit" in {
    // given
    val comment = commentForCommit(CommitId)
    val inlineComment = InlineCommitComment(comment, "test.txt", 10)

    // when
    commentDao.save(comment)
    commentDao.save(inlineComment)
    val comments = commentDao.findCommentsForEntireCommit(CommitId)

    // then
    comments.length should be(1)
    comments.head should equal(comment)
  }

  it should "find only inline comments for commit" in {
    // given
    val comment = commentForCommit(CommitId)
    val inlineComment = InlineCommitComment(comment, "test.txt", 10)

    // when
    commentDao.save(comment)
    commentDao.save(inlineComment)
    val comments = commentDao.findInlineCommentsForCommit(CommitId)

    // then
    comments.length should be(1)
    comments.head should equal(inlineComment)
  }

  it should "find all comments in thread containing given comment (general or for the same file and line)" in {
    // given general comments
    val commitComments = createCommentsForCommitId(CommitId, 3)
    commitComments.foreach(commentDao.save)

    // and some inline comments
    val firstInlineComment = createInlineCommentForCommitId(CommitId, "file_1.txt", 10)
    val secondInlineComment = createInlineCommentForCommitId(CommitId, "file_1.txt", 10)
    val anotherInlineComment = createInlineCommentForCommitId(CommitId, "file_2.txt", 20)
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

  private def createCommentsForCommitId(commitId: ObjectId, howMany: Int): Seq[EntireCommitComment] = {
    (1 to howMany).map {i => commentForCommit(commitId, i)}.toSeq
  }

  private def createInlineCommentForCommitId(commitId: ObjectId, fileName: String, lineNumber: Int) = {
      val comment = commentForCommit(commitId)
      InlineCommitComment(comment, fileName, lineNumber)
  }

  private def commentForCommit(commitId: ObjectId, i: Int = 0): EntireCommitComment = {
    EntireCommitComment(id = new ObjectId(), commitId, authorId = new ObjectId(), s"Comment $i for commit $commitId", new DateTime())
  }
}
