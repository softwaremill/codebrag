package com.softwaremill.codebrag.dao.reviewedcommits

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.common.ClockSpec

trait ReviewedCommitsDAOSpec extends FlatSpec with ShouldMatchers with ClockSpec {

  def reviewedCommitsDao: ReviewedCommitsDAO

  it should "save reviewed commit for user together with review date" in {
    // given
    val userId = ObjectIdTestUtils.oid(999)
    val reviewedCommit = ReviewedCommit("123", userId, "codebrag", clock.nowUtc)

    // when
    reviewedCommitsDao.storeReviewedCommit(reviewedCommit)

    // then
    val reviewedByUser = reviewedCommitsDao.allReviewedByUser(userId, "codebrag")
    reviewedByUser should be(Set(reviewedCommit))
  }

  it should "not store the same commit for user and repo twice" in {
    // given
    val userId = ObjectIdTestUtils.oid(999)
    val reviewedCommit = ReviewedCommit("123", userId, "codebrag", clock.nowUtc)
    reviewedCommitsDao.storeReviewedCommit(reviewedCommit)

    // when
    intercept[Exception] {
      reviewedCommitsDao.storeReviewedCommit(reviewedCommit)
    }

    // then
    val reviewedByUser = reviewedCommitsDao.allReviewedByUser(userId, "codebrag")
    reviewedByUser should be(Set(reviewedCommit))
  }

  it should "fetch all commits reviewed by user" in {
    // given
    val userId = ObjectIdTestUtils.oid(999)
    val firstCommit = ReviewedCommit("123", userId, "codebrag", clock.nowUtc)
    reviewedCommitsDao.storeReviewedCommit(firstCommit)
    val secondCommit = ReviewedCommit("456", userId, "codebrag", clock.nowUtc)
    reviewedCommitsDao.storeReviewedCommit(secondCommit)

    // when
    val allReviewedByUser = reviewedCommitsDao.allReviewedByUser(userId, "codebrag")

    // then
    allReviewedByUser should be(Set(firstCommit, secondCommit))
  }

}

class SQLReviewedCommitsDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ReviewedCommitsDAOSpec {
  var reviewedCommitsDao = new SQLReviewedCommitsDAO(sqlDatabase)
}

