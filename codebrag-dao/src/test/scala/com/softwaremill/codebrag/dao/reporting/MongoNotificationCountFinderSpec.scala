package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ThreadDetails
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

  it should "return empty counters if no data found" taggedAs(RequiresDb) in {
    // given no data for Bruce
    givenReviewTasksFor(UserSofoklesId, 2)
    givenFollowupsFor(UserSofoklesId, 8)

    // when
    val resultCounters = notificationCountFinder.getCounters(UserBruceId)

    // then
    resultCounters should equal(NotificationCountersView(0, 0))
  }

    it should "build counters only for given user" taggedAs(RequiresDb) in {
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

  private def givenFollowupsFor(userId: ObjectId, count: Int) {
    val authorId = new ObjectId
    for (i <- 1 to count) {
      val commentedCommit = CommitInfoAssembler.randomCommit.get
      commitInfoDao.storeCommit(commentedCommit)
      val followup = Followup.forComment(commentedCommit.id, authorId, userId, date, LastCommenterName, ThreadDetails(commentedCommit.id))
      followupDao.createOrUpdateExisting(followup)
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
