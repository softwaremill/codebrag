package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Followup
import org.joda.time.DateTime

class MongoFollowupFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers with MongoFollowupFinderSpecFixture {

  var followupDao: FollowupDAO = _
  var followupFinder: FollowupFinder = _

  override def beforeEach() {
    FollowupRecord.drop
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
  }


  it should "find all follow-ups only for given user" in {
    // given
    storeUserFollowups
    storeAnotherUserFollowup

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(TargetUserId)

    // then
    userFollowups.followups should have size(2)
  }

  it should "return user follow-ups with newest first order" in {
    // given
    storeUserFollowups

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(TargetUserId).followups

    // then
    userFollowups(First).date should equal(laterDate.toDate)
    userFollowups(Second).date should equal(date.toDate)
  }

  def storeUserFollowups {
    List(
      Followup(CommitInfoBuilder.createRandomCommit(), TargetUserId, date),
      Followup(CommitInfoBuilder.createRandomCommit(), TargetUserId, laterDate))
    .foreach(followupDao.createOrUpdateExisting(_))
  }

  def storeAnotherUserFollowup {
    followupDao.createOrUpdateExisting(Followup(CommitInfoBuilder.createRandomCommit(), OtherUserId, latestDate))
  }

}

trait MongoFollowupFinderSpecFixture {

  val TargetUserId = ObjectIdTestUtils.oid(12)
  val OtherUserId = ObjectIdTestUtils.oid(25)

  val date = DateTime.now()
  val laterDate = date.plusMinutes(1)
  val latestDate = date.plusMinutes(10)

  val First = 0
  val Second = 1

}
