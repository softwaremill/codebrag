package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitComment
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.joda.time.DateTime
import org.bson.types.ObjectId

class MongoCommitCommentDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commentDao: MongoCommitCommentDAO = _

  override def beforeEach() {
    CommentRecord.drop

    commentDao = new MongoCommitCommentDAO
  }

  behavior of "MongoCommitCommentDAO"

  it should "store new comment" in {
    // given
    val fixtureCommitId: ObjectId = oid(2)
    val newComment = CommitComment(id = oid(1), fixtureCommitId, authorId = oid(3), message = "well done", new DateTime())

    // when
    commentDao.save(newComment)
    val comments = commentDao.findAllForCommit(fixtureCommitId)

    // then
    comments.head should equal(newComment)
  }

  it should "load all comments for commit id" in {
    // given
    val fixtureCommitId: ObjectId = oid(2)
    val otherFixtureCommitId: ObjectId = oid(3)
    val fixtureCommentList = generateCommentForCommitId(fixtureCommitId)
    val additionalComments = generateCommentForCommitId(otherFixtureCommitId)
    fixtureCommentList.foreach(commentDao.save(_))
    additionalComments.foreach(commentDao.save(_))

    // when
    val comments = commentDao.findAllForCommit(fixtureCommitId)

    // then
    comments should equal(fixtureCommentList)
  }

  private def generateCommentForCommitId(commitId: ObjectId): Seq[CommitComment] = {
   for (i <- 1 to 10) yield
      CommitComment(id = new ObjectId(), commitId, authorId = new ObjectId(), s"Comment $i for commit $commitId", new DateTime())
  }

}
