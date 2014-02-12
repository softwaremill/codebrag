package com.softwaremill.codebrag.dao.finders.notification

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import scala.util.Random
import com.softwaremill.codebrag.common.{RealTimeClock, FixtureTimeClock}
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.commitinfo.{SQLCommitInfoDAO, CommitInfoDAO, MongoCommitInfoDAO}
import com.softwaremill.codebrag.dao.reviewtask.{SQLCommitReviewTaskDAO, CommitReviewTaskDAO, CommitReviewTaskRecord, MongoCommitReviewTaskDAO}
import com.softwaremill.codebrag.dao.followup._
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.finders.views.NotificationCountersView

trait NotificationCountFinderSpec extends FlatSpec with ShouldMatchers with NotificationCountFinderSpecFixture {

  def followupDao: FollowupDAO
  def commitInfoDao: CommitInfoDAO
  def reviewTaskDao: CommitReviewTaskDAO
  def notificationCountFinder: NotificationCountFinder

  "getCounters" should "return empty counters if no data found" taggedAs RequiresDb in {
    // given no data for Bruce
    givenReviewTasksFor(UserSofoklesId, 2)
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val resultCounters = notificationCountFinder.getCounters(UserBruceId)

    // then
    resultCounters should equal(NotificationCountersView(0, 0))
  }

  it should "build counters only for given user" taggedAs RequiresDb in {
    // given
    givenReviewTasksFor(UserBruceId, BruceCommitCount)
    givenFollowupsFor(UserBruceId, BruceFollowupCount)

    givenReviewTasksFor(UserSofoklesId, 2)
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val resultCounters = notificationCountFinder.getCounters(UserBruceId)

    // then
    resultCounters should equal(NotificationCountersView(BruceCommitCount, BruceFollowupCount))
  }

  "getCountersSince" should "return zero followups if there are no new followups for the user" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.minusDays(1).getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val counters = notificationCountFinder.getCountersSince(clock.nowUtc.plusDays(2), UserBruceId)

    //then
    counters.followupCount should equal(0)
  }


  it should "return a number of new followups for the user since a given date" taggedAs RequiresDb in {
    // given
    val clock = new FixtureTimeClock(DateTime.now.getMillis)
    val oldLike = LikeAssembler.likeFor(new ObjectId).withId(ObjectIdTestUtils.withDate(clock.nowUtc)).withFileNameAndLineNumber("file.txt", 20).get
    followupDao.createOrUpdateExisting(Followup(UserBruceId, oldLike))

    //when
    val counters = notificationCountFinder.getCountersSince(clock.nowUtc.minusDays(1), UserBruceId)

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
    val counters = notificationCountFinder.getCountersSince(timeBetweenLatestAndEarliestLike, UserBruceId)

    //then
    counters.followupCount should equal(2)
  }

  it should "return zero commits if there are no new review tasks for the user" taggedAs RequiresDb in {
    def givenNoNewReviewTasksForBruce() {
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(1))
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(2))
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(3))
    }

    //given
    val queryDate = DateTime.now()
    givenNoNewReviewTasksForBruce()

    //when
    val counters = notificationCountFinder.getCountersSince(queryDate, UserBruceId)

    //then
    counters.pendingCommitCount should equal(0)
  }

  it should "return a number of new commits for the user since a given date" taggedAs RequiresDb in {
    def givenSomeNewReviewTasksForBruce() {
      givenReviewTaskFor(UserBruceId, DateTime.now())
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(1))
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(2))
      givenReviewTaskFor(UserBruceId, DateTime.now().minusDays(3))
    }

    //given
    val queryDate = DateTime.now().minusDays(2)
    givenSomeNewReviewTasksForBruce()

    //when
    val counters = notificationCountFinder.getCountersSince(queryDate, UserBruceId)

    //then
    counters.pendingCommitCount should equal(3)
  }

  def givenReviewTaskFor(userId: ObjectId, date: DateTime)

  private def givenFollowupsFor(userId: ObjectId, count: Int) {
    val authorId = new ObjectId
    for (i <- 1 to count) {
      val commentedCommit = CommitInfoAssembler.randomCommit.get
      commitInfoDao.storeCommit(commentedCommit)
      val comment = CommentAssembler.commentFor(commentedCommit.id).withAuthorId(authorId).withDate(date).get
      followupDao.createOrUpdateExisting(Followup(userId, comment))
    }
  }

  private def givenReviewTasksFor(userId: ObjectId, count: Int) {
    for (i <- 1 to count) {
      reviewTaskDao.save(CommitReviewTask(CommitInfoAssembler.randomCommit.get.id, userId))
    }
  }

}

trait NotificationCountFinderSpecFixture {

  val UserBruceId = new ObjectId
  val UserSofoklesId = new ObjectId

  val BruceCommitCount = Random.nextInt(100) + 1
  val BruceFollowupCount = Random.nextInt(100) + 1

  val date = DateTime.now()
  val LastCommenterName = "Mary"

}

class MongoNotificationCountFinderSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with NotificationCountFinderSpec {
  val followupDao = new MongoFollowupDAO()
  val commitInfoDao = new MongoCommitInfoDAO()
  val reviewTaskDao = new MongoCommitReviewTaskDAO()
  val notificationCountFinder = new MongoNotificationCountFinder()

  def givenReviewTaskFor(userId: ObjectId, date: DateTime) {
    CommitReviewTaskRecord.createRecord
      .userId(userId)
      .commitId(CommitInfoAssembler.randomCommit.get.id)
      .id(new ObjectId(date.toDate))
      .save
  }
}

class SQLNotificationCountFinderSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with NotificationCountFinderSpec {
  val followupDao = new SQLFollowupDAO(sqlDatabase)
  val commitInfoDao = new SQLCommitInfoDAO(sqlDatabase)
  val reviewTaskDao = new SQLCommitReviewTaskDAO(sqlDatabase, RealTimeClock)
  val notificationCountFinder = new SQLNotificationCountFinder(sqlDatabase)

  def withSchemas = List(followupDao, commitInfoDao, reviewTaskDao)

  def givenReviewTaskFor(userId: ObjectId, date: DateTime) {
    new SQLCommitReviewTaskDAO(sqlDatabase, new FixtureTimeClock(date.getMillis))
      .save(CommitReviewTask(CommitInfoAssembler.randomCommit.get.id, userId))
  }
}
