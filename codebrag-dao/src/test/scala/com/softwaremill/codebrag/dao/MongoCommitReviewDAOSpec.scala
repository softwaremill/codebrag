package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{CommitComment, CommitReview}
import org.joda.time.DateTime

class MongoCommitReviewDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commitReviewDAO: MongoCommitReviewDAO = _

  override def beforeEach() {
    CommitReviewRecord.drop // drop collection to start every test with fresh database
    commitReviewDAO = new MongoCommitReviewDAO
  }

  behavior of "MongoCommitReviewDAO"

  it should "return None if provided unknown identifier" in {
    // given empty storage
    // when
    val reviewOption = commitReviewDAO.findById(new ObjectId())
    // then
    reviewOption.isDefined should be(false)
  }

  it should "return previously saved review object" in {
    // given
    val commitReview = CommitReview(new ObjectId(),
      List(CommitComment(new ObjectId(), new ObjectId(), "comment msg1", new DateTime()),
        CommitComment(new ObjectId(), new ObjectId(), "comment msg2", new DateTime())))
    // when
    commitReviewDAO.save(commitReview)
    val reviewOption = commitReviewDAO.findById(commitReview.commitId)

    // then
    reviewOption.isDefined should be(true)
    reviewOption.get should equal(commitReview)
  }
}