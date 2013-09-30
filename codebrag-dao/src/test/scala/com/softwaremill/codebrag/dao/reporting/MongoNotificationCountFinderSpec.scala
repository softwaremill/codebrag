package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{CommentAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import scala.util.Random
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoNotificationCountFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MongoNotificationCountFinderSpecFixture {

  val followupDao = new MongoFollowupDAO
  val commitInfoDao = new MongoCommitInfoDAO
  val reviewTaskDao = new MongoCommitReviewTaskDAO
  var notificationCountFinder: NotificationCountFinder = _

  override def beforeEach() {
    super.beforeEach()
    notificationCountFinder = new MongoNotificationCountFinder
  }

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
    //given
    givenFollowUpFor(UserBruceId, DateTime.now().minusDays(1))

    //when
    val counters = notificationCountFinder.getCountersSince(DateTime.now(), UserBruceId)

    //then
    counters.followupCount should equal(0)
  }


  it should "return a number of new followups for the user since a given date" taggedAs RequiresDb in {
    def givenSomeFollowUpsForBruce() {
      for (i <- 0 to 2) {
        givenFollowUpFor(UserBruceId, DateTime.now().minusDays(i))
      }
    }
    //given
    givenSomeFollowUpsForBruce()

    //when
    val counters = notificationCountFinder.getCountersSince(DateTime.now().minusDays(1), UserBruceId)

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

  private def givenFollowUpFor(userId: ObjectId, date: DateTime) {
    FollowupRecord.createRecord
      .receivingUserId(userId)
      .id(new ObjectId(date.toDate))
      .save
  }

  private def givenReviewTaskFor(userId: ObjectId, date: DateTime) {
    CommitReviewTaskRecord.createRecord
      .userId(userId)
      .commitId(CommitInfoAssembler.randomCommit.get.id)
      .id(new ObjectId(date.toDate))
      .save
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

  private def givenReviewTasksFor(userId: ObjectId, count: Int) {
    for (i <- 1 to count) {
      reviewTaskDao.save(CommitReviewTask(CommitInfoAssembler.randomCommit.get.id, userId))
    }
  }

}

trait MongoNotificationCountFinderSpecFixture {

  val UserBruceId = new ObjectId
  val UserSofoklesId = new ObjectId

  val BruceCommitCount = Random.nextInt(100) + 1
  val BruceFollowupCount = Random.nextInt(100) + 1

  val date = DateTime.now()
  val LastCommenterName = "Mary"

}
