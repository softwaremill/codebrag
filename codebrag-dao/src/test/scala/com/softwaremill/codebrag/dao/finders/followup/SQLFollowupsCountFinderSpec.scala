package com.softwaremill.codebrag.dao.finders.followup

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import scala.util.Random
import com.softwaremill.codebrag.common.FixtureTimeClock
import com.softwaremill.codebrag.test.FlatSpecWithSQL
import com.softwaremill.codebrag.dao.commitinfo.SQLCommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.followup._
import com.softwaremill.codebrag.dao.finders.views.NotificationCountersView
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.dao.finders.views.NotificationCountersView

class SQLFollowupsCountFinderSpec extends SQLFollowupFinderSpec with SQLFollowupsCountFinderSpecFixture {

  "getCounters" should "return empty counters if no data found" taggedAs RequiresDb in {
    // given no data for Bruce
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val resultCounters = followupFinder.countFollowupsForUser(UserBruceId)

    // then
    resultCounters should equal(NotificationCountersView(0, 0))
  }

  it should "build counters only for given user" taggedAs RequiresDb in {
    // given
    givenFollowupsFor(UserBruceId, BruceFollowupCount)
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val resultCounters = followupFinder.countFollowupsForUser(UserBruceId)

    // then
    resultCounters should equal(NotificationCountersView(0, BruceFollowupCount))
  }

  "getCountersSince" should "return zero followups if there are no new followups for the user" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.minusDays(1).getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val counters = followupFinder.countFollowupsForUserSince(clock.nowUtc.plusDays(2), UserBruceId)

    //then
    counters.followupCount should equal(0)
  }


  it should "return a number of new followups for the user since a given date" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val counters = followupFinder.countFollowupsForUserSince(clock.nowUtc.minusDays(1), UserBruceId)

    //then
    counters.followupCount should equal(1)
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
    val counters = followupFinder.countFollowupsForUserSince(timeBetweenLatestAndEarliestLike, UserBruceId)

    //then
    counters.followupCount should equal(2)
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




