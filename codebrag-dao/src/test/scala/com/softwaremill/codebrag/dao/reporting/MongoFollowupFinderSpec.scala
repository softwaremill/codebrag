package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.domain.ThreadDetails
import scala.Some
import com.softwaremill.codebrag.dao._

class MongoFollowupFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MongoFollowupFinderSpecFixture {

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var followupFinder: FollowupFinder = _

  override def beforeEach() {
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
    commitInfoDao = new MongoCommitInfoDAO
    storeAllCommits
  }

  it should "find all follow-ups only for given user" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn
    storeFollowupForBob

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    userFollowups.followups should have size(2)
  }

  it should "find followup by id for given user" taggedAs(RequiresDb) in {
    // given
    val followupsStored = storeFollowupsForJohn
    val Some(followupIdToFind) = followupsStored.head.id

    // when & then
    val Right(followupFound) = followupFinder.findFollowupForUser(JohnId, followupIdToFind)
  }

  it should "not find followup by id for another user" taggedAs(RequiresDb) in {
    // given
    val followupsStored = storeFollowupsForJohn
    val Some(followupIdToFind) = followupsStored.head.id

    // when & then
    val Left(msg) = followupFinder.findFollowupForUser(BobId, followupIdToFind)
  }

  it should "return user follow-ups with newest first order" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId).followups

    // then
    userFollowups(First).date should equal(laterDate.toDate)
    userFollowups(Second).date should equal(date.toDate)
  }

  it should "return followup with last commenter name and comment id included" taggedAs(RequiresDb) in {
    // given
    val stored = storeFollowupForBob

    // when
    val followup = followupFinder.findAllFollowupsForUser(BobId).followups.head

    // then
    followup.comment.commenterName should be(LastCommenterName)
    followup.comment.commentId should be(followup.comment.commentId)
  }

  def storeFollowupsForJohn = {
    val followups = List(
      Followup(ObjectIdTestUtils.oid(100), FixtureCommit1.id, JohnId, date, LastCommenterName, ThreadDetails(FixtureCommit1.id)),
      Followup(ObjectIdTestUtils.oid(200), FixtureCommit2.id, JohnId, laterDate, LastCommenterName, ThreadDetails(FixtureCommit2.id)))
    followups.foreach(followupDao.createOrUpdateExisting(_))
    followups
  }

  def storeFollowupForBob = {
    val followup = Followup(ObjectIdTestUtils.oid(300), FixtureCommit3.id, BobId, latestDate, LastCommenterName, ThreadDetails(FixtureCommit3.id))
    followupDao.createOrUpdateExisting(followup)
    followup
  }

  def storeAllCommits {
    List(FixtureCommit1, FixtureCommit2, FixtureCommit3).foreach(commitInfoDao.storeCommit(_))
  }

}

trait MongoFollowupFinderSpecFixture {

  val JohnId = ObjectIdTestUtils.oid(12)
  val BobId = ObjectIdTestUtils.oid(25)

  val date = DateTime.now()
  val laterDate = date.plusMinutes(1)
  val latestDate = date.plusMinutes(10)

  val LastCommenterName = "Mary"

  val First = 0
  val Second = 1

  val FixtureCommit1 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit2 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit3 = CommitInfoAssembler.randomCommit.get
}
