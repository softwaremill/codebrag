package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.FollowUp
import org.joda.time.DateTime

class MongoFollowUpFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers with MongoFollowUpFinderSpecFixture {

  var followUpDao: FollowUpDAO = _
  var followUpFinder: FollowUpFinder = _

  override def beforeEach() {
    FollowUpRecord.drop
    followUpDao = new MongoFollowUpDAO
    followUpFinder = new MongoFollowUpFinder
  }


  it should "find all follow-ups only for given user" in {
    // given
    storeUserFollowUps
    storeAnotherUserFollowUp

    // when
    val userFollowUps = followUpFinder.findAllFollowUpsForUser(TargetUserId)

    // then
    userFollowUps.followUps should have size(2)
  }

  it should "return user follow-ups with newest first order" in {
    // given
    storeUserFollowUps

    // when
    val userFollowUps = followUpFinder.findAllFollowUpsForUser(TargetUserId).followUps

    // then
    userFollowUps(First).date should equal(laterDate.toDate)
    userFollowUps(Second).date should equal(date.toDate)
  }

  def storeUserFollowUps {
    List(
      FollowUp(CommitInfoBuilder.createRandomCommit(), TargetUserId, date),
      FollowUp(CommitInfoBuilder.createRandomCommit(), TargetUserId, laterDate))
    .foreach(followUpDao.createOrUpdateExisting(_))
  }

  def storeAnotherUserFollowUp {
    followUpDao.createOrUpdateExisting(FollowUp(CommitInfoBuilder.createRandomCommit(), OtherUserId, latestDate))
  }

}

trait MongoFollowUpFinderSpecFixture {

  val TargetUserId = ObjectIdTestUtils.oid(12)
  val OtherUserId = ObjectIdTestUtils.oid(25)

  val date = DateTime.now()
  val laterDate = date.plusMinutes(1)
  val latestDate = date.plusMinutes(10)

  val First = 0
  val Second = 1

}
