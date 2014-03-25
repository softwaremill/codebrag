package com.softwaremill.codebrag.dao.finders.followup

import com.softwaremill.codebrag.dao._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import scala.util.Random
import com.softwaremill.codebrag.common.FixtureTimeClock
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.test.{FlatSpecWithSQL, ClearSQLDataAfterTest}
import com.softwaremill.codebrag.dao.reaction.SQLCommitCommentDAO
import com.softwaremill.codebrag.dao.followup.SQLFollowupDAO
import com.softwaremill.codebrag.dao.commitinfo.SQLCommitInfoDAO
import com.softwaremill.codebrag.dao.user.SQLUserDAO
import org.scalatest.matchers.ShouldMatchers

class SQLFollowupsCountFinderSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with SQLFollowupsCountFinderSpecFixture with ShouldMatchers {

  val commentDao = new SQLCommitCommentDAO(sqlDatabase)
  val followupDao = new SQLFollowupDAO(sqlDatabase)
  val commitInfoDao = new SQLCommitInfoDAO(sqlDatabase)
  val userDao = new SQLUserDAO(sqlDatabase)
  val followupFinder = new SQLFollowupFinder(sqlDatabase, userDao)

  it should "return 0 if no followups found" taggedAs RequiresDb in {
    // given no data for Bruce
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val count = followupFinder.countFollowupsForUser(UserBruceId)

    // then
    count should equal(0)
  }

  it should "return followups count only for given user" taggedAs RequiresDb in {
    // given
    givenFollowupsFor(UserBruceId, BruceFollowupCount)
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val count = followupFinder.countFollowupsForUser(UserBruceId)

    // then
    count should equal(BruceFollowupCount)
  }

  it should "return zero followups if there are no new followups for the user" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.minusDays(1).getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val followupsCount = followupFinder.countFollowupsForUserSince(clock.nowUtc.plusDays(2), UserBruceId)

    //then
    followupsCount should equal(0)
  }


  it should "return a number of new followups for the user since a given date" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val followupsCount = followupFinder.countFollowupsForUserSince(clock.nowUtc.minusDays(1), UserBruceId)

    //then
    followupsCount should equal(1)
  }

  it should "return a number of new and updated followups since a given date when new reactions were added to thread" taggedAs RequiresDb in {
    //given
    val commitId = new ObjectId
    val clock = new FixtureTimeClock(DateTime.now.getMillis)

    val earliestLike = LikeAssembler.likeFor(commitId).withId(ObjectIdTestUtils.withDate(clock.nowUtc.minusHours(3))).withFileNameAndLineNumber("file.txt", 20).get
    val latestLike = LikeAssembler.likeFor(commitId).withId(ObjectIdTestUtils.withDate(clock.nowUtc.minusHours(1))).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, earliestLike))
    followupDao.createOrUpdateExisting(Followup(UserBruceId, latestLike))

    val anotherLike = LikeAssembler.likeFor(commitId).withId(ObjectIdTestUtils.withDate(clock.nowUtc.minusHours(1))).withFileNameAndLineNumber("file.txt", 30).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, anotherLike))

    //when
    val timeBetweenLatestAndEarliestLike = clock.nowUtc.minusHours(2)
    val followupsCount = followupFinder.countFollowupsForUserSince(timeBetweenLatestAndEarliestLike, UserBruceId)

    //then
    followupsCount should equal(2)
  }

  private def givenFollowupsFor(userId: ObjectId, count: Int) {
    val authorId = new ObjectId
    for (i <- 1 to count) {
      val commentedCommit = CommitInfoAssembler.randomCommit.get
      commitInfoDao.storeCommit(commentedCommit)
      val comment = CommentAssembler.commentFor(commentedCommit.id).withAuthorId(authorId).withDate(date).get
      followupDao.createOrUpdateExisting(Followup(userId, comment))
    }
  }

}

trait SQLFollowupsCountFinderSpecFixture {

  val UserBruceId = new ObjectId
  val UserSofoklesId = new ObjectId

  val BruceCommitCount = Random.nextInt(100) + 1
  val BruceFollowupCount = Random.nextInt(100) + 1

  val date = DateTime.now()
  val LastCommenterName = "Mary"

}




